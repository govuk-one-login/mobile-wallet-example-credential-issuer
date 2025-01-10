package uk.gov.di.mobile.wallet.cri.credential;

import com.nimbusds.jose.JOSEException;
import com.nimbusds.jose.JWSAlgorithm;
import com.nimbusds.jose.crypto.ECDSAVerifier;
import com.nimbusds.jose.jwk.Curve;
import com.nimbusds.jose.jwk.ECKey;
import com.nimbusds.jwt.JWTClaimsSet;
import com.nimbusds.jwt.SignedJWT;
import com.nimbusds.jwt.proc.BadJWTException;
import com.nimbusds.jwt.proc.DefaultJWTClaimsVerifier;
import uk.gov.di.mobile.wallet.cri.services.ConfigurationService;

import java.security.NoSuchAlgorithmException;
import java.security.interfaces.ECPublicKey;
import java.security.spec.InvalidKeySpecException;
import java.util.Arrays;
import java.util.HashSet;
import java.util.Set;

public class ProofJwtService {

    private static final String PROOF_JWT_ALGORITHM = "ES256";
    private static final String PROOF_JWT_ISSUER = "urn:fdc:gov:uk:wallet";
    private final ConfigurationService configurationService;

    public ProofJwtService(ConfigurationService configurationService) {
        this.configurationService = configurationService;
    }

    /**
     * Verifies the Proof JWT's header and payload claims and its signature.
     *
     * @param proofJwt The Proof JWT to verify
     * @throws ProofJwtValidationException On any error verifying the token claims and signature
     */
    public void verifyProofJwt(SignedJWT proofJwt) throws ProofJwtValidationException {
        verifyTokenHeader(proofJwt);
        verifyTokenClaims(proofJwt);
        if (!this.verifyTokenSignature(proofJwt)) {
            throw new ProofJwtValidationException("Proof JWT signature verification failed");
        }
    }

    /**
     * Verifies that the required header claims are present and/or match an expected value.
     *
     * @param proofJwt The Proof JWT to validate
     * @throws ProofJwtValidationException On invalid header claims
     */
    private void verifyTokenHeader(SignedJWT proofJwt) throws ProofJwtValidationException {
        JWSAlgorithm clientAlgorithm = JWSAlgorithm.parse(ProofJwtService.PROOF_JWT_ALGORITHM);
        JWSAlgorithm jwtAlgorithm = proofJwt.getHeader().getAlgorithm();
        if (jwtAlgorithm != clientAlgorithm) {
            throw new ProofJwtValidationException(
                    String.format(
                            "JWT alg header claim [%s] does not match client config alg [%s]",
                            jwtAlgorithm, clientAlgorithm));
        }

        String keyId = proofJwt.getHeader().getKeyID();
        if (keyId == null) {
            throw new ProofJwtValidationException("JWT kid header claim is null");
        }
    }

    /**
     * Verifies that the required payload claims are present and/or match an expected value.
     *
     * @param proofJwt The Proof JWT to validate
     * @throws ProofJwtValidationException On invalid payload claims
     */
    private void verifyTokenClaims(SignedJWT proofJwt) throws ProofJwtValidationException {
        Set<String> requiredClaims = new HashSet<>(Arrays.asList("iat", "nonce"));
        JWTClaimsSet expectedClaimValues =
                new JWTClaimsSet.Builder()
                        .issuer(PROOF_JWT_ISSUER)
                        .audience(configurationService.getSelfUrl())
                        .build();

        try {
            JWTClaimsSet jwtClaimsSet = proofJwt.getJWTClaimsSet();
            DefaultJWTClaimsVerifier<?> verifier =
                    new DefaultJWTClaimsVerifier<>(expectedClaimValues, requiredClaims);
            verifier.verify(jwtClaimsSet, null);
        } catch (BadJWTException | java.text.ParseException exception) {
            throw new ProofJwtValidationException(exception.getMessage(), exception);
        }
    }

    /**
     * Verifies the Proof JWT signature with the public key extracted from the did:key included in
     * the token's "kid" header claim.
     *
     * @param proofJwt The Proof JWT to verify
     * @throws ProofJwtValidationException On error verifying the token signature
     */
    private boolean verifyTokenSignature(SignedJWT proofJwt) throws ProofJwtValidationException {
        String didKey = proofJwt.getHeader().getKeyID();
        try {
            DidKeyResolver didKeyResolver = new DidKeyResolver();
            DidKeyResolver.DecodedKeyData resolvedDidKey = didKeyResolver.decodeDidKey(didKey);
            byte[] rawPublicKeyBytes = resolvedDidKey.rawPublicKeyBytes();
            ECPublicKey publicKey = didKeyResolver.generatePublicKeyFromBytes(rawPublicKeyBytes);
            ECKey ecKey = new ECKey.Builder(Curve.P_256, publicKey).build();
            ECDSAVerifier verifier = new ECDSAVerifier(ecKey);
            return proofJwt.verify(verifier);
        } catch (JOSEException
                | IllegalArgumentException
                | NoSuchAlgorithmException
                | InvalidKeySpecException
                | InvalidDidKeyException exception) {
            throw new ProofJwtValidationException(
                    String.format("Error verifying signature: %s", exception.getMessage()),
                    exception);
        }
    }
}
