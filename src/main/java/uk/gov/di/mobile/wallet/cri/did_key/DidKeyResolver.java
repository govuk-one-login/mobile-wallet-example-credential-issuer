package uk.gov.di.mobile.wallet.cri.did_key;

import org.bitcoinj.core.Base58;
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
    public DecodedData decodeDIDKey(@NotNull String didKey) throws InvalidDidKeyException {
        String multibaseValue = extractMultibaseFromDidKey(didKey);
        byte[] multicodec = Base58.decode(multibaseValue);

        return extractMulticodecValue(multicodec);
    }

    private static @NotNull String extractMultibaseFromDidKey(@NotNull String didKey)
            throws InvalidDidKeyException {
        // Validate the did key starts with did:key:
        if (!didKey.startsWith("did:key:")) {
            throw new InvalidDidKeyException("Expected key to start with prefix did:key:");
        }

        // Validate multibase type is 'z'
        String[] segments = didKey.split(":");
        String multibase = segments[segments.length - 1];
        char multibaseType = multibase.charAt(0);
        if (multibaseType != 'z') {
            // DID keys only support base58-btc encoding, which is informed by z character as first
            // character
            throw new InvalidDidKeyException(
                    "DID Keys need to be encoded in base58-btc encoding, "
                            + "but found multibase type "
                            + multibaseType);
        }

        // Remove z character from multibase and return
        return multibase.substring(1);
    }

    private static DecodedData extractMulticodecValue(byte[] keyBytes)
            throws InvalidDidKeyException {
        String hex = HexUtils.bytesToHex(keyBytes);
        // Have to iterate through every potential multicodec this could be
        for (Multicodec c : Multicodec.values()) {
            // We need to match the unigned varint value of the multicodec header, not the actual
            // value!
            if (hex.startsWith(c.uvarintcode)) {
                // Extract the actual public key by removing the multicodec header
                String keyHex = hex.substring(c.uvarintcode.length());
                byte[] keyHexBytes = HexFormat.of().parseHex(keyHex);
                assertPublicKeyLengthIsExpected(c, keyHexBytes);

                return new DecodedData(
                        c, keyHexBytes, Base64.getUrlEncoder().encodeToString(keyHexBytes));
            }
        }

        throw new InvalidDidKeyException("DID key multicodec value is not supported");
    }

    private static void assertPublicKeyLengthIsExpected(Multicodec c, byte[] keyHexBytes)
            throws InvalidDidKeyException {
        if (c.expectedKeyLength != -1 && keyHexBytes.length != c.expectedKeyLength) {
            throw new InvalidDidKeyException(
                    "Expected key length of: "
                            + c.expectedKeyLength
                            + ", but found: "
                            + keyHexBytes.length);
        }
    }

    /**
     * Creates an EC public key from a compressed key
     *
     * @param compressedPublicKey Compressed public key in byte array format
     * @return The public key as ECPublicKey object
     * @throws NoSuchAlgorithmException On error creating a key factory with an invalid algorithm
     * @throws InvalidKeySpecException On error generating the public key
     */
    public ECPublicKey generatePublicKeyFromBytes(byte[] compressedPublicKey)
            throws NoSuchAlgorithmException, InvalidKeySpecException {
        if (compressedPublicKey.length != 33
                || compressedPublicKey[0] != 2 && compressedPublicKey[0] != 3) {
            throw new IllegalArgumentException();
        }

        ECNamedCurveParameterSpec spec = ECNamedCurveTable.getParameterSpec("secp256r1");
        byte[] publicKeyUncompressed = uncompressKey(compressedPublicKey, spec);

        ECNamedCurveSpec params =
                new ECNamedCurveSpec("secp256r1", spec.getCurve(), spec.getG(), spec.getN());
        ECPoint point = ECPointUtil.decodePoint(params.getCurve(), publicKeyUncompressed);
        ECPublicKeySpec publicKeySpec = new ECPublicKeySpec(point, params);

        KeyFactory keyFactory = KeyFactory.getInstance("EC");
        return (ECPublicKey) keyFactory.generatePublic(publicKeySpec);
    }

    /**
     * Uncompresses a compressed public key
     *
     * @param compressedPublicKey Compressed public key in byte array format
     * @return Uncompressed public key in byte array format
     */
    private static byte[] uncompressKey(
            byte[] compressedPublicKey, ECNamedCurveParameterSpec spec) {
        return spec.getCurve().decodePoint(compressedPublicKey).getEncoded(false);
    }
}
