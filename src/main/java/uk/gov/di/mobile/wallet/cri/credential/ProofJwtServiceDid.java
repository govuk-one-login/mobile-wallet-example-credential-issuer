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

import java.security.NoSuchAlgorithmException;
import java.security.interfaces.ECPublicKey;
import java.security.spec.InvalidKeySpecException;
import java.util.Arrays;
import java.util.HashSet;
import java.util.Set;

public class ProofJwtServiceDid {

    private static final String CLIENT_CONFIG_ALGORITHM = "ES256";
    private static final String CLIENT_CONFIG_ISSUER = "urn:fdc:gov:uk:wallet";
    private static final String CLIENT_CONFIG_AUDIENCE = "urn:fdc:gov:uk:example-credential-issuer";

    public ProofJwtServiceDid() {}

    /**
     * Verifies the Proof JWT's header and payload claims and its signature.
     *
     * @param signedJwt The Proof JWT to verify
     * @throws ProofJwtValidationException On any error verifying the token claims and signature
     */
    public void verifyProofJwt(SignedJWT signedJwt) throws ProofJwtValidationException {
        verifyTokenHeader(signedJwt);
        verifyTokenClaims(signedJwt);
        if (!this.verifyTokenSignature(signedJwt)) {
            throw new ProofJwtValidationException("Proof JWT signature verification failed");
        }
    }

    /**
     * Verifies that the required header claims are present and/or match an expected value.
     *
     * @param signedJwt The Proof JWT to validate
     * @throws ProofJwtValidationException On invalid header claims
     */
    private void verifyTokenHeader(SignedJWT signedJwt) throws ProofJwtValidationException {
        JWSAlgorithm clientAlgorithm =
                JWSAlgorithm.parse(ProofJwtServiceDid.CLIENT_CONFIG_ALGORITHM);
        JWSAlgorithm jwtAlgorithm = signedJwt.getHeader().getAlgorithm();
        if (jwtAlgorithm != clientAlgorithm) {
            throw new ProofJwtValidationException(
                    String.format(
                            "JWT alg header claim [%s] does not match client config alg [%s]",
                            jwtAlgorithm, clientAlgorithm));
        }

        String keyId = signedJwt.getHeader().getKeyID();
        if (keyId == null) {
            throw new ProofJwtValidationException("JWT kid header claim is null");
        }
    }

    /**
     * Verifies that the required payload claims are present and/or match an expected value.
     *
     * @param signedJwt The Proof JWT to validate
     * @throws ProofJwtValidationException On invalid payload claims
     */
    private void verifyTokenClaims(SignedJWT signedJwt) throws ProofJwtValidationException {
        Set<String> requiredClaims = new HashSet<>(Arrays.asList("iat", "nonce"));
        JWTClaimsSet expectedClaimValues =
                new JWTClaimsSet.Builder()
                        .issuer(CLIENT_CONFIG_ISSUER)
                        .audience(CLIENT_CONFIG_AUDIENCE)
                        .build();

        try {
            JWTClaimsSet jwtClaimsSet = signedJwt.getJWTClaimsSet();
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
     * @param signedJwt The Proof JWT to verify
     * @throws ProofJwtValidationException On error verifying the token signature
     */
    private boolean verifyTokenSignature(SignedJWT signedJwt) throws ProofJwtValidationException {
        String didKey = signedJwt.getHeader().getKeyID();
        try {
            DidKeyResolver didKeyResolver = new DidKeyResolver();
            DidKeyResolver.DecodedKeyData resolvedDidKey = didKeyResolver.decodeDidKey(didKey);
            byte[] rawPublicKeyBytes = resolvedDidKey.rawPublicKeyBytes();
            ECPublicKey publicKey = didKeyResolver.generatePublicKeyFromBytes(rawPublicKeyBytes);
            ECKey ecKey = new ECKey.Builder(Curve.P_256, publicKey).build();
            ECDSAVerifier verifier = new ECDSAVerifier(ecKey);
            return signedJwt.verify(verifier);
        } catch (JOSEException
                | IllegalArgumentException
                | NoSuchAlgorithmException
                | InvalidKeySpecException
                | AddressFormatException
                | InvalidDidKeyException exception) {
            throw new ProofJwtValidationException(
                    String.format("Error verifying signature: %s", exception.getMessage()),
                    exception);
        }
    }
}
