package uk.gov.di.mobile.wallet.cri.credential;

import com.nimbusds.jwt.SignedJWT;
import jakarta.ws.rs.client.Client;
import jakarta.ws.rs.core.MediaType;
import jakarta.ws.rs.core.Response;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
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
import java.util.List;

public class CredentialService {

    private final ConfigurationService configurationService;
    private final DataStore dataStore;
    private final AccessTokenService accessTokenService;
    private final ProofJwtService proofJwtService;
    private final Client httpClient;
    private final CredentialBuilder credentialBuilder;
    private static final Logger LOGGER = LoggerFactory.getLogger(CredentialService.class);

    public CredentialService(
            ConfigurationService configurationService,
            DataStore dataStore,
            AccessTokenService accessTokenService,
            ProofJwtService proofJwtService,
            Client httpClient,
            CredentialBuilder credentialBuilder) {
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
                    CredentialServiceException {

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
        LOGGER.info("Credential offer retrieved for credentialOfferId {}", credentialOfferId);

        if (credentialOffer == null) {
            throw new AccessTokenValidationException(
                    "Null response returned when fetching credential offer");
        }

        if (!credentialOffer.getWalletSubjectId().equals(accessTokenCustomClaims.sub())) {
            throw new AccessTokenValidationException(
                    "Access token sub claim does not match cached walletSubjectId");
        }

        String documentId = credentialOffer.getDocumentId();
        Object documentDetails = getDocumentDetails(documentId);
        LOGGER.info(
                "Document details retrieved for credentialOfferId {} and documentId {}",
                credentialOfferId,
                documentId);

        dataStore.deleteCredentialOffer(credentialOfferId);

        return credentialBuilder.buildCredential(proofJwtClaims.kid, documentDetails);
    }

    private static AccessTokenClaims getAccessTokenClaims(SignedJWT accessToken)
            throws AccessTokenValidationException {
        try {
            List<Object> credentialIdentifiers =
                    accessToken.getJWTClaimsSet().getListClaim("credential_identifiers");
            if (credentialIdentifiers.isEmpty()) {
                throw new InvalidAttributeValueException("credential_identifiers is invalid");
            }
            String credentialIdentifier = (String) credentialIdentifiers.get(0);
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

    private Object getDocumentDetails(String documentId)
            throws URISyntaxException, CredentialServiceException {
        String credentialStoreUrl = configurationService.getCredentialStoreUrl();
        String credentialStoreDocumentPath = configurationService.getCredentialStoreDocumentPath();
        URI uri = new URI(credentialStoreUrl + credentialStoreDocumentPath + documentId);

        Response response = httpClient.target(uri).request(MediaType.APPLICATION_JSON).get();

        if (response.getStatus() != Response.Status.OK.getStatusCode()) {
            throw new CredentialServiceException(
                    String.format(
                            "Request to fetch document details for documentId %s failed with status code %s",
                            documentId, response.getStatus()));
        }
        return response.readEntity(Object.class);
    }
}
