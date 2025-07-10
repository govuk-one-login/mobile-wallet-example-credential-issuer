package uk.gov.di.mobile.wallet.cri.credential;

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
import uk.gov.di.mobile.wallet.cri.credential.did_key.DidKeyResolver;
import uk.gov.di.mobile.wallet.cri.credential.did_key.InvalidDidKeyException;
import uk.gov.di.mobile.wallet.cri.services.ConfigurationService;

import java.security.NoSuchAlgorithmException;
import java.security.interfaces.ECPublicKey;
import java.security.spec.InvalidKeySpecException;
import java.text.ParseException;
import java.util.Arrays;
import java.util.HashSet;

public class ProofJwtService {

    public static final String NONCE = "nonce";
    private static final JWSAlgorithm EXPECTED_SIGNING_ALGORITHM = JWSAlgorithm.parse("ES256");
    private static final String EXPECTED_ISSUER = "urn:fdc:gov:uk:wallet";

    private final ConfigurationService configurationService;

    public record ProofJwtData(String didKey, String nonce, ECPublicKey publicKey) {}

    public ProofJwtService(ConfigurationService configurationService) {
        this.configurationService = configurationService;
    }

    /**
     * Verifies the Proof JWT header and payload claims and its signature.
     *
     * @param proofJwt The Proof JWT to verify
     * @return ProofJwtData
     * @throws ProofJwtValidationException On any error verifying the token claims and signature
     */
    public ProofJwtData verifyProofJwt(SignedJWT proofJwt) throws ProofJwtValidationException {
        verifyTokenHeader(proofJwt);
        verifyTokenClaims(proofJwt);

        ECPublicKey publicKey = getPublicKey(proofJwt.getHeader().getKeyID());
        if (!this.verifyTokenSignature(proofJwt, publicKey)) {
            throw new ProofJwtValidationException("Proof JWT signature verification failed");
        }

        return extractProofJwtData(proofJwt, publicKey);
    }

    /**
     * Verifies that the required header claims are present and/or match an expected value.
     *
     * @param proofJwt The Proof JWT to validate
     * @throws ProofJwtValidationException On invalid header claims
     */
    private void verifyTokenHeader(SignedJWT proofJwt) throws ProofJwtValidationException {

        JWSAlgorithm jwtAlgorithm = proofJwt.getHeader().getAlgorithm();
        if (jwtAlgorithm != EXPECTED_SIGNING_ALGORITHM) {
            throw new ProofJwtValidationException(
                    String.format(
                            "JWT alg header claim [%s] does not match client config alg [%s]",
                            jwtAlgorithm, EXPECTED_SIGNING_ALGORITHM));
        }

        if (proofJwt.getHeader().getKeyID() == null) {
            throw new ProofJwtValidationException("JWT kid header claim is null");
        }

        JOSEObjectType typ = proofJwt.getHeader().getType();
        if (typ == null) {
            throw new ProofJwtValidationException("JWT type header claim is null");
        }
        if (!"openid4vci-proof+jwt".equals(typ.toString())) {
            throw new ProofJwtValidationException("JWT type header claim is invalid");
        }
    }

    /**
     * Verifies that the required payload claims are present and/or match an expected value.
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

    private ECPublicKey getPublicKey(String didKey) throws ProofJwtValidationException {
        try {
            DidKeyResolver didKeyResolver = new DidKeyResolver();
            DidKeyResolver.DecodedKeyData resolvedDidKey = didKeyResolver.decodeDidKey(didKey);
            byte[] rawPublicKeyBytes = resolvedDidKey.rawPublicKeyBytes();
            return didKeyResolver.generatePublicKeyFromBytes(rawPublicKeyBytes);
        } catch (IllegalArgumentException
                | NoSuchAlgorithmException
                | InvalidKeySpecException
                | InvalidDidKeyException exception) {
            throw new ProofJwtValidationException(
                    String.format(
                            "Error getting public key from did:key: %s", exception.getMessage()),
                    exception);
        }
    }

    /**
     * Verifies the Proof JWT signature with the public key extracted from the did:key included in
     * the token's "kid" header claim.
     *
     * @param proofJwt The Proof JWT to verify
     * @throws ProofJwtValidationException On error verifying the token signature
     */
    private boolean verifyTokenSignature(SignedJWT proofJwt, ECPublicKey publicKey)
            throws ProofJwtValidationException {
        try {
            ECKey ecKey = new ECKey.Builder(Curve.P_256, publicKey).build();
            ECDSAVerifier verifier = new ECDSAVerifier(ecKey);
            return proofJwt.verify(verifier);
        } catch (JOSEException | IllegalArgumentException exception) {
            throw new ProofJwtValidationException(
                    String.format("Error verifying signature: %s", exception.getMessage()),
                    exception);
        }
    }

    private static ProofJwtData extractProofJwtData(SignedJWT proofJwt, ECPublicKey publicKey)
            throws ProofJwtValidationException {
        try {
            JWSHeader header = proofJwt.getHeader();
            JWTClaimsSet claimsSet = proofJwt.getJWTClaimsSet();
            return new ProofJwtData(header.getKeyID(), claimsSet.getStringClaim(NONCE), publicKey);
        } catch (ParseException exception) {
            throw new ProofJwtValidationException(exception.getMessage(), exception);
        }
    }
}
