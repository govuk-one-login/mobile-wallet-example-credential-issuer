package uk.gov.di.mobile.wallet.cri.credential;

import com.nimbusds.jwt.SignedJWT;
import jakarta.ws.rs.client.Client;
import jakarta.ws.rs.core.MediaType;
import jakarta.ws.rs.core.Response;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import uk.gov.di.mobile.wallet.cri.credential.basic_check_credential.BasicCheckCredentialSubject;
import uk.gov.di.mobile.wallet.cri.credential.digital_veteran_card.VeteranCardCredentialSubject;
import uk.gov.di.mobile.wallet.cri.credential.social_security_credential.SocialSecurityCredentialSubject;
import uk.gov.di.mobile.wallet.cri.models.CredentialOfferCacheItem;
import uk.gov.di.mobile.wallet.cri.services.ConfigurationService;
import uk.gov.di.mobile.wallet.cri.services.authentication.AccessTokenService;
import uk.gov.di.mobile.wallet.cri.services.authentication.AccessTokenValidationException;
import uk.gov.di.mobile.wallet.cri.services.data_storage.DataStore;
import uk.gov.di.mobile.wallet.cri.services.data_storage.DataStoreException;
import uk.gov.di.mobile.wallet.cri.services.signing.SigningException;

import java.net.URI;
import java.net.URISyntaxException;
import java.security.NoSuchAlgorithmException;
import java.time.Instant;
import java.util.Objects;

import static uk.gov.di.mobile.wallet.cri.credential.CredentialType.*;

public class CredentialService {

    private final ConfigurationService configurationService;
    private final DataStore dataStore;
    private final AccessTokenService accessTokenService;
    private final ProofJwtService proofJwtService;
    private final Client httpClient;
    private final CredentialBuilder<? extends CredentialSubject> credentialBuilder;
    private static final Logger LOGGER = LoggerFactory.getLogger(CredentialService.class);

    public CredentialService(
            ConfigurationService configurationService,
            DataStore dataStore,
            AccessTokenService accessTokenService,
            ProofJwtService proofJwtService,
            Client httpClient,
            CredentialBuilder<?> credentialBuilder) {
        this.configurationService = configurationService;
        this.dataStore = dataStore;
        this.accessTokenService = accessTokenService;
        this.proofJwtService = proofJwtService;
        this.httpClient = httpClient;
        this.credentialBuilder = credentialBuilder;
    }

    public CredentialResponse getCredential(SignedJWT accessToken, SignedJWT proofJwt)
            throws DataStoreException,
                    ProofJwtValidationException,
                    SigningException,
                    AccessTokenValidationException,
                    NoSuchAlgorithmException,
                    URISyntaxException,
                    CredentialServiceException,
                    CredentialOfferException {

        AccessTokenService.AccessTokenData accessTokenData =
                accessTokenService.verifyAccessToken(accessToken);
        String credentialOfferId = accessTokenData.credentialIdentifier();
        LOGGER.info("Access token for credential offer {} verified", credentialOfferId);

        ProofJwtService.ProofJwtData proofJwtData = proofJwtService.verifyProofJwt(proofJwt);
        LOGGER.info("Proof JWT for credential offer {} verified", credentialOfferId);

        if (!proofJwtData.nonce().equals(accessTokenData.nonce())) {
            throw new ProofJwtValidationException(
                    "Access token c_nonce claim does not match Proof JWT nonce claim");
        }

        CredentialOfferCacheItem credentialOffer = dataStore.getCredentialOffer(credentialOfferId);

        if (!isValidCredentialOffer(credentialOffer, credentialOfferId)) {
            throw new CredentialOfferException("Credential offer validation failed");
        }

        if (!credentialOffer.getWalletSubjectId().equals(accessTokenData.walletSubjectId())) {
            throw new AccessTokenValidationException(
                    "Access token sub claim does not match cached walletSubjectId");
        }

        String documentId = credentialOffer.getDocumentId();
        DocumentAPIResponse documentAPIResponse = getDocumentData(documentId);

        LOGGER.info(
                "{} retrieved for credentialOfferId {} and documentId {}",
                documentAPIResponse.getVcType(),
                credentialOfferId,
                documentId);

        credentialOffer.setRedeemed(true); // mark credential offer as redeemed to prevent replay
        dataStore.updateCredentialOffer(credentialOffer);

        String sub = proofJwtData.didKey();
        String vcType = documentAPIResponse.getVcType();

        String notificationId = credentialOffer.getNotificationId();
        SignedJWT credential;

        if (Objects.equals(vcType, SOCIAL_SECURITY_CREDENTIAL.getType())) {
            credential = getSocialSecurityCredential(documentAPIResponse, sub);

        } else if (Objects.equals(vcType, BASIC_CHECK_CREDENTIAL.getType())) {
            credential = getBasicCheckCredential(documentAPIResponse, sub);

        } else if (Objects.equals(vcType, DIGITAL_VETERAN_CARD.getType())) {
            credential = getDigitalVeteranCard(documentAPIResponse, sub);

        } else {
            throw new CredentialServiceException(
                    String.format("Invalid verifiable credential type %s", vcType));
        }
        return new CredentialResponse(credential.serialize(), notificationId);
    }

