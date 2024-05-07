package uk.gov.di.mobile.wallet.cri.credential_spike;

import com.nimbusds.jwt.SignedJWT;
import com.nimbusds.oauth2.sdk.token.BearerAccessToken;
import jakarta.ws.rs.client.Client;
import jakarta.ws.rs.core.MediaType;
import jakarta.ws.rs.core.Response;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import uk.gov.di.mobile.wallet.cri.credential.*;
import uk.gov.di.mobile.wallet.cri.did_key.InvalidDidKeyException;
import uk.gov.di.mobile.wallet.cri.models.CredentialOfferCacheItem;
import uk.gov.di.mobile.wallet.cri.services.ConfigurationService;
import uk.gov.di.mobile.wallet.cri.services.data_storage.DataStore;
import uk.gov.di.mobile.wallet.cri.services.data_storage.DataStoreException;
import uk.gov.di.mobile.wallet.cri.services.signing.SigningException;

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

    public Credential run(
            BearerAccessToken bearerAccessToken, CredentialRequestBody credentialRequestBody)
            throws DataStoreException,
                    ClaimMismatchException,
                    SigningException,
                    AccessTokenValidationException,
                    NoSuchAlgorithmException,
                    InvalidDidKeyException,
                    ProofJwtValidationException {

        SignedJWT accessToken = accessTokenService.verifyAccessToken(bearerAccessToken);

        AccessTokenClaims accessTokenCustomClaims = getAccessTokenClaims(accessToken);

        SignedJWT proofJwt =
                proofJwtService.verifyProofJwt(credentialRequestBody.getProof().getJwt());
        ProofJwtClaims proofJwtClaims = getProofJwtClaims(proofJwt);

        if (!proofJwtClaims.nonce().equals(accessTokenCustomClaims.cNonce())) {
            throw new ClaimMismatchException(
                    "Access token c_nonce claim does not match Proof JWT nonce claim");
        }

        String credentialOfferId = accessTokenCustomClaims.credentialIdentifier();
        CredentialOfferCacheItem credentialOffer = dataStore.getCredentialOffer(credentialOfferId);

        LOGGER.info("Credential offer retrieved for credentialOfferId: {}", credentialOfferId);

        if (credentialOffer == null) {
            throw new DataStoreException("Null response returned when fetching credential offer");
        }

        if (!credentialOffer.getWalletSubjectId().equals(accessTokenCustomClaims.sub())) {
            throw new ClaimMismatchException(
                    "Access token sub claim does not match cached walletSubjectId");
        }

        Object documentDetails = getDocumentDetails(credentialOffer.getDocumentId());

        return credentialBuilder.buildCredential(proofJwtClaims.kid, documentDetails);
    }

    private static AccessTokenClaims getAccessTokenClaims(SignedJWT accessToken) {
        try {
            List<Object> credentialIdentifiers =
                    accessToken.getJWTClaimsSet().getListClaim("credential_identifiers");
            String credentialIdentifier = (String) credentialIdentifiers.get(0);
            String sub = accessToken.getJWTClaimsSet().getStringClaim("sub");
            String cNonce = accessToken.getJWTClaimsSet().getStringClaim("c_nonce");
            return new AccessTokenClaims(credentialIdentifier, sub, cNonce);
        } catch (ParseException | NullPointerException exception) {
            throw new RuntimeException(
                    String.format(
                            "Error parsing access token custom claims: %s",
                            exception.getMessage()));
        }
    }

    private record AccessTokenClaims(String credentialIdentifier, String sub, String cNonce) {}

    private static ProofJwtClaims getProofJwtClaims(SignedJWT proofJwt) {
        try {
            String nonce = proofJwt.getJWTClaimsSet().getStringClaim("nonce");
            String kid = proofJwt.getHeader().getKeyID();
            return new ProofJwtClaims(nonce, kid);
        } catch (ParseException exception) {
            throw new RuntimeException(
                    String.format(
                            "Error parsing Proof JWT custom claims: %s", exception.getMessage()));
        }
    }

    private record ProofJwtClaims(String nonce, String kid) {}

    private Object getDocumentDetails(String documentId) {
        URI uri;
        try {
            String documentBuilderUri = configurationService.getDocumentBuilderUrl();
            String getDocumentDetailsPath = "/document/" + documentId;
            uri = new URI(documentBuilderUri + getDocumentDetailsPath);
        } catch (URISyntaxException exception) {
            throw new RuntimeException("Error building Document URI: ", exception);
        }

        Response response = httpClient.target(uri).request(MediaType.APPLICATION_JSON).get();

        if (response.getStatus() != Response.Status.OK.getStatusCode()) {
            throw new RuntimeException(
                    String.format(
                            "Request to fetch document details for documentId %s failed with status code %s",
                            documentId, response.getStatus()));
        }
        return response.readEntity(Object.class);
    }
}
