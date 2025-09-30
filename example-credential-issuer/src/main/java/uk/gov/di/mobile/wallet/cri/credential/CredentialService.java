package uk.gov.di.mobile.wallet.cri.credential;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.datatype.jdk8.Jdk8Module;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;
import com.nimbusds.jwt.SignedJWT;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import uk.gov.di.mobile.wallet.cri.credential.mobile_driving_licence.DrivingLicenceDocument;
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

public class CredentialService {

    private final DataStore dataStore;
    private final AccessTokenService accessTokenService;
    private final ProofJwtService proofJwtService;
    private final DocumentStoreClient documentStoreClient;
    private final CredentialHandlerFactory credentialHandlerFactory;
    private final CredentialExpiryCalculator credentialExpiryCalculator;

    private static final Logger LOGGER = LoggerFactory.getLogger(CredentialService.class);
    private static final ObjectMapper mapper =
            new ObjectMapper()
                    .registerModule(new JavaTimeModule())
                    .registerModule(new Jdk8Module());

    public CredentialService(
            DataStore dataStore,
            AccessTokenService accessTokenService,
            ProofJwtService proofJwtService,
            DocumentStoreClient documentStoreClient,
            CredentialHandlerFactory credentialHandlerFactory,
            CredentialExpiryCalculator credentialExpiryCalculator) {
        this.dataStore = dataStore;
        this.accessTokenService = accessTokenService;
        this.proofJwtService = proofJwtService;
        this.documentStoreClient = documentStoreClient;
        this.credentialHandlerFactory = credentialHandlerFactory;
        this.credentialExpiryCalculator = credentialExpiryCalculator;
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

            LOGGER.info(
                    "{} retrieved - credentialOfferId: {}, documentId: {}",
                    document.getVcType(),
                    credentialOfferId,
                    documentId);

            // Delete credential offer after redeeming it to prevent replay
            dataStore.deleteCredentialOffer(credentialOfferId);

            CredentialHandler handler =
                    credentialHandlerFactory.createHandler(document.getVcType());
            String credential = handler.buildCredential(document, proofJwtData);

            long expiry = credentialExpiryCalculator.calculateExpiry(document);

            var storedCredential =
                    StoredCredential.builder()
                            .credentialIdentifier(credentialOffer.getCredentialIdentifier())
                            .notificationId(notificationId)
                            .walletSubjectId(credentialOffer.getWalletSubjectId())
                            .timeToLive(expiry)
                            .documentPrimaryIdentifier(getDocumentPrimaryIdentifier(document));

            dataStore.saveStoredCredential(storedCredential.build());

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

    private String getDocumentPrimaryIdentifier(Document document) {
        if (document.getVcType().equals("org.iso.18013.5.1.mDL")) {
            DrivingLicenceDocument drivingLicenceDocument =
                    mapper.convertValue(document.getData(), DrivingLicenceDocument.class);
            return drivingLicenceDocument.getDocumentNumber();
        }

        /* For veterans card, national insurance, and DBS credentials, this value
        should be set to the service number, NINo, and DBS certificate ID respectively.

        At the moment documentId is a UUID for the document in the document
        database table rather than the service number, NINo or DBS certificate ID.
        We are accepting the existing documentId as the primaryIdentifier for non-mDL
        credentials because it is not currently being read. Future work will set
        documentId to the correct value within the Document Builder.
        See https://govukverify.atlassian.net/browse/DCMAW-15868 */

        return document.getDocumentId();
    }
}
