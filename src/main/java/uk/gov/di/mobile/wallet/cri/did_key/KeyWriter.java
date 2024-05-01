package uk.gov.di.mobile.wallet.cri.did_key;

import com.nimbusds.jose.jwk.Curve;
import com.nimbusds.jose.jwk.ECKey;
import org.bouncycastle.jce.ECNamedCurveTable;
import org.bouncycastle.jce.provider.BouncyCastleProvider;
import org.bouncycastle.jce.spec.ECNamedCurveParameterSpec;

import java.math.BigInteger;
import java.security.*;
import java.security.interfaces.ECPublicKey;

import static com.nimbusds.jose.JWSAlgorithm.ES256;
import static org.bitcoinj.core.ECKey.CURVE;

public class KeyWriter {
    /**
     * Compresses a public key of type PublicKey into an array of bytes
     *
     * @return Compressed public key in byte array format
     */
    public byte[] getCompressedPublicKey(PublicKey publicKey) {
        System.out.println("Public key BEFORE: " + publicKey);

        System.out.println(
                "Public key BEFORE JWK: "
                        + new ECKey.Builder(Curve.P_256, (ECPublicKey) publicKey)
                                .algorithm(ES256)
                                .build());

        BigInteger xCoordinate = ((ECPublicKey) publicKey).getW().getAffineX();
        BigInteger yCoordinate = ((ECPublicKey) publicKey).getW().getAffineY();

        return CURVE.getCurve().createPoint(xCoordinate, yCoordinate).getEncoded(true);
    }

    /**
     * Creates an asymmetric key-pair
     *
     * @return A key-pair
     * @throws NoSuchAlgorithmException On error creating a key-pair generator with an invalid
     *     algorithm
     * @throws InvalidAlgorithmParameterException On error initializing a key-pair generator with an
     *     invalid algorithm
     */
    public KeyPair generateKeyPair()
            throws NoSuchAlgorithmException, InvalidAlgorithmParameterException {
        ECNamedCurveParameterSpec spec = ECNamedCurveTable.getParameterSpec("secp256r1");

        KeyPairGenerator keyPairGenerator =
                KeyPairGenerator.getInstance("EC", new BouncyCastleProvider());
        keyPairGenerator.initialize(spec);
        return keyPairGenerator.generateKeyPair();
    }
}
