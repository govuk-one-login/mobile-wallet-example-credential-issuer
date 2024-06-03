package uk.gov.di.mobile.wallet.cri.credential;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.nimbusds.jose.JOSEException;
import com.nimbusds.jose.JWSAlgorithm;
import com.nimbusds.jose.crypto.ECDSAVerifier;
import com.nimbusds.jose.jwk.ECKey;
import com.nimbusds.jose.jwk.JWK;
import com.nimbusds.jwt.JWTClaimsSet;
import com.nimbusds.jwt.SignedJWT;
import com.nimbusds.jwt.proc.BadJWTException;
import com.nimbusds.jwt.proc.DefaultJWTClaimsVerifier;
import jakarta.ws.rs.ProcessingException;
import jakarta.ws.rs.client.Client;
import jakarta.ws.rs.client.Invocation;
import jakarta.ws.rs.client.WebTarget;
import jakarta.ws.rs.core.MediaType;
import jakarta.ws.rs.core.Response;
import uk.gov.di.mobile.wallet.cri.services.ConfigurationService;

import java.net.URI;
import java.net.URISyntaxException;
import java.util.Arrays;
import java.util.HashSet;
import java.util.Set;

public class AccessTokenService {

    private static final String CLIENT_CONFIG_ALGORITHM = "ES256";
    private static final String CLIENT_CONFIG_ISSUER = "urn:fdc:gov:uk:wallet";
    private static final String CLIENT_CONFIG_AUDIENCE = "urn:fdc:gov:uk:example-credential-issuer";
    private final Client httpClient;
    private final ConfigurationService configurationService;

    public AccessTokenService(Client httpClient, ConfigurationService configurationService) {
        this.httpClient = httpClient;
        this.configurationService = configurationService;
    }

    public void verifyAccessToken(SignedJWT signedJwt)
            throws AccessTokenValidationException, URISyntaxException {
        verifyTokenHeader(signedJwt);
        verifyTokenClaims(signedJwt);
        if (!this.verifyTokenSignature(signedJwt)) {
            throw new AccessTokenValidationException("Access token signature verification failed");
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
            throws AccessTokenValidationException, URISyntaxException {
        String keyId = signedJwt.getHeader().getKeyID();
        JWK jwk = getJwk(keyId);

        try {
            final ECKey publicKey = new ECKey.Builder(jwk.toECKey()).build();
            ECDSAVerifier verifier = new ECDSAVerifier(publicKey);
            return signedJwt.verify(verifier);
        } catch (JOSEException exception) {
            throw new AccessTokenValidationException(exception.getMessage(), exception);
        }
    }

    private JWK getJwk(String keyId) throws AccessTokenValidationException, URISyntaxException {
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

    private String getDidDocument() throws AccessTokenValidationException, URISyntaxException {
        String authServerUrl = configurationService.getOneLoginAuthServerUrl();
        String didDocumentPath = configurationService.getAuthServerDidDocumentPath();
        URI uri = new URI(authServerUrl + didDocumentPath);

        Response response;
        try {
            WebTarget webTarget = httpClient.target(uri);
            Invocation.Builder invocationBuilder = webTarget.request(MediaType.APPLICATION_JSON);
            response = invocationBuilder.get();
        } catch (ProcessingException exception) {
            throw new AccessTokenValidationException("Could not fetch DID Document: ", exception);
        }

        if (response.getStatus() != Response.Status.OK.getStatusCode()) {
            throw new AccessTokenValidationException(
                    "Request to fetch DID Document failed with status code "
                            + response.getStatus());
        }
        return response.readEntity(String.class);
    }
}
