package uk.gov.di.mobile.wallet.cri.credential;

import com.nimbusds.jose.JOSEException;
import com.nimbusds.jose.JWSAlgorithm;
import com.nimbusds.jose.crypto.ECDSAVerifier;
import com.nimbusds.jose.jwk.ECKey;
import com.nimbusds.jose.jwk.JWK;
import com.nimbusds.jwt.JWTClaimsSet;
import com.nimbusds.jwt.SignedJWT;
import com.nimbusds.jwt.proc.BadJWTException;
import com.nimbusds.jwt.proc.DefaultJWTClaimsVerifier;
import uk.gov.di.mobile.wallet.cri.services.ConfigurationService;
import uk.gov.di.mobile.wallet.cri.services.JwksService;

import javax.management.InvalidAttributeValueException;
import java.text.ParseException;
import java.util.List;
import java.util.Set;

public class AccessTokenService {
    private final JwksService jwksService;
    private final ConfigurationService configurationService;

    public AccessTokenService(JwksService jwksService, ConfigurationService configurationService) {
        this.jwksService = jwksService;
        this.configurationService = configurationService;
    }

    public void verifyAccessToken(SignedJWT accessToken)
            throws AccessTokenValidationException {
        verifyTokenHeader(accessToken);
        verifyTokenClaims(accessToken);
        if (!this.verifyTokenSignature(accessToken)) {
            throw new AccessTokenValidationException("Access token signature verification failed");
        }
    }

    private void verifyTokenHeader(SignedJWT accessToken) throws AccessTokenValidationException {
        JWSAlgorithm clientAlgorithm = JWSAlgorithm.parse("ES256");
        JWSAlgorithm jwtAlgorithm = accessToken.getHeader().getAlgorithm();
        if (jwtAlgorithm != clientAlgorithm) {
            throw new AccessTokenValidationException(
                    String.format(
                            "JWT alg header claim [%s] does not match client config alg [%s]",
                            jwtAlgorithm, clientAlgorithm));
        }

        String keyId = accessToken.getHeader().getKeyID();
        if (keyId == null) {
            throw new AccessTokenValidationException("JWT kid header claim is null");
        }
    }

    private void verifyTokenClaims(SignedJWT accessToken)
            throws AccessTokenValidationException {
        JWTClaimsSet expectedClaimValues =
                new JWTClaimsSet.Builder()
                        .issuer(configurationService.getOneLoginAuthServerUrl())
                        .audience(configurationService.getSelfUrl())
                        .build();
        Set<String> requiredClaims = Set.of("sub", "c_nonce", "credential_identifiers");
        try {
            JWTClaimsSet jwtClaimsSet = accessToken.getJWTClaimsSet();
            DefaultJWTClaimsVerifier<?> verifier =
                    new DefaultJWTClaimsVerifier<>(expectedClaimValues, requiredClaims);
            verifier.verify(jwtClaimsSet, null);
        } catch (BadJWTException | java.text.ParseException exception) {
            throw new AccessTokenValidationException(exception.getMessage(), exception);
        }
    }

    private boolean verifyTokenSignature(SignedJWT accessToken)
            throws AccessTokenValidationException {
        String keyId = accessToken.getHeader().getKeyID();
        try {
            JWK jwk = jwksService.retrieveJwkFromURLWithKeyId(keyId);
            final ECKey publicKey = new ECKey.Builder(jwk.toECKey()).build();
            ECDSAVerifier verifier = new ECDSAVerifier(publicKey);
            return accessToken.verify(verifier);
        } catch (JOSEException exception) {
            throw new AccessTokenValidationException(exception.getMessage(), exception);
        }
    }

    public record AccessTokenClaims(String credentialIdentifier, String sub, String cNonce) {}

    public AccessTokenClaims getAccessTokenClaims(SignedJWT accessToken)
            throws AccessTokenValidationException {
        try {
            JWTClaimsSet jwtClaimsSet = accessToken.getJWTClaimsSet();

            List<String> credentialIdentifiers =
                    jwtClaimsSet.getStringListClaim("credential_identifiers");
            if (credentialIdentifiers.isEmpty()) {
                throw new InvalidAttributeValueException("Empty credential_identifiers");
            }
            String credentialIdentifier = credentialIdentifiers.get(0);
            String sub = jwtClaimsSet.getStringClaim("sub");
            String cNonce = jwtClaimsSet.getStringClaim("c_nonce");
            return new AccessTokenClaims(credentialIdentifier, sub, cNonce);
        } catch (ParseException | NullPointerException | InvalidAttributeValueException exception) {
            throw new AccessTokenValidationException(
                    String.format(
                            "Error parsing access token custom claims: %s",
                            exception.getMessage()));
        }
    }
}