    private boolean isValidCredentialOffer(
            CredentialOfferCacheItem credentialOffer, String credentialOfferId) {
        if (credentialOffer == null) {
            getLogger().error("Credential offer {} was not found", credentialOfferId);
            return false;
        }

        if (hasOfferBeenRedeemed(credentialOffer)) {
            getLogger().error("Credential offer {} has already been redeemed", credentialOfferId);
            return false;
        }

        if (isOfferExpired(credentialOffer)) {
            getLogger().error("Credential offer {} is expired", credentialOfferId);
            return false;
        }
        return true;
    }

    private static boolean isOfferExpired(CredentialOfferCacheItem credentialOffer) {
        long now = Instant.now().getEpochSecond();
        return now > credentialOffer.getExpiry();
    }

    private static boolean hasOfferBeenRedeemed(CredentialOfferCacheItem credentialOffer) {
        return credentialOffer.getRedeemed();
    }

    private DocumentAPIResponse getDocumentData(String documentId)
            throws URISyntaxException, CredentialServiceException {
        String credentialStoreUrl = configurationService.getCredentialStoreUrl();
        String documentEndpoint = configurationService.getDocumentEndpoint();
        URI uri = new URI(credentialStoreUrl + documentEndpoint + documentId);

        Response response = httpClient.target(uri).request(MediaType.APPLICATION_JSON).get();

        if (response.getStatus() != Response.Status.OK.getStatusCode()) {
            throw new CredentialServiceException(
                    String.format(
                            "Request to fetch document %s failed with status code %s",
                            documentId, response.getStatus()));
        }
        return response.readEntity(DocumentAPIResponse.class);
    }

    private SignedJWT getSocialSecurityCredential(DocumentAPIResponse documentAPIResponse, String sub)
            throws SigningException, NoSuchAlgorithmException {
        SocialSecurityCredentialSubject socialSecurityCredentialSubject =
                CredentialSubjectMapper.buildSocialSecurityCredentialSubject(documentAPIResponse, sub);
        return ((CredentialBuilder<SocialSecurityCredentialSubject>) credentialBuilder)
                .buildCredential(socialSecurityCredentialSubject, SOCIAL_SECURITY_CREDENTIAL, null);
    }

    private SignedJWT getBasicCheckCredential(DocumentAPIResponse documentAPIResponse, String sub)
            throws SigningException, NoSuchAlgorithmException {
        BasicCheckCredentialSubject basicCheckCredentialSubject =
                CredentialSubjectMapper.buildBasicCheckCredentialSubject(documentAPIResponse, sub);

        return ((CredentialBuilder<BasicCheckCredentialSubject>) credentialBuilder)
                .buildCredential(
                        basicCheckCredentialSubject,
                        BASIC_CHECK_CREDENTIAL,
                        basicCheckCredentialSubject.getExpirationDate());
    }

    private SignedJWT getDigitalVeteranCard(DocumentAPIResponse documentAPIResponse, String sub)
            throws SigningException, NoSuchAlgorithmException {
        VeteranCardCredentialSubject veteranCardCredentialSubject =
                CredentialSubjectMapper.buildVeteranCardCredentialSubject(documentAPIResponse, sub);
        return ((CredentialBuilder<VeteranCardCredentialSubject>) credentialBuilder)
                .buildCredential(
                        veteranCardCredentialSubject,
                        DIGITAL_VETERAN_CARD,
                        veteranCardCredentialSubject.getVeteranCard().get(0).getExpiryDate());
    }

    protected Logger getLogger() {
        return LOGGER;
    }
}
