package uk.gov.di.mobile.wallet.cri.credential.jwt.did_key;

import org.bouncycastle.jce.ECNamedCurveTable;
import org.bouncycastle.jce.ECPointUtil;
import org.bouncycastle.jce.spec.ECNamedCurveParameterSpec;
import org.bouncycastle.jce.spec.ECNamedCurveSpec;
import org.jetbrains.annotations.NotNull;
import uk.gov.di.mobile.wallet.cri.credential.jwt.did_key.exceptions.AddressFormatException;
import uk.gov.di.mobile.wallet.cri.credential.jwt.did_key.exceptions.InvalidDidKeyException;

import java.security.KeyFactory;
import java.security.NoSuchAlgorithmException;
import java.security.interfaces.ECPublicKey;
import java.security.spec.ECPoint;
import java.security.spec.ECPublicKeySpec;
import java.security.spec.InvalidKeySpecException;
import java.util.Base64;
import java.util.HexFormat;

public class DidKeyResolver {

    @SuppressWarnings("java:S6218")
    public record DecodedKeyData(
            /*
            "Equals method should be overridden in records containing array fields java:S6218"
            Overriding the equals method is not required as byte[] rawPublicKeyBytes is created within this class
            and can only contain bytes
            */
            Multicodec multicodecValue, byte[] rawPublicKeyBytes, String publicKeyBase64) {}

    /**
     * Decodes the given did:key into a response object containing the public key algorithm, and the
     * contents of the public key.
     *
     * <p>A did:key is structured such that:
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
     * @param didKey The did:key to resolve
     * @return A response containing the public key algorithm, and the public key material
     * @throws InvalidDidKeyException On error resolving the did:key
     */
    public DecodedKeyData decodeDidKey(@NotNull String didKey)
            throws InvalidDidKeyException, AddressFormatException {
        // get fingerprint/multibase
        String multibase = removePrefixAndMultibaseCode(didKey);
        // base58 decode the key
        byte[] multicodec = Base58.decode(multibase);

        return extractPublicKey(multicodec);
    }

    /**
     * Checks that key has prefix "did:key:" followed by the letter "z" from the base58 encoding.
     * Then removes both the prefix "did:key:" and the multibase code "z" from the key and returns
     * it.
     *
     * @param didKey The did key
     * @return The base58 encoded key without the prefix "did:key:z"
     * @throws InvalidDidKeyException On error validating the did:key
     */
    private static @NotNull String removePrefixAndMultibaseCode(@NotNull String didKey)
            throws InvalidDidKeyException {
        // validate did:key prefix
        if (!didKey.startsWith("did:key:")) {
            throw new InvalidDidKeyException("Expected did:key to start with prefix did:key:");
        }

        // did:key must be base58 encoded, which is informed by the z character as first character
        String[] segments = didKey.split(":");
        String multibase = segments[segments.length - 1];
        char multibaseType = multibase.charAt(0);
        if (multibaseType != 'z') {
            throw new InvalidDidKeyException(
                    String.format(
                            "did:key must be base58 encoded but found multibase code %s instead",
                            multibaseType));
        }

        // skip leading "z" from the base58 encoding
        return multibase.substring(1);
    }

    /**
     * Extracts the public key material from the function input.
     *
     * @param keyBytes The multicodec identifier for the public key type and the public key material
     * @return DecodedKeyData
     * @throws InvalidDidKeyException On error validating the did:key
     */
    private static DecodedKeyData extractPublicKey(byte[] keyBytes) throws InvalidDidKeyException {
        String hex = HexUtils.bytesToHex(keyBytes);
        Multicodec multicodec = Multicodec.P256_PUB;

        byte[] keyHexBytes = removeMulticodec(hex, multicodec);

        // check if key is compressed by checking that its length is 33 bytes and the first byte is
        // either 0x02 (2) or 0x03 (3)
        assertPublicKeyIsCompressed(multicodec, keyHexBytes);

        return new DecodedKeyData(
                multicodec, keyHexBytes, Base64.getUrlEncoder().encodeToString(keyHexBytes));
    }

    /**
     * Validates that key starts with the expected unsigned varint of the multicodec and then
     * removes it, returning only the public key material.
     *
     * @param multicodec The key's multicodec value
     * @param hexKey The multicodec identifier for the public key type and the public key material
     *     hex encoded
     * @return The public key material in byte array format
     * @throws InvalidDidKeyException On an invalid multicodec
     */
    private static byte[] removeMulticodec(String hexKey, Multicodec multicodec)
            throws InvalidDidKeyException {
        // check if hex encoded public key starts with the unsigned varint of the multicodec value
        // supported
        if (!hexKey.startsWith(multicodec.uvarintcode)) {
            throw new InvalidDidKeyException("did:key multicodec value is not supported");
        }
        // extract the actual public key by removing the multicodec
        String keyHex = hexKey.substring(multicodec.uvarintcode.length());

        return HexFormat.of().parseHex(keyHex);
    }

    /**
     * Checks that the public key is compressed. A compressed key must be 33 bytes long and have
     * prefix 2 or 3.
     *
     * @param multicodec The key's multicodec value
     * @param keyHexBytes The public key material
     * @throws InvalidDidKeyException On a public key that is not compressed
     */
    private static void assertPublicKeyIsCompressed(Multicodec multicodec, byte[] keyHexBytes)
            throws InvalidDidKeyException {
        if (keyHexBytes.length != multicodec.expectedKeyLength) {
            throw new InvalidDidKeyException(
                    String.format(
                            "Expected key length equal to %s, but found %s instead",
                            multicodec.expectedKeyLength, keyHexBytes.length));
        }

        if (keyHexBytes[0] != 2 && keyHexBytes[0] != 3) {
            throw new InvalidDidKeyException(
                    String.format(
                            "Expected key prefix equal to 2 or 3, but found %s instead",
                            keyHexBytes[0]));
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
