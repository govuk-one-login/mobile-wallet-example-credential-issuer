package uk.gov.di.mobile.wallet.cri.credential;

import com.nimbusds.jwt.JWTClaimsSet;
import com.nimbusds.jwt.SignedJWT;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import uk.gov.di.mobile.wallet.cri.credential.mobile_driving_licence.MDLException;
import uk.gov.di.mobile.wallet.cri.models.CachedCredentialOffer;
import uk.gov.di.mobile.wallet.cri.models.StoredCredential;
import uk.gov.di.mobile.wallet.cri.services.authentication.AccessTokenService;
import uk.gov.di.mobile.wallet.cri.services.authentication.AccessTokenValidationException;
import uk.gov.di.mobile.wallet.cri.services.data_storage.DataStore;
import uk.gov.di.mobile.wallet.cri.services.data_storage.DataStoreException;
import uk.gov.di.mobile.wallet.cri.services.object_storage.ObjectStoreException;
import uk.gov.di.mobile.wallet.cri.services.signing.SigningException;

import java.security.cert.CertificateException;
import java.time.Instant;
import java.util.UUID;

import static uk.gov.di.mobile.wallet.cri.credential.CredentialType.MOBILE_DRIVING_LICENCE;

public class CredentialService {

    private final DataStore dataStore;
    private final AccessTokenService accessTokenService;
    private final ProofJwtService proofJwtService;
    private final DocumentStoreClient documentStoreClient;
    private final CredentialHandlerFactory credentialHandlerFactory;
    private final CredentialExpiryCalculator credentialExpiryCalculator;
    private final StatusListRequestTokenBuilder statusListRequestTokenBuilder;

    private static final Logger LOGGER = LoggerFactory.getLogger(CredentialService.class);

    public CredentialService(
            DataStore dataStore,
            AccessTokenService accessTokenService,
            ProofJwtService proofJwtService,
            DocumentStoreClient documentStoreClient,
            CredentialHandlerFactory credentialHandlerFactory,
            CredentialExpiryCalculator credentialExpiryCalculator,
            StatusListRequestTokenBuilder statusListRequestTokenBuilder) {
        this.dataStore = dataStore;
        this.accessTokenService = accessTokenService;
        this.proofJwtService = proofJwtService;
        this.documentStoreClient = documentStoreClient;
        this.credentialHandlerFactory = credentialHandlerFactory;
        this.credentialExpiryCalculator = credentialExpiryCalculator;
        this.statusListRequestTokenBuilder = statusListRequestTokenBuilder;
    }

    public CredentialResponse getCredential(SignedJWT accessToken, SignedJWT proofJwt)
            throws AccessTokenValidationException,
                    NonceValidationException,
                    ProofJwtValidationException,
                    CredentialOfferException,
                    CredentialServiceException {
        try {
            AccessTokenService.AccessTokenData accessTokenData =
                    accessTokenService.verifyAccessToken(accessToken);

            ProofJwtService.ProofJwtData proofJwtData = proofJwtService.verifyProofJwt(proofJwt);

            if (!proofJwtData.nonce().equals(accessTokenData.nonce())) {
                throw new NonceValidationException(
                        "Access token c_nonce claim does not match Proof JWT nonce claim");
            }

            String credentialOfferId = accessTokenData.credentialIdentifier();
            CachedCredentialOffer credentialOffer = dataStore.getCredentialOffer(credentialOfferId);
            if (!isValidCredentialOffer(credentialOffer, credentialOfferId)) {
                throw new CredentialOfferException("Credential offer validation failed");
            }

            if (!credentialOffer.getWalletSubjectId().equals(accessTokenData.walletSubjectId())) {
                throw new AccessTokenValidationException(
                        "Access token sub claim does not match cached walletSubjectId");
            }

            String documentId = credentialOffer.getDocumentId();
            Document document = documentStoreClient.getDocument(documentId);
            String notificationId = UUID.randomUUID().toString();
            String vcType = document.getVcType();

            LOGGER.info(
                    "{} retrieved - credentialOfferId: {}, documentId: {}",
                    vcType,
                    credentialOfferId,
                    documentId);

            // Delete credential offer after redeeming it to prevent replay
            dataStore.deleteCredentialOffer(credentialOfferId);

            long expiry = credentialExpiryCalculator.calculateExpiry(document);

            CredentialType credentialType = CredentialType.fromType(vcType);
            if (credentialType == MOBILE_DRIVING_LICENCE) {
                JWTClaimsSet claims = statusListRequestTokenBuilder.buildIssueClaims(expiry);
                String issueToken = statusListRequestTokenBuilder.buildToken(claims);
                System.out.println(issueToken);
            }

            CredentialHandler handler = credentialHandlerFactory.createHandler(vcType);
            String credential = handler.buildCredential(document, proofJwtData);

            dataStore.saveStoredCredential(
                    new StoredCredential(
                            credentialOffer.getCredentialIdentifier(),
                            notificationId,
                            credentialOffer.getWalletSubjectId(),
                            expiry));

            return new CredentialResponse(credential, notificationId);
        } catch (DataStoreException
                | SigningException
                | MDLException
                | ObjectStoreException
                | CertificateException
                | DocumentStoreException exception) {
            throw new CredentialServiceException(
                    "Failed to issue credential due to an internal error", exception);
        }
    }

    private boolean isValidCredentialOffer(
            CachedCredentialOffer credentialOffer, String credentialOfferId) {
        long now = Instant.now().getEpochSecond();

        if (credentialOffer == null) {
            getLogger().error("Credential offer {} was not found", credentialOfferId);
            return false;
        } else if (now > credentialOffer.getTimeToLive()) {
            getLogger().error("Credential offer {} is expired", credentialOfferId);
            return false;
        }
        return true;
    }

    protected Logger getLogger() {
        return LOGGER;
    }
}
