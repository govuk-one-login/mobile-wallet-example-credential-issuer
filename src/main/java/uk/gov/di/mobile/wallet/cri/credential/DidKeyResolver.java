package uk.gov.di.mobile.wallet.cri.credential;

import org.bouncycastle.jce.ECNamedCurveTable;
import org.bouncycastle.jce.ECPointUtil;
import org.bouncycastle.jce.spec.ECNamedCurveParameterSpec;
import org.bouncycastle.jce.spec.ECNamedCurveSpec;
import org.jetbrains.annotations.NotNull;

import java.security.KeyFactory;
import java.security.NoSuchAlgorithmException;
import java.security.interfaces.ECPublicKey;
import java.security.spec.ECPoint;
import java.security.spec.ECPublicKeySpec;
import java.security.spec.InvalidKeySpecException;
import java.util.Base64;
import java.util.HexFormat;

public class DidKeyResolver {
    public record DecodedData(
            Multicodec multicodecValue, byte[] rawPublicKeyBytes, String publicKeyBase64) {}

    /**
     * Decodes the given DID key into a response stating the public key algorithm, and the actual
     * contents of the public key.
     *
     * <p>DID keys are structured such that:
     *
     * <ul>
     *   <li>The prefix <i>must be</i> <code>did:key</code>
     *   <li>The multibase of the <code>did:key</code> is the section after <code>did:key:</code>
     *   <li>The first character of the multibase <i>must be</i> <code>z</code>, signifying the key
     *       is base58-btc encoded
     *   <li>The multicodec value is the base58-btc decoded portion <b>after the z character</b>
     *   <li>The multicodec header is the prefix of the multicodec
     *   <li>The multicodec header is unsigned varint encoded. This is done to reduce the size of
     *       the did:key
     * </ul>
     *
     * <p>The multicodec header being unsigned varint encoded explains why the documentation states
     * that e.g. P-256 keys are represented as header <code>0x1200</code>, but when base58-btc
     * decoded are actually represented as <code>0x8024</code>. In this code, we have to compare the
     * base58-btc decoded multicodec to the <b>unsigned varint</b> value of the multicodec header.
     *
     * @param didKey The did key to decode
     * @return A response containing the public key algorithm, and the did key itself
     * @throws InvalidDidKeyException On error decoding the did:key
     */
    public DecodedData decodeDIDKey(@NotNull String didKey)
            throws InvalidDidKeyException, AddressFormatException {
        // get fingerprint/multibase
        String multibase = extractMultibaseFromDidKey(didKey);
        // base58 decode the public key
        byte[] multicodec = Base58.decode(multibase);

        return extractMulticodecValue(multicodec);
    }

    private static @NotNull String extractMultibaseFromDidKey(@NotNull String didKey)
            throws InvalidDidKeyException {
        // validate the did key starts with did:key:
        if (!didKey.startsWith("did:key:")) {
            throw new InvalidDidKeyException("Expected key to start with prefix did:key:");
        }

        // DID keys only support base58 encoding, which is informed by z character as first
        // character: validate that key is base58 encoded by checking it starts with 'z'
        String[] segments = didKey.split(":");
        String multibase = segments[segments.length - 1];
        char multibaseType = multibase.charAt(0);
        if (multibaseType != 'z') {
            throw new InvalidDidKeyException(
                    "DID Keys need to be encoded in base58-btc encoding, "
                            + "but found multibase type "
                            + multibaseType);
        }

        // skip leading "z" from the base58 encoding
        return multibase.substring(1);
    }

    private static DecodedData extractMulticodecValue(byte[] keyBytes)
            throws InvalidDidKeyException {
        String hex = HexUtils.bytesToHex(keyBytes);
        Multicodec multicodec = Multicodec.P256_PUB;

        // check if hex encoded public key starts with the unsigned varint of the multicodec value
        // supported
        if (!hex.startsWith(multicodec.uvarintcode)) {
            throw new InvalidDidKeyException("DID key multicodec value is not supported");
        }
        // extract the actual public key by removing the multicodec
        String keyHex = hex.substring(multicodec.uvarintcode.length());

        byte[] keyHexBytes = HexFormat.of().parseHex(keyHex);

        // check if key length is correct
        assertPublicKeyLengthIsExpected(multicodec, keyHexBytes);

        return new DecodedData(
                multicodec, keyHexBytes, Base64.getUrlEncoder().encodeToString(keyHexBytes));
    }

    private static void assertPublicKeyLengthIsExpected(Multicodec multicodec, byte[] keyHexBytes)
            throws InvalidDidKeyException {
        if (multicodec.expectedKeyLength != -1
                && keyHexBytes.length != multicodec.expectedKeyLength) {
            throw new InvalidDidKeyException(
                    "Expected key length of: "
                            + multicodec.expectedKeyLength
                            + ", but found: "
                            + keyHexBytes.length);
        }
    }

    /**
     * Creates a public key from a compressed key.
     *
     * @param compressedPublicKey Compressed public key in byte array format
     * @return The public key as ECPublicKey object
     * @throws NoSuchAlgorithmException On error creating a key factory with an invalid algorithm
     * @throws InvalidKeySpecException On error generating the public key
     */
    public ECPublicKey generatePublicKeyFromBytes(byte[] compressedPublicKey)
            throws NoSuchAlgorithmException, InvalidKeySpecException {
        // check if key is compressed by checking that its length is 33 bytes and the first byte is
        // either 0x02 or 0x03
        if (compressedPublicKey.length != 33
                || compressedPublicKey[0] != 2 && compressedPublicKey[0] != 3) {
            throw new IllegalArgumentException();
        }

        ECNamedCurveParameterSpec spec = ECNamedCurveTable.getParameterSpec("secp256r1");
        byte[] publicKeyUncompressed = decompressKey(compressedPublicKey, spec);

        ECNamedCurveSpec params =
                new ECNamedCurveSpec("secp256r1", spec.getCurve(), spec.getG(), spec.getN());
        ECPoint point = ECPointUtil.decodePoint(params.getCurve(), publicKeyUncompressed);
        ECPublicKeySpec publicKeySpec = new ECPublicKeySpec(point, params);

        KeyFactory keyFactory = KeyFactory.getInstance("EC");
        return (ECPublicKey) keyFactory.generatePublic(publicKeySpec);
    }

    /**
     * Decompresses a compressed public key.
     *
     * @param compressedPublicKey Compressed public key in byte array format
     * @return Uncompressed public key in byte array format
     */
    private static byte[] decompressKey(
            byte[] compressedPublicKey, ECNamedCurveParameterSpec spec) {
        return spec.getCurve().decodePoint(compressedPublicKey).getEncoded(false);
    }
}
