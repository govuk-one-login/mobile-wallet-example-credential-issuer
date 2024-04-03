package uk.gov.di.mobile.wallet.cri.credential;

import com.nimbusds.jwt.SignedJWT;
import com.nimbusds.oauth2.sdk.token.BearerAccessToken;
import jakarta.ws.rs.client.Client;
import jakarta.ws.rs.core.MediaType;
import jakarta.ws.rs.core.Response;
import uk.gov.di.mobile.wallet.cri.models.CredentialOfferCacheItem;
import uk.gov.di.mobile.wallet.cri.services.ConfigurationService;
import uk.gov.di.mobile.wallet.cri.services.data_storage.DataStore;
import uk.gov.di.mobile.wallet.cri.services.data_storage.DataStoreException;
import uk.gov.di.mobile.wallet.cri.services.signing.SigningException;

import java.net.URI;
import java.net.URISyntaxException;
import java.text.ParseException;
import java.util.List;

public class CredentialService {

    private final ConfigurationService configurationService;
    private final DataStore dataStore;
    private final AccessTokenService accessTokenService;
    private final ProofJwtService proofJwtService;
    private final Client httpClient;
    private final CredentialBuilder credentialBuilder;

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

    public Credential run(BearerAccessToken bearerAccessToken, CredentialRequest credentialRequest)
            throws DataStoreException,
                    ProofJwtValidationException,
                    ClaimMismatchException,
                    SigningException,
                    AccessTokenValidationException {

        SignedJWT accessToken = accessTokenService.verifyAccessToken(bearerAccessToken);

        AccessTokenClaims accessTokenCustomClaims = getAccessTokenClaims(accessToken);

        SignedJWT proofJwt = proofJwtService.verifyProofJwt(credentialRequest);
        ProofJwtClaims proofJwtClaims = getProofJwtClaims(proofJwt);

        if (!proofJwtClaims.nonce().equals(accessTokenCustomClaims.cNonce())) {
            throw new ClaimMismatchException(
                    "Access token c_nonce claim does not match Proof JWT nonce claim");
        }

        String partitionValue = accessTokenCustomClaims.credentialIdentifier();
        CredentialOfferCacheItem credentialOffer = dataStore.getCredentialOffer(partitionValue);

        if (credentialOffer == null) {
            throw new DataStoreException(
                    "Null response returned when fetching credential offer with identifier "
                            + partitionValue);
        }

        if (!credentialOffer.getWalletSubjectId().equals(accessTokenCustomClaims.sub())) {
            throw new ClaimMismatchException(
                    "Access token sub (walletSubjectId) claim does not match credential offer walletSubjectId");
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

        } catch (ParseException exception) {
            throw new RuntimeException("Error parsing access token custom custom");
        }
    }

    private record AccessTokenClaims(String credentialIdentifier, String sub, String cNonce) {}

    private static ProofJwtClaims getProofJwtClaims(SignedJWT proofJwt) {
        try {
            String nonce = proofJwt.getJWTClaimsSet().getStringClaim("nonce");
            String kid = proofJwt.getHeader().getKeyID();
            return new ProofJwtClaims(nonce, kid);

        } catch (ParseException exception) {
            throw new RuntimeException("Error parsing Proof JWT custom custom");
        }
    }

    private record ProofJwtClaims(String nonce, String kid) {}

    private Object getDocumentDetails(String documentId) {
        URI uri;
        try {
            String documentBuilderUri = configurationService.getDocumentBuilderUrl();
            String getDocumentDetailsPath = "/document/" + documentId;
            uri = new URI(documentBuilderUri + getDocumentDetailsPath);
        } catch (URISyntaxException e) {
            throw new RuntimeException("Error building Document URI", e);
        }

        Response response =
                httpClient.target(uri).request().accept(MediaType.APPLICATION_JSON).get();

        return response.readEntity(Object.class);
    }
}
