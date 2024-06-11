package uk.gov.di.mobile.wallet.cri.did_key;

import com.nimbusds.jose.jwk.Curve;
import com.nimbusds.jose.jwk.ECKey;
import org.bouncycastle.asn1.x9.ECNamedCurveTable;
import org.bouncycastle.asn1.x9.X9ECParameters;
import org.bouncycastle.math.ec.ECCurve;
import org.bouncycastle.math.ec.ECPoint;
import org.bouncycastle.util.encoders.Hex;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import uk.gov.di.mobile.wallet.cri.credential_spike.CredentialService;

import javax.management.openmbean.InvalidKeyException;

import java.math.BigInteger;
import java.security.*;
import java.security.interfaces.ECPublicKey;
import java.security.spec.ECGenParameterSpec;

import static com.nimbusds.jose.JWSAlgorithm.ES256;

public class KeyWriter {

    private static final Logger LOGGER = LoggerFactory.getLogger(CredentialService.class);

    /**
     * Compresses a public key of type PublicKey and returns it as an array of bytes.
     *
     * <p>The public key is an EC point, that is a pair of integer coordinates {x, y}, laying on the
     * curve. It can be compressed to just one of the coordinates {x} + 1 bit (parity). In other
     * words, the compressed public key stores whether the y-coordinate is even or odd and the
     * x-coordinate:
     * <li>1 byte: the indicator of the y coordinate value (equal to 02 if the y coordinate of the
     *     public point is even, or 03 if it is odd)
     * <li>32 bytes: the x coordinate of the public point
     *
     * @return Compressed public key (257-bit integer) in byte array format (33 bytes)
     */
    public static byte[] getCompressedPublicKey(ECPublicKey publicKey) {
        LOGGER.debug("Public key BEFORE compression: {}", publicKey);
        LOGGER.debug(
                "Public key JWK BEFORE compression: {}",
                new ECKey.Builder(Curve.P_256, publicKey).algorithm(ES256).build());

        // get x-coordinate
        byte[] x = publicKey.getW().getAffineX().toByteArray();
        // get y-coordinate
        byte[] y = publicKey.getW().getAffineY().toByteArray();

        // convert x-coordinate to big integer
        BigInteger xbi = new BigInteger(1, x);
        // convert y-coordinate to big integer
        BigInteger ybi = new BigInteger(1, y);

        X9ECParameters x9 = ECNamedCurveTable.getByName("secp256r1");
        ECCurve curve = x9.getCurve();
        ECPoint point = curve.createPoint(xbi, ybi);

        byte[] publicKeyCompressed = point.getEncoded(true);
        LOGGER.debug(
                "getCompressedPublicKey public key hex encoded: {}",
                Hex.toHexString(publicKeyCompressed));
        return publicKeyCompressed;
    }

    /**
     * Compresses a public key of type PublicKey and returns it as an array of bytes without
     * external libraries. Requires calculating the compressed key prefix based on the value of y
     * coordinate and prepending it to the x coordinate.
     *
     * @return Compressed public key (257-bit integer) in byte array format (33 bytes)
     */
    public static byte[] getCompressedPublicKeyManually(ECPublicKey publicKey) {
        // get x-coordinate
        byte[] x = publicKey.getW().getAffineX().toByteArray();
        // get y-coordinate
        byte[] y = publicKey.getW().getAffineY().toByteArray();

        byte[] xTrimmed;
        // remove leading padding zeros if present
        if (x.length == 33 && x[0] == 0) {
            xTrimmed = new byte[x.length - 1];
            System.arraycopy(x, 1, xTrimmed, 0, xTrimmed.length);
        } else if (x.length == 32) {
            xTrimmed = x;
        } else {
            throw new InvalidKeyException("Public key has an invalid length");
        }

        // calculate compressed key prefix
        int prefix = 2 + (y[y.length - 1] & 1); // "(y[y.length - 1] & 1" returns 1 or 0
        byte[] prefixBytes = new byte[1];
        prefixBytes[0] = (byte) prefix;

        byte[] publicKeyCompressed = new byte[xTrimmed.length + 1];

        System.arraycopy(prefixBytes, 0, publicKeyCompressed, 0, 1);
        System.arraycopy(xTrimmed, 0, publicKeyCompressed, prefixBytes.length, xTrimmed.length);

        LOGGER.debug(
                "getCompressedPublicKeyManually public key hex encoded: {}",
                Hex.toHexString(publicKeyCompressed));
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
