package uk.gov.di.mobile.wallet.cri.credential.proof;

import com.nimbusds.jose.JOSEException;
import com.nimbusds.jose.JOSEObjectType;
import com.nimbusds.jose.JWSAlgorithm;
import com.nimbusds.jose.JWSHeader;
import com.nimbusds.jose.crypto.ECDSAVerifier;
import com.nimbusds.jose.jwk.Curve;
import com.nimbusds.jose.jwk.ECKey;
import com.nimbusds.jwt.JWTClaimsSet;
import com.nimbusds.jwt.SignedJWT;
import com.nimbusds.jwt.proc.BadJWTException;
import com.nimbusds.jwt.proc.DefaultJWTClaimsVerifier;
import uk.gov.di.mobile.wallet.cri.credential.proof.did_key.DidKeyResolver;
import uk.gov.di.mobile.wallet.cri.credential.proof.did_key.InvalidDidKeyException;
import uk.gov.di.mobile.wallet.cri.services.ConfigurationService;

import java.security.NoSuchAlgorithmException;
import java.security.interfaces.ECPublicKey;
import java.security.spec.InvalidKeySpecException;
import java.text.ParseException;
import java.util.Arrays;
import java.util.HashSet;

/**
 * Service for verifying OpenID4VCI Proof JWTs.
 *
 * <p>This service handles the verification of Proof JWTs used in OpenID4VCI flows, including header
 * validation, payload claims verification, and signature verification using DID key resolution.
 */
public class ProofJwtService {

    public static final String NONCE = "nonce";
    private static final JWSAlgorithm EXPECTED_SIGNING_ALGORITHM = JWSAlgorithm.parse("ES256");
    private static final String EXPECTED_ISSUER = "urn:fdc:gov:uk:wallet";
    private static final String EXPECTED_JWT_TYPE = "openid4vci-proof+jwt";

    private final ConfigurationService configurationService;

    /**
     * Data container for verified Proof JWT information.
     *
     * @param didKey The did:key from the JWT header
     * @param nonce The nonce value from the JWT payload
     * @param publicKey The resolved EC public key from the did:key
     */
    public record ProofJwtData(String didKey, String nonce, ECPublicKey publicKey) {}

    /**
     * Constructs a new ProofJwtService with the specified configuration service.
     *
     * @param configurationService The configuration service for retrieving application settings
     */
    public ProofJwtService(ConfigurationService configurationService) {
        this.configurationService = configurationService;
    }

    /**
     * Verifies the Proof JWT header and payload claims and its signature.
     *
     * @param proofJwt The Proof JWT to verify
     * @return ProofJwtData containing the did:key, nonce, and public key
     * @throws ProofJwtValidationException On any error verifying the token claims and signature
     */
    public ProofJwtData verifyProofJwt(SignedJWT proofJwt) throws ProofJwtValidationException {
        verifyTokenHeader(proofJwt);
        verifyTokenClaims(proofJwt);

        String didKey = proofJwt.getHeader().getKeyID();
        ECPublicKey publicKey = getPublicKey(didKey);

        if (!verifyTokenSignature(proofJwt, publicKey)) {
            throw new ProofJwtValidationException("Proof JWT signature verification failed");
        }

        return extractProofJwtData(proofJwt, publicKey);
    }

    /**
     * Verifies that the required header claims are present and match expected values.
     *
     * @param proofJwt The Proof JWT to validate
     * @throws ProofJwtValidationException On invalid header claims
     */
    private void verifyTokenHeader(SignedJWT proofJwt) throws ProofJwtValidationException {
        JWSHeader header = proofJwt.getHeader();

        JWSAlgorithm jwtAlgorithm = header.getAlgorithm();
        if (!EXPECTED_SIGNING_ALGORITHM.equals(jwtAlgorithm)) {
            throw new ProofJwtValidationException(
                    String.format(
                            "JWT alg header claim [%s] does not match expected algorithm [%s]",
                            jwtAlgorithm, EXPECTED_SIGNING_ALGORITHM));
        }

        if (header.getKeyID() == null) {
            throw new ProofJwtValidationException("JWT kid header claim is null");
        }

        JOSEObjectType typ = header.getType();
        if (typ == null) {
            throw new ProofJwtValidationException("JWT type header claim is null");
        }
        if (!EXPECTED_JWT_TYPE.equals(typ.toString())) {
            throw new ProofJwtValidationException(
                    String.format(
                            "JWT type header claim [%s] does not match expected type [%s]",
                            typ, EXPECTED_JWT_TYPE));
        }
    }

