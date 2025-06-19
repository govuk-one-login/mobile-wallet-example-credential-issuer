package uk.gov.di.mobile.wallet.cri.services.authentication;

import com.nimbusds.jose.JOSEException;
import com.nimbusds.jose.JWSAlgorithm;
import com.nimbusds.jose.JWSHeader;
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

public class AccessTokenService {

    public static final String CREDENTIAL_IDENTIFIERS = "credential_identifiers";
    public static final String C_NONCE = "c_nonce";
    private static final JWSAlgorithm EXPECTED_SIGNING_ALGORITHM = JWSAlgorithm.parse("ES256");
    private static final String EXPECTED_HEADER_TYP = "at+jwt";

    private final JwksService jwksService;
    private final ConfigurationService configurationService;

    public record AccessTokenData(
            String walletSubjectId, String nonce, String credentialIdentifier) {}

    public AccessTokenService(JwksService jwksService, ConfigurationService configurationService) {
        this.jwksService = jwksService;
        this.configurationService = configurationService;
    }

    public AccessTokenData verifyAccessToken(SignedJWT accessToken)
            throws AccessTokenValidationException {
        verifyTokenHeader(accessToken);
        verifyTokenClaims(accessToken);
        if (!this.verifyTokenSignature(accessToken)) {
            throw new AccessTokenValidationException("Access token signature verification failed");
        }
        return extractAccessTokenData(accessToken);
    }

    private void verifyTokenHeader(SignedJWT accessToken) throws AccessTokenValidationException {
        JWSHeader header = accessToken.getHeader();

        JWSAlgorithm algorithm = header.getAlgorithm();
        if (!EXPECTED_SIGNING_ALGORITHM.equals(algorithm)) {
            throw new AccessTokenValidationException(
                    String.format(
                            "JWT alg header claim [%s] does not match client config alg [%s]",
                            algorithm, EXPECTED_SIGNING_ALGORITHM));
        }

        if (header.getKeyID() == null) {
            throw new AccessTokenValidationException("JWT kid header claim is null");
        }

        String type = header.getType().toString();
        if (!EXPECTED_HEADER_TYP.equals(type)) {
            throw new AccessTokenValidationException(
                    String.format(
                            "JWT typ header claim [%s] does not match expected typ [%s]",
                            algorithm, EXPECTED_HEADER_TYP));
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
                new HashSet<>(Arrays.asList("sub", C_NONCE, CREDENTIAL_IDENTIFIERS));

        try {
            JWTClaimsSet jwtClaimsSet = accessToken.getJWTClaimsSet();
            DefaultJWTClaimsVerifier<?> verifier =
                    new DefaultJWTClaimsVerifier<>(expectedClaimValues, requiredClaims);
            verifier.verify(jwtClaimsSet, null);

            if (jwtClaimsSet.getStringListClaim(CREDENTIAL_IDENTIFIERS).isEmpty()) {
                throw new InvalidAttributeValueException("Empty credential_identifiers claim");
            }

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

    private static AccessTokenData extractAccessTokenData(SignedJWT token)
            throws AccessTokenValidationException {
        try {
            JWTClaimsSet jwtClaimsSet = token.getJWTClaimsSet();
            return new AccessTokenData(
                    jwtClaimsSet.getSubject(),
                    jwtClaimsSet.getStringClaim(C_NONCE),
                    jwtClaimsSet.getListClaim(CREDENTIAL_IDENTIFIERS).get(0).toString());
        } catch (ParseException exception) {
            throw new AccessTokenValidationException(exception.getMessage(), exception);
        }
    }
}
