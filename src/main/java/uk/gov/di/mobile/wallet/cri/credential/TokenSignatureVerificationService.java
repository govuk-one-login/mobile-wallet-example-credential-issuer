package uk.gov.di.mobile.wallet.cri.credential;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.nimbusds.jose.JWSHeader;
import com.nimbusds.jose.jwk.JWK;
import com.nimbusds.jwt.JWTClaimsSet;
import com.nimbusds.jwt.SignedJWT;
import com.nimbusds.oauth2.sdk.token.BearerAccessToken;
import jakarta.ws.rs.client.Client;
import jakarta.ws.rs.core.MediaType;
import jakarta.ws.rs.core.Response;

import java.text.ParseException;

public class TokenSignatureVerificationService {

    private final Client client;

    public TokenSignatureVerificationService(Client client) {
        this.client = client;
    }

    public JWK verifyAccessTokenSignature(BearerAccessToken accessToken)
            throws JsonProcessingException, ParseException {

        SignedJWT signedAccessToken = SignedJWT.parse(accessToken.getValue());

        JWTClaimsSet claims = signedAccessToken.getJWTClaimsSet();
        System.out.println(claims);

        JWSHeader headers = signedAccessToken.getHeader();
        System.out.println(headers);

        String keyId = headers.getKeyID();
        System.out.println(keyId);

        JWK jwk = getJwk(keyId);

        return jwk;
    }

    private JWK getJwk(String keyId) throws JsonProcessingException, ParseException {
        String didDocumentString = getDidDocument();

        ObjectMapper objectMapper = new ObjectMapper();

        JsonNode verificationMethod =
                objectMapper.readTree(didDocumentString).get("verificationMethod");

        if (verificationMethod.isArray()) {
            for (JsonNode verificationMethodItem : verificationMethod) {
                String publicKeyJwk = verificationMethodItem.path("publicKeyJwk").toString();
                System.out.println(publicKeyJwk);

                JWK jwk = JWK.parse(publicKeyJwk);
                System.out.println(jwk);

                if (jwk.getKeyID().equals(keyId)) {
                    System.out.println("EQUAL");
                    return jwk;
                }
            }
        }
        return null;
    }

    private String getDidDocument() {
        Response response =
                client.target("http://localhost:8000/sts-stub/.well-known/did.json")
                        .request()
                        .accept(MediaType.APPLICATION_JSON)
                        .get();

        return response.readEntity(String.class);
    }
}
