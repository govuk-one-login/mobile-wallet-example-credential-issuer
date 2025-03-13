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
import java.util.Arrays;
import java.util.HashSet;
import java.util.List;

public class AccessTokenService {

    private static final JWSAlgorithm EXPECTED_SIGNING_ALGORITHM = JWSAlgorithm.parse("ES256");
    private final JwksService jwksService;
    private final ConfigurationService configurationService;
    public String sub;
    public String credentialIdentifier;
    public String cNonce;

    public AccessTokenService(JwksService jwksService, ConfigurationService configurationService) {
        this.jwksService = jwksService;
        this.configurationService = configurationService;
    }

    public void verifyAccessToken(SignedJWT accessToken) throws AccessTokenValidationException {
        verifyTokenHeader(accessToken);
        verifyTokenClaims(accessToken);
        if (!this.verifyTokenSignature(accessToken)) {
            throw new AccessTokenValidationException("Access token signature verification failed");
        }
    }

    private void verifyTokenHeader(SignedJWT accessToken) throws AccessTokenValidationException {
        JWSAlgorithm jwtAlgorithm = accessToken.getHeader().getAlgorithm();
        if (jwtAlgorithm != EXPECTED_SIGNING_ALGORITHM) {
            throw new AccessTokenValidationException(
                    String.format(
                            "JWT alg header claim [%s] does not match client config alg [%s]",
                            jwtAlgorithm, EXPECTED_SIGNING_ALGORITHM));
        }

        if (accessToken.getHeader().getKeyID() == null) {
            throw new AccessTokenValidationException("JWT kid header claim is null");
        }
    }

    private void verifyTokenClaims(SignedJWT accessToken) throws AccessTokenValidationException {
        String expectedIssuer = configurationService.getOneLoginAuthServerUrl();
        String expectedAudience = configurationService.getSelfUrl();
        JWTClaimsSet expectedClaimValues =
                new JWTClaimsSet.Builder()
                        .issuer(expectedIssuer)
                        .audience(expectedAudience)
                        .build();
        HashSet<String> requiredClaims =
                new HashSet<>(Arrays.asList("sub", "c_nonce", "credential_identifiers"));

        try {
            JWTClaimsSet jwtClaimsSet = accessToken.getJWTClaimsSet();
            DefaultJWTClaimsVerifier<?> verifier =
                    new DefaultJWTClaimsVerifier<>(expectedClaimValues, requiredClaims);
            verifier.verify(jwtClaimsSet, null);

            sub = jwtClaimsSet.getSubject();
            cNonce = jwtClaimsSet.getClaim("c_nonce").toString();
            List<String> credentialIdentifiers =
                    jwtClaimsSet.getStringListClaim("credential_identifiers");
            if (credentialIdentifiers.isEmpty()) {
                throw new InvalidAttributeValueException("Empty credential_identifiers claim");
            }
            credentialIdentifier = credentialIdentifiers.get(0);

        } catch (BadJWTException | InvalidAttributeValueException | ParseException exception) {
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
}
