package uk.gov.di.mobile.wallet.cri.credential;

import com.nimbusds.jwt.SignedJWT;
import jakarta.ws.rs.client.Client;
import jakarta.ws.rs.core.MediaType;
import jakarta.ws.rs.core.Response;
import org.jetbrains.annotations.NotNull;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import uk.gov.di.mobile.wallet.cri.credential.basic_check_credential.*;
import uk.gov.di.mobile.wallet.cri.credential.digital_veteran_card.VeteranCardCredentialSubject;
import uk.gov.di.mobile.wallet.cri.credential.digital_veteran_card.VeteranCardCredentialSubjectV1;
import uk.gov.di.mobile.wallet.cri.credential.social_security_credential.*;
import uk.gov.di.mobile.wallet.cri.models.CredentialOfferCacheItem;
import uk.gov.di.mobile.wallet.cri.services.ConfigurationService;
import uk.gov.di.mobile.wallet.cri.services.data_storage.DataStore;
import uk.gov.di.mobile.wallet.cri.services.data_storage.DataStoreException;
import uk.gov.di.mobile.wallet.cri.services.signing.SigningException;

import javax.management.InvalidAttributeValueException;

import java.net.URI;
import java.net.URISyntaxException;
import java.security.NoSuchAlgorithmException;
import java.text.ParseException;
import java.time.Instant;
import java.util.List;
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

    public Credential getCredential(SignedJWT accessToken, SignedJWT proofJwt)
            throws DataStoreException,
                    ProofJwtValidationException,
                    SigningException,
                    AccessTokenValidationException,
                    NoSuchAlgorithmException,
                    URISyntaxException,
                    CredentialServiceException,
                    CredentialOfferNotFoundException {
        accessTokenService.verifyAccessToken(accessToken);
        AccessTokenClaims accessTokenCustomClaims = getAccessTokenClaims(accessToken);
        String credentialOfferId = accessTokenCustomClaims.credentialIdentifier();
        LOGGER.info("Access token for credentialOfferId {} verified", credentialOfferId);

        proofJwtService.verifyProofJwt(proofJwt);
        ProofJwtClaims proofJwtClaims = getProofJwtClaims(proofJwt);
        LOGGER.info("Proof JWT for credentialOfferId {} verified", credentialOfferId);

        if (!proofJwtClaims.nonce().equals(accessTokenCustomClaims.cNonce())) {
            throw new ProofJwtValidationException(
                    "Access token c_nonce claim does not match Proof JWT nonce claim");
        }

        CredentialOfferCacheItem credentialOffer = dataStore.getCredentialOffer(credentialOfferId);

        if (credentialOffer == null) {
            throw new CredentialOfferNotFoundException(
                    String.format(
                            "Credential offer not found for credentialOfferId %s",
                            credentialOfferId));
        }

        if (isExpired(credentialOffer)) {
            throw new CredentialOfferNotFoundException(
                    String.format(
                            "Credential offer for credentialOfferId %s expired at %s",
                            credentialOfferId, credentialOffer.getTimeToLive()));
        }
        LOGGER.info("Credential offer retrieved for credentialOfferId {}", credentialOfferId);

        if (!credentialOffer.getWalletSubjectId().equals(accessTokenCustomClaims.sub())) {
            throw new AccessTokenValidationException(
                    "Access token sub claim does not match cached walletSubjectId");
        }

        String documentId = credentialOffer.getDocumentId();
        Document document = getDocument(documentId);

        LOGGER.info(
                "{} retrieved for credentialOfferId {} and documentId {}",
                document.getVcType(),
                credentialOfferId,
                documentId);

        dataStore.deleteCredentialOffer(
                credentialOfferId); // delete credential offer to prevent replay

        String sub = proofJwtClaims.kid;
        String vcType = document.getVcType();

        if (Objects.equals(vcType, SOCIAL_SECURITY_CREDENTIAL.getType())) {
            return getSocialSecurityCredential(document, sub, vcType);
        } else if (Objects.equals(vcType, BASIC_CHECK_CREDENTIAL.getType())) {
            return getBasicCheckCredential(document, sub, vcType);
        } else if (Objects.equals(vcType, DIGITAL_VETERAN_CARD.getType())) {
            return getDigitalVeteranCard(document, sub, vcType);
        } else {
            throw new CredentialServiceException(
                    String.format("Invalid verifiable credential type %s", vcType));
        }
    }

    private static boolean isExpired(CredentialOfferCacheItem credentialOffer) {
        long now = Instant.now().getEpochSecond();
        return now > credentialOffer.getTimeToLive();
    }

    private static AccessTokenClaims getAccessTokenClaims(SignedJWT accessToken)
            throws AccessTokenValidationException {
        try {
            List<String> credentialIdentifiers =
                    accessToken.getJWTClaimsSet().getStringListClaim("credential_identifiers");
            if (credentialIdentifiers.isEmpty()) {
                throw new InvalidAttributeValueException("Empty credential_identifiers");
            }
            String credentialIdentifier = credentialIdentifiers.get(0);
            String sub = accessToken.getJWTClaimsSet().getStringClaim("sub");
            String cNonce = accessToken.getJWTClaimsSet().getStringClaim("c_nonce");
            return new AccessTokenClaims(credentialIdentifier, sub, cNonce);
        } catch (ParseException | NullPointerException | InvalidAttributeValueException exception) {
            throw new AccessTokenValidationException(
                    String.format(
                            "Error parsing access token custom claims: %s",
                            exception.getMessage()));
        }
    }

    private record AccessTokenClaims(String credentialIdentifier, String sub, String cNonce) {}

    private static ProofJwtClaims getProofJwtClaims(SignedJWT proofJwt)
            throws CredentialServiceException {
        try {
            String nonce = proofJwt.getJWTClaimsSet().getStringClaim("nonce");
            String kid = proofJwt.getHeader().getKeyID();
            return new ProofJwtClaims(nonce, kid);
        } catch (ParseException exception) {
            throw new CredentialServiceException(
                    String.format(
                            "Error parsing RequestBody JWT custom claims: %s",
                            exception.getMessage()));
        }
    }

    private record ProofJwtClaims(String nonce, String kid) {}

    private Document getDocument(String documentId)
            throws URISyntaxException, CredentialServiceException {
        String credentialStoreUrl = configurationService.getCredentialStoreUrl();
        String documentEndpoint = configurationService.getDocumentEndpoint();
        URI uri = new URI(credentialStoreUrl + documentEndpoint + documentId);

        Response response = httpClient.target(uri).request(MediaType.APPLICATION_JSON).get();

        if (response.getStatus() != Response.Status.OK.getStatusCode()) {
            throw new CredentialServiceException(
                    String.format(
                            "Request to fetch document details for documentId %s failed with status code %s",
                            documentId, response.getStatus()));
        }
        return response.readEntity(Document.class);
    }

    private Credential getSocialSecurityCredential(Document document, String sub, String vcType)
            throws SigningException, NoSuchAlgorithmException {
        SocialSecurityCredentialSubject socialSecurityCredentialSubject =
                CredentialSubjectMapper.buildSocialSecurityCredentialSubject(document, sub);
        if (Objects.equals(document.getVcDataModel(), "v1.1")) {
            VCClaim vcClaim =
                    new VCClaim(
                            vcType,
                            getSocialSecurityCredentialSubjectV1(socialSecurityCredentialSubject));
            return credentialBuilder.buildCredential(sub, vcClaim);
        } else {
            return ((CredentialBuilder<SocialSecurityCredentialSubject>) credentialBuilder)
                    .buildCredential(
                            socialSecurityCredentialSubject, SOCIAL_SECURITY_CREDENTIAL, null);
        }
    }

    private Credential getBasicCheckCredential(Document document, String sub, String vcType)
            throws SigningException, NoSuchAlgorithmException {
        BasicCheckCredentialSubject basicCheckCredentialSubject =
                CredentialSubjectMapper.buildBasicCheckCredentialSubject(document, sub);
        if (Objects.equals(document.getVcDataModel(), "v1.1")) {
            VCClaim vcClaim =
                    new VCClaim(
                            vcType, getBasicCheckCredentialSubjectV1(basicCheckCredentialSubject));
            return credentialBuilder.buildCredential(sub, vcClaim);
        } else {
            return ((CredentialBuilder<BasicCheckCredentialSubject>) credentialBuilder)
                    .buildCredential(
                            basicCheckCredentialSubject,
                            BASIC_CHECK_CREDENTIAL,
                            basicCheckCredentialSubject.getExpirationDate());
        }
    }

    private Credential getDigitalVeteranCard(Document document, String sub, String vcType)
            throws SigningException, NoSuchAlgorithmException {
        VeteranCardCredentialSubject veteranCardCredentialSubject =
                CredentialSubjectMapper.buildVeteranCardCredentialSubject(document, sub);
        if (Objects.equals(document.getVcDataModel(), "v1.1")) {
            VCClaim vcClaim =
                    new VCClaim(
                            vcType,
                            getVeteranCardCredentialSubjectV1(veteranCardCredentialSubject));
            return credentialBuilder.buildCredential(sub, vcClaim);
        } else {
            return ((CredentialBuilder<VeteranCardCredentialSubject>) credentialBuilder)
                    .buildCredential(
                            veteranCardCredentialSubject,
                            DIGITAL_VETERAN_CARD,
                            veteranCardCredentialSubject.getVeteranCard().get(0).getExpiryDate());
        }
    }

    // Needed for VC MD v1.1 - to be removed once Wallet switches over to VC MD v2.0
    private static @NotNull SocialSecurityCredentialSubjectV1 getSocialSecurityCredentialSubjectV1(
            SocialSecurityCredentialSubject socialSecurityCredentialSubject) {
        return new SocialSecurityCredentialSubjectV1(
                socialSecurityCredentialSubject.getName(),
                socialSecurityCredentialSubject.getSocialSecurityRecord());
    }

    // Needed for VC MD v1.1 - to be removed once Wallet switches over to VC MD v2.0
    private static @NotNull BasicCheckCredentialSubjectV1 getBasicCheckCredentialSubjectV1(
            BasicCheckCredentialSubject basicCheckCredentialSubject) {
        return new BasicCheckCredentialSubjectV1(
                basicCheckCredentialSubject.getIssuanceDate(),
                basicCheckCredentialSubject.getExpirationDate(),
                basicCheckCredentialSubject.getName(),
                basicCheckCredentialSubject.getBirthDate(),
                basicCheckCredentialSubject.getAddress(),
                basicCheckCredentialSubject.getBasicCheckRecord());
    }

    // Needed for VC MD v1.1 - to be removed once Wallet switches over to VC MD v2.0
    private static @NotNull VeteranCardCredentialSubjectV1 getVeteranCardCredentialSubjectV1(
            VeteranCardCredentialSubject veteranCardCredentialSubject) {
        return new VeteranCardCredentialSubjectV1(
                veteranCardCredentialSubject.getName(),
                veteranCardCredentialSubject.getBirthDate(),
                veteranCardCredentialSubject.getVeteranCard());
    }
}
