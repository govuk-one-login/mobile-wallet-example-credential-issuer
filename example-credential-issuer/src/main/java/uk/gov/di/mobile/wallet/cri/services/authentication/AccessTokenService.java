package uk.gov.di.mobile.wallet.cri.services.authentication;

import com.nimbusds.jose.Algorithm;
import com.nimbusds.jose.JOSEException;
import com.nimbusds.jose.JOSEObjectType;
import com.nimbusds.jose.JWSAlgorithm;
import com.nimbusds.jose.JWSHeader;
import com.nimbusds.jose.crypto.ECDSAVerifier;
import com.nimbusds.jose.jwk.Curve;
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

/** Service for validating and extracting data from access tokens. */
public class AccessTokenService {

    public static final String CLAIM_CREDENTIAL_IDENTIFIERS = "credential_identifiers";
    public static final String CLAIM_C_NONCE = "c_nonce";
    private static final String CLAIM_SUBJECT = "sub";
    private static final String CLAIM_EXPIRATION_TIME = "exp";
    private static final String CLAIM_JWT_ID = "jti";

    private static final JWSAlgorithm EXPECTED_SIGNING_ALGORITHM = JWSAlgorithm.parse("ES256");
    private static final JOSEObjectType EXPECTED_TYPE = new JOSEObjectType("at+jwt");

    private static final String REQUIRED_KEY_TYPE = "EC";
    private static final Curve REQUIRED_CURVE = Curve.P_256;

    private final JwksService jwksService;
    private final ConfigurationService configurationService;

    /**
     * Container for access token data.
     *
     * @param walletSubjectId The subject identifier from the access token.
     * @param nonce The nonce value from the access token.
     * @param credentialIdentifier The first credential identifier from the access token.
     */
    public record AccessTokenData(
            String walletSubjectId, String nonce, String credentialIdentifier) {}

    /**
     * Constructs a new AccessTokenService.
     *
     * @param jwksService Service to retrieve JWKs for signature validation.
     * @param configurationService Service providing configuration values.
     */
    public AccessTokenService(JwksService jwksService, ConfigurationService configurationService) {
        this.jwksService = jwksService;
        this.configurationService = configurationService;
    }

    /**
     * Verifies the access token's header, claims, and signature, and extracts its data.
     *
     * @param accessToken The signed JWT access token.
     * @return The extracted access token data.
     * @throws AccessTokenValidationException If the token is invalid.
     */
    public AccessTokenData verifyAccessToken(SignedJWT accessToken)
            throws AccessTokenValidationException {
        verifyTokenHeader(accessToken);
        verifyTokenClaims(accessToken);
        if (!verifyTokenSignature(accessToken)) {
            throw new AccessTokenValidationException("Access token signature verification failed");
        }
        return extractAccessTokenData(accessToken);
    }

    /**
     * Verifies the access token's header.
     *
     * @param accessToken The signed JWT access token.
     * @throws AccessTokenValidationException If the header is invalid.
     */
    private void verifyTokenHeader(SignedJWT accessToken) throws AccessTokenValidationException {
        JWSHeader header = accessToken.getHeader();

        JWSAlgorithm algorithm = header.getAlgorithm();
        if (!EXPECTED_SIGNING_ALGORITHM.equals(algorithm)) {
            throw new AccessTokenValidationException(
                    String.format(
                            "JWT alg header claim [%s] does not match expected alg [%s]",
                            algorithm, EXPECTED_SIGNING_ALGORITHM));
        }

        if (header.getKeyID() == null) {
            throw new AccessTokenValidationException("JWT kid header claim is null");
        }

        JOSEObjectType type = header.getType();
        if (!EXPECTED_TYPE.equals(type)) {
            throw new AccessTokenValidationException(
                    String.format(
                            "JWT typ header claim [%s] does not match expected typ [%s]",
                            type, EXPECTED_TYPE));
        }
    }

