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
import jakarta.ws.rs.client.Invocation;
import jakarta.ws.rs.client.WebTarget;
import jakarta.ws.rs.core.MediaType;
import jakarta.ws.rs.core.Response;
import uk.gov.di.mobile.wallet.cri.services.ConfigurationService;

import java.net.URI;
import java.net.URISyntaxException;
import java.util.*;

public class AccessTokenService {

    private static final String CLIENT_CONFIG_ALGORITHM = "RS256";
    private static final String CLIENT_CONFIG_ISSUER = "urn:fdc:gov:uk:sts";
    private static final String CLIENT_CONFIG_AUDIENCE = "urn:fdc:gov:uk:<HMRC>";
    private static final String DID_DOCUMENT_PATH = "/.well-known/did.json";

    private final Client httpClient;
    private final ConfigurationService configurationService;

    public AccessTokenService(Client httpClient, ConfigurationService configurationService) {
        this.httpClient = httpClient;
        this.configurationService = configurationService;
    }

    public SignedJWT verifyAccessToken(BearerAccessToken accessToken)
            throws AccessTokenValidationException {

        SignedJWT signedJwt = parseAccessToken(accessToken);

        verifyTokenHeader(signedJwt);
        verifyTokenClaims(signedJwt);

        if (!this.verifyTokenSignature(signedJwt)) {
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
                    String.format("Error parsing access token: %s", exception.getMessage()),
                    exception);
        }
    }

    private void verifyTokenHeader(SignedJWT signedJwt) throws AccessTokenValidationException {
        JWSAlgorithm clientAlgorithm = JWSAlgorithm.parse(CLIENT_CONFIG_ALGORITHM);
        JWSAlgorithm jwtAlgorithm = signedJwt.getHeader().getAlgorithm();
        if (jwtAlgorithm != clientAlgorithm) {
            throw new AccessTokenValidationException(
                    String.format(
                            "JWT alg header claim [%s] does not match client config alg [%s]",
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
                new JWTClaimsSet.Builder()
                        .issuer(CLIENT_CONFIG_ISSUER)
                        .audience(CLIENT_CONFIG_AUDIENCE)
                        .build();

        JWTClaimsSet jwtClaimsSet;
        try {
            jwtClaimsSet = signedJwt.getJWTClaimsSet();
            DefaultJWTClaimsVerifier<?> verifier =
                    new DefaultJWTClaimsVerifier<>(expectedClaimValues, requiredClaims);
            verifier.verify(jwtClaimsSet, null);
        } catch (BadJWTException | java.text.ParseException exception) {
            throw new AccessTokenValidationException(exception.getMessage(), exception);
        }
    }

    private boolean verifyTokenSignature(SignedJWT signedJwt)
            throws AccessTokenValidationException {
        String keyId = signedJwt.getHeader().getKeyID();
        JWK jwk = getJwk(keyId);

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

            if (verificationMethod == null) {
                throw new AccessTokenValidationException(
                        "Invalid DID document: verificationMethod is null");
            }

            if (!verificationMethod.isArray()) {
                throw new AccessTokenValidationException(
                        "Invalid DID document: verificationMethod is not an array");
            }

            for (JsonNode verificationMethodItem : verificationMethod) {
                String publicKeyJwk = verificationMethodItem.path("publicKeyJwk").toString();

                JWK jwk = JWK.parse(publicKeyJwk);

                if (jwk.getKeyID().equals(keyId)) {
                    return jwk;
                }
            }
        } catch (JsonProcessingException | java.text.ParseException exception) {
            throw new AccessTokenValidationException(
                    String.format("Error parsing JWK: %s", exception.getMessage()), exception);
        }

        throw new AccessTokenValidationException(
                "JWT key ID did not match any key in DID document");
    }

    private String getDidDocument() {
        String stsStubUrl = configurationService.getStsStubUrl();

        URI uri;
        try {
            uri = new URI(stsStubUrl + DID_DOCUMENT_PATH);
        } catch (URISyntaxException e) {
            throw new RuntimeException("Error building STS URI", e);
        }

        WebTarget webTarget = httpClient.target(uri);
        Invocation.Builder invocationBuilder = webTarget.request(MediaType.APPLICATION_JSON);
        Response response = invocationBuilder.get();

        if (response.getStatus() != Response.Status.OK.getStatusCode()) {
            System.out.println(response);
            throw new RuntimeException(
                    "Request to fetch DID Document failed with status code "
                            + response.getStatus());
        }

        return response.readEntity(String.class);
    }
}
