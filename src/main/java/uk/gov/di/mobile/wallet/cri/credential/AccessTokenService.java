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

import java.net.MalformedURLException;
import java.net.URL;
import java.util.Arrays;
import java.util.HashSet;
import java.util.Set;

public class AccessTokenService {

    private static final String CLIENT_CONFIG_ALGORITHM = "ES256";
    private static final String CLIENT_CONFIG_ISSUER = "urn:fdc:gov:uk:wallet";
    private static final String CLIENT_CONFIG_AUDIENCE = "urn:fdc:gov:uk:example-credential-issuer";
    private static final String JWKS_PATH = "/.well-known/jwks.json"; // NOSONAR

    private final JwksService jwksService;

    public AccessTokenService(ConfigurationService configurationService)
            throws MalformedURLException {
        this.jwksService =
                new JwksService(
                        new URL(configurationService.getOneLoginAuthServerUrl() + JWKS_PATH));
    }

    public AccessTokenService(JwksService jwksService) {
        this.jwksService = jwksService;
    }

    public void verifyAccessToken(SignedJWT signedJwt) throws AccessTokenValidationException {
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
            throws AccessTokenValidationException {
        String keyId = signedJwt.getHeader().getKeyID();
        try {
            JWK jwk = jwksService.retrieveJwkFromURLWithKeyId(keyId);
            final ECKey publicKey = new ECKey.Builder(jwk.toECKey()).build();
            ECDSAVerifier verifier = new ECDSAVerifier(publicKey);
            return signedJwt.verify(verifier);
        } catch (JOSEException exception) {
            throw new AccessTokenValidationException(exception.getMessage(), exception);
        }
    }
}
