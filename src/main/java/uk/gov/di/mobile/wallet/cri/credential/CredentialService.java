package uk.gov.di.mobile.wallet.cri.credential;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.datatype.jdk8.Jdk8Module;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;
import com.nimbusds.jwt.SignedJWT;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import uk.gov.di.mobile.wallet.cri.credential.basic_check_credential.BasicCheckCredentialSubject;
import uk.gov.di.mobile.wallet.cri.credential.basic_check_credential.BasicCheckDocument;
import uk.gov.di.mobile.wallet.cri.credential.digital_veteran_card.VeteranCardCredentialSubject;
import uk.gov.di.mobile.wallet.cri.credential.digital_veteran_card.VeteranCardDocument;
import uk.gov.di.mobile.wallet.cri.credential.mobile_driving_licence.DrivingLicenceDocument;
import uk.gov.di.mobile.wallet.cri.credential.mobile_driving_licence.MDLException;
import uk.gov.di.mobile.wallet.cri.credential.mobile_driving_licence.MobileDrivingLicenceService;
import uk.gov.di.mobile.wallet.cri.credential.social_security_credential.SocialSecurityCredentialSubject;
import uk.gov.di.mobile.wallet.cri.credential.social_security_credential.SocialSecurityDocument;
import uk.gov.di.mobile.wallet.cri.models.CachedCredentialOffer;
import uk.gov.di.mobile.wallet.cri.models.StoredCredential;
import uk.gov.di.mobile.wallet.cri.services.authentication.AccessTokenService;
import uk.gov.di.mobile.wallet.cri.services.authentication.AccessTokenValidationException;
import uk.gov.di.mobile.wallet.cri.services.data_storage.DataStore;
import uk.gov.di.mobile.wallet.cri.services.data_storage.DataStoreException;
import uk.gov.di.mobile.wallet.cri.services.object_storage.ObjectStoreException;
import uk.gov.di.mobile.wallet.cri.services.signing.SigningException;

import java.security.NoSuchAlgorithmException;
import java.security.cert.CertificateException;
import java.text.ParseException;
import java.time.*;
import java.util.Date;
import java.util.Objects;

import static uk.gov.di.mobile.wallet.cri.credential.CredentialType.BASIC_DISCLOSURE_CREDENTIAL;
import static uk.gov.di.mobile.wallet.cri.credential.CredentialType.DIGITAL_VETERAN_CARD;
import static uk.gov.di.mobile.wallet.cri.credential.CredentialType.MOBILE_DRIVING_LICENCE;
import static uk.gov.di.mobile.wallet.cri.credential.CredentialType.SOCIAL_SECURITY_CREDENTIAL;

public class CredentialService {

    private final DataStore dataStore;
    private final AccessTokenService accessTokenService;
    private final ProofJwtService proofJwtService;
    private final DocumentStoreClient documentStoreClient;
    private final CredentialBuilder<? extends CredentialSubject> credentialBuilder;
    private final MobileDrivingLicenceService mobileDrivingLicenceService;

    private final ObjectMapper mapper =
            new ObjectMapper()
                    .registerModule(new JavaTimeModule())
                    .registerModule(new Jdk8Module());
    private static final Logger LOGGER = LoggerFactory.getLogger(CredentialService.class);

    public CredentialService(
            DataStore dataStore,
            AccessTokenService accessTokenService,
            ProofJwtService proofJwtService,
            DocumentStoreClient documentStoreClient,
            CredentialBuilder<?> credentialBuilder,
            MobileDrivingLicenceService mobileDrivingLicenceService) {
        this.dataStore = dataStore;
        this.accessTokenService = accessTokenService;
        this.proofJwtService = proofJwtService;
        this.documentStoreClient = documentStoreClient;
        this.credentialBuilder = credentialBuilder;
        this.mobileDrivingLicenceService = mobileDrivingLicenceService;
    }