    /**
     * Verifies the access token's claims.
     *
     * @param accessToken The signed JWT access token.
     * @throws AccessTokenValidationException If the claims are invalid.
     */
    private void verifyTokenClaims(SignedJWT accessToken) throws AccessTokenValidationException {
        try {

            String expectedIssuer = configurationService.getOneLoginAuthServerUrl();
            String expectedAudience = configurationService.getSelfUrl().toString();
            JWTClaimsSet expectedClaimValues =
                    new JWTClaimsSet.Builder()
                            .issuer(expectedIssuer)
                            .audience(expectedAudience)
                            .build();
            HashSet<String> requiredClaims =
                    new HashSet<>(
                            Arrays.asList(
                                    CLAIM_SUBJECT,
                                    CLAIM_C_NONCE,
                                    CLAIM_CREDENTIAL_IDENTIFIERS,
                                    CLAIM_EXPIRATION_TIME,
                                    CLAIM_JWT_ID));
            JWTClaimsSet jwtClaimsSet = accessToken.getJWTClaimsSet();
            DefaultJWTClaimsVerifier<?> verifier =
                    new DefaultJWTClaimsVerifier<>(expectedClaimValues, requiredClaims);

            verifier.verify(jwtClaimsSet, null);

            if (jwtClaimsSet.getStringListClaim(CLAIM_CREDENTIAL_IDENTIFIERS).isEmpty()) {
                throw new InvalidAttributeValueException("Empty credential_identifiers claim");
            }

        } catch (BadJWTException | InvalidAttributeValueException | ParseException exception) {
            throw new AccessTokenValidationException(exception.getMessage(), exception);
        }
    }

    /**
     * Verifies the access token's signature.
     *
     * @param accessToken The signed JWT access token.
     * @return True if the signature is valid.
     * @throws AccessTokenValidationException If the signature verification fails.
     */
    private boolean verifyTokenSignature(SignedJWT accessToken)
            throws AccessTokenValidationException {
        String keyId = accessToken.getHeader().getKeyID();
        try {
            JWK jwk = jwksService.retrieveJwkFromURLWithKeyId(keyId);

            Algorithm algorithm = jwk.getAlgorithm();
            // Check that the JWK's algorithm matches expectation, if present
            if (algorithm != null && !EXPECTED_SIGNING_ALGORITHM.equals(algorithm)) {
                throw new AccessTokenValidationException(
                        String.format(
                                "JWK alg claim [%s] does not match expected alg [%s]",
                                algorithm, EXPECTED_SIGNING_ALGORITHM));
            }

            // If alg is not set, check key type and curve for ES256 compatibility
            if (algorithm == null) {
                if (!REQUIRED_KEY_TYPE.equals(jwk.getKeyType().getValue())) {
                    throw new AccessTokenValidationException("JWK key type is not EC");
                }
                ECKey ecKey = (ECKey) jwk;
                if (!REQUIRED_CURVE.equals(ecKey.getCurve())) {
                    throw new AccessTokenValidationException(
                            "JWK curve does not match expected curve for ES256");
                }
            }

            final ECKey publicKey = new ECKey.Builder(jwk.toECKey()).build();
            ECDSAVerifier verifier = new ECDSAVerifier(publicKey);
            return accessToken.verify(verifier);
        } catch (JOSEException exception) {
            throw new AccessTokenValidationException(exception.getMessage(), exception);
        }
    }

    /**
     * Extracts data from the access token.
     *
     * @param token The signed JWT access token.
     * @return The extracted access token data.
     * @throws AccessTokenValidationException If the token data is invalid.
     */
    private static AccessTokenData extractAccessTokenData(SignedJWT token)
            throws AccessTokenValidationException {
        try {
            JWTClaimsSet jwtClaimsSet = token.getJWTClaimsSet();
            return new AccessTokenData(
                    jwtClaimsSet.getSubject(),
                    jwtClaimsSet.getStringClaim(CLAIM_C_NONCE),
                    jwtClaimsSet.getListClaim(CLAIM_CREDENTIAL_IDENTIFIERS).get(0).toString());
        } catch (ParseException exception) {
            throw new AccessTokenValidationException(exception.getMessage(), exception);
        }
    }
}
