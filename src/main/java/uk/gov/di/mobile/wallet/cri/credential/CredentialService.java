package uk.gov.di.mobile.wallet.cri.credential;

import com.nimbusds.jwt.JWTClaimsSet;
import com.nimbusds.jwt.SignedJWT;
import com.nimbusds.oauth2.sdk.token.BearerAccessToken;
import jakarta.ws.rs.client.Client;
import jakarta.ws.rs.core.MediaType;
import jakarta.ws.rs.core.Response;
import uk.gov.di.mobile.wallet.cri.models.CredentialOfferCacheItem;
import uk.gov.di.mobile.wallet.cri.services.ConfigurationService;
import uk.gov.di.mobile.wallet.cri.services.data_storage.DataStore;
import uk.gov.di.mobile.wallet.cri.services.data_storage.DataStoreException;

import java.text.ParseException;

public class CredentialService {
    private final ConfigurationService configurationService;

    private final DataStore dataStore;
    private final AccessTokenService accessTokenService;
    private final ProofJwtService proofJwtService;
    private final Client client;

    public CredentialService(
            ConfigurationService configurationService,
            DataStore dataStore,
            AccessTokenService accessTokenService,
            ProofJwtService proofJwtService,
            Client client) {
        this.configurationService = configurationService;
        this.dataStore = dataStore;
        this.accessTokenService = accessTokenService;
        this.proofJwtService = proofJwtService;
        this.client = client;
    }

    public String buildCredential(
            BearerAccessToken bearerAccessToken, CredentialRequest credentialRequest)
            throws AccessTokenValidationException,
                    ParseException,
                    DataStoreException,
                    ProofJwtValidationException {

        SignedJWT accessToken = accessTokenService.verifyAccessToken(bearerAccessToken);
        System.out.println("accessToken");

        AccessTokenCustomClaims accessTokenCustomClaims = getAccessTokenCustomClaims(accessToken);
        CredentialOfferCacheItem credentialOffer =
                dataStore.getCredentialOffer(accessTokenCustomClaims.getCredentialIdentifier());

        if (!credentialOffer.getWalletSubjectId().equals(accessTokenCustomClaims.getSub())) {
            throw new AccessTokenValidationException(
                    "JWT sub claim does not match credential offer walletSubjectId");
        }

        // SignedJWT proofJwt = proofJwtService.verifyProofJwt(credentialRequest);

        Object response = getDocumentDetails(credentialOffer.getDocumentId());
        System.out.println(response);

        //        Credential credential = new Credential();

        return "Credential";
    }

    private AccessTokenCustomClaims getAccessTokenCustomClaims(SignedJWT accessToken)
            throws ParseException {
        JWTClaimsSet jwtClaimsSet = accessToken.getJWTClaimsSet();
        String credentialIdentifier =
                (String) jwtClaimsSet.getListClaim("credential_identifiers").get(0);
        String sub = jwtClaimsSet.getStringClaim("sub");
        String c_nonce = jwtClaimsSet.getStringClaim("c_nonce");
        return new AccessTokenCustomClaims(credentialIdentifier, sub, c_nonce);
    }

    private String getDocumentDetails(String documentId) {
        //        try {
        Response response =
                client.target("http://localhost:8000/document/" + documentId)
                        .request()
                        .accept(MediaType.APPLICATION_JSON)
                        .get();
        return response.readEntity(String.class);

        //        } catch (Exception exception) {
        //            throw new Exception(exception);
        //        }
    }
}
