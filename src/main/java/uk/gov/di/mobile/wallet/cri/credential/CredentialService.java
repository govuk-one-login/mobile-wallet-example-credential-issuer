package uk.gov.di.mobile.wallet.cri.credential;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;
import com.nimbusds.jwt.SignedJWT;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import uk.gov.di.mobile.wallet.cri.credential.basic_check_credential.BasicCheckCredentialSubject;
import uk.gov.di.mobile.wallet.cri.credential.digital_veteran_card.VeteranCardCredentialSubject;
import uk.gov.di.mobile.wallet.cri.credential.mobile_driving_licence.DrivingLicenceDocument;
import uk.gov.di.mobile.wallet.cri.credential.mobile_driving_licence.MobileDrivingLicenceService;
import uk.gov.di.mobile.wallet.cri.credential.mobile_driving_licence.cbor.MDLException;
import uk.gov.di.mobile.wallet.cri.credential.social_security_credential.SocialSecurityCredentialSubject;
import uk.gov.di.mobile.wallet.cri.models.CachedCredentialOffer;
import uk.gov.di.mobile.wallet.cri.services.authentication.AccessTokenService;
import uk.gov.di.mobile.wallet.cri.services.authentication.AccessTokenValidationException;
import uk.gov.di.mobile.wallet.cri.services.data_storage.DataStore;
import uk.gov.di.mobile.wallet.cri.services.data_storage.DataStoreException;
import uk.gov.di.mobile.wallet.cri.services.signing.SigningException;

import java.security.NoSuchAlgorithmException;
import java.time.Instant;
import java.util.Objects;

import static uk.gov.di.mobile.wallet.cri.credential.CredentialType.BASIC_CHECK_CREDENTIAL;
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

    private final ObjectMapper mapper = new ObjectMapper().registerModule(new JavaTimeModule());
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
                    AccessTokenValidationException,
                    CredentialServiceException,
                    CredentialOfferException,
                    DocumentStoreException {
        AccessTokenService.AccessTokenData accessTokenData =
                accessTokenService.verifyAccessToken(accessToken);

        ProofJwtService.ProofJwtData proofJwtData = proofJwtService.verifyProofJwt(proofJwt);

        if (!proofJwtData.nonce().equals(accessTokenData.nonce())) {
            throw new ProofJwtValidationException(
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
                credential = getSocialSecurityCredential(document, sub);
            } else if (Objects.equals(vcType, BASIC_CHECK_CREDENTIAL.getType())) {
                credential = getBasicCheckCredential(document, sub);
            } else if (Objects.equals(vcType, DIGITAL_VETERAN_CARD.getType())) {
                credential = getDigitalVeteranCard(document, sub);
            } else if (Objects.equals(vcType, MOBILE_DRIVING_LICENCE.getType())) {
                credential = getMobileDrivingLicence(document);
            } else {
                throw new CredentialServiceException(
                        String.format("Invalid verifiable credential type %s", vcType));
            }
            return new CredentialResponse(credential, credentialOffer.getNotificationId());
        } catch (NoSuchAlgorithmException | SigningException | MDLException exception) {
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
    private String getSocialSecurityCredential(Document document, String sub)
            throws SigningException, NoSuchAlgorithmException {
        SocialSecurityCredentialSubject socialSecurityCredentialSubject =
                CredentialSubjectMapper.buildSocialSecurityCredentialSubject(document, sub);
        return ((CredentialBuilder<SocialSecurityCredentialSubject>) credentialBuilder)
                .buildCredential(socialSecurityCredentialSubject, SOCIAL_SECURITY_CREDENTIAL, null);
    }

    @SuppressWarnings("unchecked")
    private String getBasicCheckCredential(Document document, String sub)
            throws SigningException, NoSuchAlgorithmException {
        BasicCheckCredentialSubject basicCheckCredentialSubject =
                CredentialSubjectMapper.buildBasicCheckCredentialSubject(document, sub);

        return ((CredentialBuilder<BasicCheckCredentialSubject>) credentialBuilder)
                .buildCredential(
                        basicCheckCredentialSubject,
                        BASIC_CHECK_CREDENTIAL,
                        basicCheckCredentialSubject.getExpirationDate());
    }

    @SuppressWarnings("unchecked")
    private String getDigitalVeteranCard(Document document, String sub)
            throws SigningException, NoSuchAlgorithmException {
        VeteranCardCredentialSubject veteranCardCredentialSubject =
                CredentialSubjectMapper.buildVeteranCardCredentialSubject(document, sub);
        return ((CredentialBuilder<VeteranCardCredentialSubject>) credentialBuilder)
                .buildCredential(
                        veteranCardCredentialSubject,
                        DIGITAL_VETERAN_CARD,
                        veteranCardCredentialSubject.getVeteranCard().get(0).getExpiryDate());
    }

    private String getMobileDrivingLicence(Document document) throws MDLException {
        final DrivingLicenceDocument drivingLicenceDocument =
                mapper.convertValue(document.getData(), DrivingLicenceDocument.class);
        return mobileDrivingLicenceService.createMobileDrivingLicence(drivingLicenceDocument);
    }

    protected Logger getLogger() {
        return LOGGER;
    }
}
