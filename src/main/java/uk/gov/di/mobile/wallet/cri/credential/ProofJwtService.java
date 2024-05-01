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
import uk.gov.di.mobile.wallet.cri.did_key.DidKeyResolver;
import uk.gov.di.mobile.wallet.cri.did_key.InvalidDidKeyException;

import java.security.NoSuchAlgorithmException;
import java.security.interfaces.ECPublicKey;
import java.security.spec.InvalidKeySpecException;
import java.util.*;

import static com.nimbusds.jose.JWSAlgorithm.ES256;

public class ProofJwtService {

    private static final String CLIENT_CONFIG_ALGORITHM = "ES256";
    private static final String CLIENT_CONFIG_ISSUER = "urn:fdc:gov:uk:wallet";
    private static final String CLIENT_CONFIG_AUDIENCE = "urn:fdc:gov:uk:example-credential-issuer";

    public ProofJwtService() {}

    public SignedJWT verifyProofJwt(String jwt)
            throws ProofJwtValidationException, InvalidDidKeyException {
        SignedJWT signedJwt = parseJwt(jwt);

        verifyTokenHeader(CLIENT_CONFIG_ALGORITHM, signedJwt);
        verifyTokenClaims(signedJwt);

        if (!this.verifyTokenSignature(signedJwt)) {
            throw new ProofJwtValidationException("Proof JWT signature verification failed");
        }
        return signedJwt;
    }

    private SignedJWT parseJwt(String jwt) throws ProofJwtValidationException {
        try {
            return SignedJWT.parse(jwt);
        } catch (java.text.ParseException exception) {
            throw new ProofJwtValidationException(
                    String.format("Error parsing proof JWT: %s", exception.getMessage()),
                    exception);
        }
    }

    private void verifyTokenHeader(String clientAlgorithmString, SignedJWT signedJwt)
            throws ProofJwtValidationException {
        JWSAlgorithm clientAlgorithm = JWSAlgorithm.parse(clientAlgorithmString);
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

    private boolean verifyTokenSignature(SignedJWT signedJwt)
            throws ProofJwtValidationException, InvalidDidKeyException {
        String didKey = signedJwt.getHeader().getKeyID();
        try {

            DidKeyResolver didKeyResolver = new DidKeyResolver();
            DidKeyResolver.DecodedData resolvedDidKey = didKeyResolver.decodeDIDKey(didKey);

            byte[] rawPublicKeyBytes = resolvedDidKey.rawPublicKeyBytes();

            ECPublicKey publicKey = didKeyResolver.generatePublicKeyFromBytes(rawPublicKeyBytes);
            System.out.println("Public key AFTER: " + publicKey);

            System.out.println(
                    "Public key AFTER JWK: "
                            + new ECKey.Builder(Curve.P_256, publicKey).algorithm(ES256).build());

            ECKey ecKey = new ECKey.Builder(Curve.P_256, publicKey).build();

            ECDSAVerifier verifier = new ECDSAVerifier(ecKey);
            return signedJwt.verify(verifier);
        } catch (JOSEException
                | IllegalArgumentException
                | NoSuchAlgorithmException
                | InvalidKeySpecException exception) {
            throw new ProofJwtValidationException(
                    String.format("Error verifying signature: %s", exception.getMessage()),
                    exception);
        }
    }
}
