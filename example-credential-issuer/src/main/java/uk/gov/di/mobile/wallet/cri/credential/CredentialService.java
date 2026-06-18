package uk.gov.di.mobile.wallet.cri.credential;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.google.common.io.Resources;
import com.nimbusds.jwt.SignedJWT;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import uk.gov.di.mobile.wallet.cri.credential.mdoc.MdocException;
import uk.gov.di.mobile.wallet.cri.credential.proof.ProofJwtService;
import uk.gov.di.mobile.wallet.cri.credential.proof.ProofJwtValidationException;
import uk.gov.di.mobile.wallet.cri.credential.util.CredentialExpiryCalculator;
import uk.gov.di.mobile.wallet.cri.credential_offer.CachedCredentialOffer;
import uk.gov.di.mobile.wallet.cri.services.authentication.AccessTokenService;
import uk.gov.di.mobile.wallet.cri.services.authentication.AccessTokenValidationException;
import uk.gov.di.mobile.wallet.cri.services.data_storage.DataStore;
import uk.gov.di.mobile.wallet.cri.services.data_storage.DataStoreException;
import uk.gov.di.mobile.wallet.cri.services.object_storage.ObjectStoreException;
import uk.gov.di.mobile.wallet.cri.services.signing.SigningException;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.security.cert.CertificateException;
import java.time.Instant;
import java.util.Optional;
import java.util.UUID;

import static uk.gov.di.mobile.wallet.cri.credential.CredentialType.MOBILE_DRIVING_LICENCE;
import static uk.gov.di.mobile.wallet.cri.credential.CredentialType.SIMPLE_MDOC;

public class CredentialService {

    private final DataStore dataStore;
    private final AccessTokenService accessTokenService;
    private final ProofJwtService proofJwtService;
    private final DocumentStoreClient documentStoreClient;
    private final CredentialHandlerFactory credentialHandlerFactory;
    private final CredentialExpiryCalculator credentialExpiryCalculator;
    private final StatusListClient statusListClient;

    private static final Logger LOGGER = LoggerFactory.getLogger(CredentialService.class);

    public CredentialService(
            DataStore dataStore,
            AccessTokenService accessTokenService,
            ProofJwtService proofJwtService,
            DocumentStoreClient documentStoreClient,
            CredentialHandlerFactory credentialHandlerFactory,
            CredentialExpiryCalculator credentialExpiryCalculator,
            StatusListClient statusListClient) {
        this.dataStore = dataStore;
        this.accessTokenService = accessTokenService;
        this.proofJwtService = proofJwtService;
        this.documentStoreClient = documentStoreClient;
        this.credentialHandlerFactory = credentialHandlerFactory;
        this.credentialExpiryCalculator = credentialExpiryCalculator;
        this.statusListClient = statusListClient;
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

            String credentialIdentifier = accessTokenData.credentialIdentifier();
            boolean isRefreshCredential = credentialIdentifier == null;

            DocumentStoreRecord document;
            String walletSubjectId = null;

            if (isRefreshCredential) {
                String credentialConfigurationId = accessTokenData.credentialConfigurationId();
                document = loadRefreshCredential(credentialConfigurationId);
            } else {
                CachedCredentialOffer credentialOffer =
                        dataStore.getCredentialOffer(credentialIdentifier);
                if (!isValidCredentialOffer(credentialOffer, credentialIdentifier)) {
                    throw new CredentialOfferException("Credential offer validation failed");
                }

                if (!credentialOffer
                        .getWalletSubjectId()
                        .equals(accessTokenData.walletSubjectId())) {
                    throw new AccessTokenValidationException(
                            "Access token sub claim does not match cached walletSubjectId");
                }

                String itemId = credentialOffer.getItemId();
                walletSubjectId = credentialOffer.getWalletSubjectId();
                document = documentStoreClient.getDocument(itemId);

                // Delete credential offer after redeeming it to prevent replay
                dataStore.deleteCredentialOffer(credentialIdentifier);
            }

            String notificationId = UUID.randomUUID().toString();
            String vcType = document.getVcType();
            CredentialType credentialType = CredentialType.fromType(vcType);
            long expiry = credentialExpiryCalculator.calculateExpiry(document);

            Optional<StatusListClient.StatusListInformation> statusListInformation =
                    Optional.empty();
            if (credentialType == MOBILE_DRIVING_LICENCE || credentialType == SIMPLE_MDOC) {
                statusListInformation = Optional.of(statusListClient.getIndex(expiry));
            }

            CredentialHandler handler = credentialHandlerFactory.createHandler(vcType);
            String credential =
                    handler.buildCredential(document, proofJwtData, statusListInformation);

            StoredCredential storedCredential =
                    StoredCredential.builder()
                            .credentialIdentifier(credentialIdentifier)
                            .notificationId(notificationId)
                            .walletSubjectId(walletSubjectId)
                            .timeToLive(expiry)
                            .statusList(statusListInformation)
                            .documentId(document.getDocumentId())
                            .build();

            dataStore.saveStoredCredential(storedCredential);

            return new CredentialResponse(credential, notificationId);
        } catch (DataStoreException
                | SigningException
                | MdocException
                | ObjectStoreException
                | CertificateException
                | DocumentStoreException
                | StatusListClientException
                | IllegalArgumentException
                | IOException exception) {
            throw new CredentialServiceException(
                    "Failed to issue credential due to an internal error", exception);
        }
    }

    private DocumentStoreRecord loadRefreshCredential(String credentialConfigurationId)
            throws IOException {
        String json =
                Resources.toString(
                        Resources.getResource(
                                "refresh_credentials/" + credentialConfigurationId + ".json"),
                        StandardCharsets.UTF_8);
        json = json.replace("{{UNIQUE_DOCUMENT_NUMBER}}", UUID.randomUUID() + "RFH");
        return new ObjectMapper().readValue(json, DocumentStoreRecord.class);
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
