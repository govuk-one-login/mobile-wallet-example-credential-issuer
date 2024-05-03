package uk.gov.di.mobile.wallet.cri.did_key;

import com.nimbusds.jose.jwk.Curve;
import com.nimbusds.jose.jwk.ECKey;
import org.bouncycastle.asn1.x9.ECNamedCurveTable;
import org.bouncycastle.asn1.x9.X9ECParameters;
import org.bouncycastle.math.ec.ECCurve;
import org.bouncycastle.math.ec.ECPoint;

import java.math.BigInteger;
import java.security.*;
import java.security.interfaces.ECPublicKey;
import java.security.spec.ECGenParameterSpec;

import static com.nimbusds.jose.JWSAlgorithm.ES256;

public class KeyWriter {
    /**
     * Compresses a public key of type PublicKey and returns it as an array of bytes.
     *
     * <p>The public key is an EC point, that is a pair of integer coordinates {x, y}, laying on the
     * curve. It can be compressed to just one of the coordinates {x} + 1 bit (parity). In other
     * words, the compressed public key stores whether the y-coordinate is even or odd and the
     * x-coordinate: - 1 byte: the indicator of the y coordinate value (equal to 02 if the y
     * coordinate of the public point is even, or 03 if it is odd) - 32 bytes: the x coordinate of
     * the public point
     *
     * @return Compressed public key (257-bit integer) in byte array format (33 bytes)
     */
    public static byte[] getCompressedPublicKey(PublicKey publicKey) {
        System.out.println("Public key BEFORE COMPRESSION: " + publicKey);
        System.out.println(
                "Public key JWK: "
                        + new ECKey.Builder(Curve.P_256, (ECPublicKey) publicKey)
                                .algorithm(ES256)
                                .build());

        // parse public key as public key of type EC
        ECPublicKey ecPublicKey = ((ECPublicKey) publicKey);

        // get x-coordinate
        byte[] x = ecPublicKey.getW().getAffineX().toByteArray();
        // get y-coordinate
        byte[] y = ecPublicKey.getW().getAffineY().toByteArray();

        // convert x-coordinate to big integer
        BigInteger xbi = new BigInteger(1, x);
        // convert y-coordinate to big integer
        BigInteger ybi = new BigInteger(1, y);

        X9ECParameters x9 = ECNamedCurveTable.getByName("secp256r1");
        ECCurve curve = x9.getCurve();
        ECPoint point = curve.createPoint(xbi, ybi);

        byte[] publicKeyCompressed = point.getEncoded(true);
        return publicKeyCompressed;
    }

    /**
     * Generates an ECDSA key-pair (256-bit elliptic curve aka secp256r1). - Private key: an 256-bit
     * integer (32 bytes) - generated at random - Public key: a (EC) point on the elliptic curve,
     * calculated by multiplying the private key by the generator point G
     *
     * @return An ECDSA key-pair
     * @throws NoSuchAlgorithmException On error creating a key-pair generator with an invalid
     *     algorithm
     * @throws InvalidAlgorithmParameterException On error initializing a key-pair generator with an
     *     invalid algorithm
     */
    public static KeyPair generateKeyPair()
            throws NoSuchAlgorithmException, InvalidAlgorithmParameterException {

        KeyPairGenerator keyPairGenerator = KeyPairGenerator.getInstance("EC");
        keyPairGenerator.initialize(new ECGenParameterSpec("secp256r1"), new SecureRandom());
        return keyPairGenerator.generateKeyPair();
    }
}
