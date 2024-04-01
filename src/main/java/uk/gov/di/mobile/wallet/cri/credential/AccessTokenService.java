package uk.gov.di.mobile.wallet.cri.credential;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.nimbusds.jose.JOSEException;
import com.nimbusds.jose.JWSAlgorithm;
import com.nimbusds.jose.crypto.RSASSAVerifier;
import com.nimbusds.jose.jwk.JWK;
import com.nimbusds.jose.jwk.RSAKey;
import com.nimbusds.jwt.JWTClaimsSet;
import com.nimbusds.jwt.SignedJWT;
import com.nimbusds.jwt.proc.BadJWTException;
import com.nimbusds.jwt.proc.DefaultJWTClaimsVerifier;
import com.nimbusds.oauth2.sdk.token.BearerAccessToken;
import jakarta.ws.rs.client.Client;
import jakarta.ws.rs.core.MediaType;
import jakarta.ws.rs.core.Response;

import java.util.*;

public class AccessTokenService {
    private static final String CONFIG_ALGORITHM = "RS256";
    private static final String CONFIG_AUDIENCE = "urn:fdc:gov:uk:wallet";
    private static final String CONFIG_ISSUER = "urn:fdc:gov:uk:<HMRC>";

    private final Client client;

    public AccessTokenService(Client client) {
        this.client = client;
    }

    public SignedJWT verifyAccessToken(BearerAccessToken accessToken)
            throws AccessTokenValidationException {

        SignedJWT signedJwt = parseAccessToken(accessToken);

        verifyTokenHeader(CONFIG_ALGORITHM, signedJwt);
        verifyTokenClaims(signedJwt);

        if (!this.verifyJWTSignature(signedJwt)) {
            throw new AccessTokenValidationException("Access token signature verification failed");
        }

        return signedJwt;
    }

    private SignedJWT parseAccessToken(BearerAccessToken accessToken)
            throws AccessTokenValidationException {
        try {
            return SignedJWT.parse(accessToken.getValue());
        } catch (java.text.ParseException exception) {
            throw new AccessTokenValidationException(
                    String.format(
                            "Could not parse request access token: %s", exception.getMessage()),
                    exception);
        }
    }

    private void verifyTokenHeader(String clientAlgorithmString, SignedJWT signedJwt)
            throws AccessTokenValidationException {
        JWSAlgorithm clientAlgorithm = JWSAlgorithm.parse(clientAlgorithmString);
        JWSAlgorithm jwtAlgorithm = signedJwt.getHeader().getAlgorithm();
        if (jwtAlgorithm != clientAlgorithm) {
            throw new AccessTokenValidationException(
                    String.format(
                            "JWT alg header claim %s does not match client config alg %s",
                            jwtAlgorithm, clientAlgorithm));
        }

        String keyId = signedJwt.getHeader().getKeyID();
        if (keyId == null) {
            throw new AccessTokenValidationException("JWT kid header claim is null");
        }
    }

    private void verifyTokenClaims(SignedJWT signedJwt) throws AccessTokenValidationException {

        Set<String> requiredClaims =
                new HashSet<>(Arrays.asList("sub", "c_nonce", "credential_identifiers"));
        JWTClaimsSet expectedClaimValues =
                new JWTClaimsSet.Builder().issuer(CONFIG_ISSUER).audience(CONFIG_AUDIENCE).build();

        try {
            JWTClaimsSet jwtClaimsSet = signedJwt.getJWTClaimsSet();
            DefaultJWTClaimsVerifier<?> verifier =
                    new DefaultJWTClaimsVerifier<>(expectedClaimValues, requiredClaims);
            verifier.verify(jwtClaimsSet, null);
        } catch (BadJWTException | java.text.ParseException exception) {
            throw new AccessTokenValidationException(exception.getMessage(), exception);
        }
    }

    private boolean verifyJWTSignature(SignedJWT signedJwt) throws AccessTokenValidationException {
        String keyId = signedJwt.getHeader().getKeyID();
        JWK jwk = getJwk(keyId);

        if (jwk == null) {
            throw new AccessTokenValidationException(
                    "JWT key ID did not match any key in did document");
        }

        try {
            final RSAKey publicKey = new RSAKey.Builder(jwk.toRSAKey()).build();
            RSASSAVerifier verifier = new RSASSAVerifier(publicKey);
            return signedJwt.verify(verifier);
        } catch (JOSEException exception) {
            throw new AccessTokenValidationException(exception.getMessage(), exception);
        }
    }

    private JWK getJwk(String keyId) throws AccessTokenValidationException {
        String didDocumentString = getDidDocument();

        ObjectMapper objectMapper = new ObjectMapper();

        try {
            JsonNode verificationMethod =
                    objectMapper.readTree(didDocumentString).get("verificationMethod");

            if (verificationMethod.isArray()) {
                for (JsonNode verificationMethodItem : verificationMethod) {
                    String publicKeyJwk = verificationMethodItem.path("publicKeyJwk").toString();

                    JWK jwk = JWK.parse(publicKeyJwk);

                    if (jwk.getKeyID().equals(keyId)) {
                        return jwk;
                    }
                }
            }
        } catch (JsonProcessingException | java.text.ParseException exception) {
            throw new AccessTokenValidationException(exception.getMessage(), exception);
        }

        return null;
    }

    private String getDidDocument() {
        //        try {
        Response response =
                client.target("http://localhost:8000/sts-stub/.well-known/did.json")
                        .request()
                        .accept(MediaType.APPLICATION_JSON)
                        .get();
        return response.readEntity(String.class);

        //        } catch (Exception exception) {
        //            throw new Exception(exception);
        //        }
    }
}