    /**
     * Verifies that the required payload claims are present and match expected values.
     *
     * @param proofJwt The Proof JWT to validate
     * @throws ProofJwtValidationException On invalid payload claims
     */
    private void verifyTokenClaims(SignedJWT proofJwt) throws ProofJwtValidationException {
        String expectedAudience = configurationService.getSelfUrl();
        JWTClaimsSet expectedClaimValues =
                new JWTClaimsSet.Builder()
                        .issuer(EXPECTED_ISSUER)
                        .audience(expectedAudience)
                        .build();
        HashSet<String> requiredClaims = new HashSet<>(Arrays.asList("iat", NONCE));

        try {
            JWTClaimsSet jwtClaimsSet = proofJwt.getJWTClaimsSet();
            DefaultJWTClaimsVerifier<?> verifier =
                    new DefaultJWTClaimsVerifier<>(expectedClaimValues, requiredClaims);
            verifier.verify(jwtClaimsSet, null);
        } catch (BadJWTException | ParseException exception) {
            throw new ProofJwtValidationException(exception.getMessage(), exception);
        }
    }

    /**
     * Extracts the EC public key from the did:key.
     *
     * @param didKey The did:key to resolve
     * @return The EC public key
     * @throws ProofJwtValidationException On error resolving the did:key or generating the public
     *     key
     */
    private ECPublicKey getPublicKey(String didKey) throws ProofJwtValidationException {
        try {
            DidKeyResolver didKeyResolver = new DidKeyResolver();
            DidKeyResolver.DecodedKeyData resolvedDidKey = didKeyResolver.decodeDidKey(didKey);
            byte[] rawPublicKeyBytes = resolvedDidKey.rawPublicKeyBytes();
            return didKeyResolver.generatePublicKeyFromBytes(rawPublicKeyBytes);
        } catch (NoSuchAlgorithmException
                | InvalidKeySpecException
                | InvalidDidKeyException exception) {
            throw new ProofJwtValidationException(
                    String.format(
                            "Error getting public key from did:key [%s]: %s",
                            didKey, exception.getMessage()),
                    exception);
        }
    }

    /**
     * Verifies the Proof JWT signature using the provided public key.
     *
     * <p>Creates an ECDSA verifier with the P-256 curve and verifies the JWT signature.
     *
     * @param proofJwt The Proof JWT to verify
     * @param publicKey The EC public key to use for signature verification
     * @return true if the signature is valid, false otherwise
     * @throws ProofJwtValidationException On error during signature verification
     */
    private boolean verifyTokenSignature(SignedJWT proofJwt, ECPublicKey publicKey)
            throws ProofJwtValidationException {
        try {
            ECKey ecKey = new ECKey.Builder(Curve.P_256, publicKey).build();
            ECDSAVerifier verifier = new ECDSAVerifier(ecKey);
            return proofJwt.verify(verifier);
        } catch (JOSEException exception) {
            throw new ProofJwtValidationException(
                    String.format("Error verifying signature: %s", exception.getMessage()),
                    exception);
        }
    }

    /**
     * Extracts data from the verified Proof JWT.
     *
     * <p>This method is called after successful verification to extract the relevant information
     * from the JWT into a structured object.
     *
     * @param proofJwt The verified Proof JWT
     * @param publicKey The public key from the did:key
     * @return ProofJwtData containing the extracted information
     * @throws ProofJwtValidationException On error parsing the JWT claims
     */
    private static ProofJwtData extractProofJwtData(SignedJWT proofJwt, ECPublicKey publicKey)
            throws ProofJwtValidationException {
        try {
            JWSHeader header = proofJwt.getHeader();
            JWTClaimsSet claimsSet = proofJwt.getJWTClaimsSet();
            return new ProofJwtData(header.getKeyID(), claimsSet.getStringClaim(NONCE), publicKey);
        } catch (ParseException exception) {
            throw new ProofJwtValidationException(
                    String.format("Error extracting Proof JWT data: %s", exception.getMessage()),
                    exception);
        }
    }
}