    public CredentialResponse getCredential(SignedJWT accessToken, SignedJWT proofJwt)
            throws DataStoreException,
                    ProofJwtValidationException,
                    NonceValidationException,
                    AccessTokenValidationException,
                    CredentialServiceException,
                    CredentialOfferException,
                    DocumentStoreException {
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

        LOGGER.info(
                "{} retrieved - credentialOfferId: {}, documentId: {}",
                document.getVcType(),
                credentialOfferId,
                documentId);

        credentialOffer.setRedeemed(true); // Mark credential offer as redeemed to prevent replay
        dataStore.updateCredentialOffer(credentialOffer);

        String sub = proofJwtData.didKey();
        String vcType = document.getVcType();
        String credential;
        try {
            if (Objects.equals(vcType, SOCIAL_SECURITY_CREDENTIAL.getType())) {
                credential =
                        getSocialSecurityCredential(
                                mapper.convertValue(
                                        document.getData(), SocialSecurityDocument.class),
                                sub);
            } else if (Objects.equals(vcType, BASIC_DISCLOSURE_CREDENTIAL.getType())) {
                credential =
                        getBasicCheckCredential(
                                mapper.convertValue(document.getData(), BasicCheckDocument.class),
                                sub);
            } else if (Objects.equals(vcType, DIGITAL_VETERAN_CARD.getType())) {
                credential =
                        getDigitalVeteranCard(
                                mapper.convertValue(document.getData(), VeteranCardDocument.class),
                                sub);
            } else if (Objects.equals(vcType, MOBILE_DRIVING_LICENCE.getType())) {
                credential =
                        getMobileDrivingLicence(
                                mapper.convertValue(
                                        document.getData(), DrivingLicenceDocument.class));
            } else {
                throw new CredentialServiceException(
                        String.format("Invalid verifiable credential type %s", vcType));
            }

            CredentialResponse credentialResponse =
                    new CredentialResponse(credential, credentialOffer.getNotificationId());

            dataStore.saveStoredCredential(storedCredential(credential, credentialOffer.getCredentialIdentifier(), credentialResponse.getNotificationId()));
            LOGGER.info("Credential was saved successfully");

            return credentialResponse;

        } catch (NoSuchAlgorithmException | ParseException
                | DataStoreException
                | SigningException
                | MDLException
                | ObjectStoreException
                | CertificateException exception) {
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
        } else if (Boolean.TRUE.equals(credentialOffer.getRedeemed())) {
            getLogger().error("Credential offer {} has already been redeemed", credentialOfferId);
            return false;
        } else if (now > credentialOffer.getExpiry()) {
            getLogger().error("Credential offer {} is expired", credentialOfferId);
            return false;
        }
        return true;
    }

    @SuppressWarnings("unchecked")
    private String getSocialSecurityCredential(
            SocialSecurityDocument socialSecurityDocument, String sub) throws SigningException {
        SocialSecurityCredentialSubject socialSecurityCredentialSubject =
                CredentialSubjectMapper.buildSocialSecurityCredentialSubject(
                        socialSecurityDocument, sub);
        return ((CredentialBuilder<SocialSecurityCredentialSubject>) credentialBuilder)
                        .buildCredential(
                                socialSecurityCredentialSubject,
                                SOCIAL_SECURITY_CREDENTIAL,
                                socialSecurityDocument.getCredentialTtlMinutes());
    }

    @SuppressWarnings("unchecked")
    private String getBasicCheckCredential(
            BasicCheckDocument basicCheckDocument, String sub)
            throws SigningException, NoSuchAlgorithmException {
        BasicCheckCredentialSubject basicCheckCredentialSubject =
                CredentialSubjectMapper.buildBasicCheckCredentialSubject(basicCheckDocument, sub);

        return ((CredentialBuilder<BasicCheckCredentialSubject>) credentialBuilder)
                        .buildCredential(
                                basicCheckCredentialSubject,
                                BASIC_DISCLOSURE_CREDENTIAL,
                                basicCheckDocument.getCredentialTtlMinutes());
        }

    @SuppressWarnings("unchecked")
    private String getDigitalVeteranCard(
            VeteranCardDocument veteranCardDocument, String sub) throws SigningException {
        VeteranCardCredentialSubject veteranCardCredentialSubject =
                CredentialSubjectMapper.buildVeteranCardCredentialSubject(veteranCardDocument, sub);
        return ((CredentialBuilder<VeteranCardCredentialSubject>) credentialBuilder)
                        .buildCredential(
                                veteranCardCredentialSubject,
                                DIGITAL_VETERAN_CARD,
                                veteranCardDocument.getCredentialTtlMinutes());
        }

    private String getMobileDrivingLicence(DrivingLicenceDocument drivingLicenceDocument)
            throws ObjectStoreException, SigningException, CertificateException {
        return mobileDrivingLicenceService.createMobileDrivingLicence(drivingLicenceDocument);
        }

    protected Logger getLogger() {
        return LOGGER;
    }

    private StoredCredential storedCredential(String credential, String credentialIdentifier, String notificationId) throws ParseException {

        SignedJWT parsedCredential = SignedJWT.parse(credential);
        Date expDate = parsedCredential.getJWTClaimsSet().getIssueTime();
        long timeToLive = expDate.toInstant().getEpochSecond();

        return new StoredCredential(credentialIdentifier, notificationId, timeToLive);
    }
}
