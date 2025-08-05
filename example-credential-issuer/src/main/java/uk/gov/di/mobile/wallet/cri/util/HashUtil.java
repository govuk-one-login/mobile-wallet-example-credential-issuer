package uk.gov.di.mobile.wallet.cri.util;

import org.apache.hc.client5.http.utils.Hex;
import uk.gov.di.mobile.wallet.cri.annotations.ExcludeFromGeneratedCoverageReport;

import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;

public final class HashUtil {

    private static final String SHA_256 = "SHA-256";

    @ExcludeFromGeneratedCoverageReport
    private HashUtil() {
        throw new IllegalStateException("Instantiation is not valid for this class.");
    }

    /**
     * Creates a SHA-256 MessageDigest instance.
     *
     * @return MessageDigest instance for SHA-256
     * @throws IllegalStateException if SHA-256 algorithm is not available
     */
    private static MessageDigest createSha256Digest() {
        try {
            return MessageDigest.getInstance(SHA_256);
        } catch (NoSuchAlgorithmException exception) {
            throw new IllegalStateException("SHA-256 algorithm not available", exception);
        }
    }

    /**
     * Computes SHA-256 hash of the given string using UTF-8 encoding.
     *
     * @param value the string to hash
     * @return SHA-256 hash as byte array
     * @throws IllegalArgumentException if value is null
     */
    public static byte[] sha256(String value) {
        if (value == null) {
            throw new IllegalArgumentException("Input string cannot be null");
        }
        return createSha256Digest().digest(value.getBytes(StandardCharsets.UTF_8));
    }

    /**
     * Computes SHA-256 hash of the given byte array.
     *
     * @param value the byte array to hash
     * @return SHA-256 hash as byte array
     * @throws IllegalArgumentException if value is null
     */
    public static byte[] sha256(byte[] value) {
        if (value == null) {
            throw new IllegalArgumentException("Input byte array cannot be null");
        }
        return createSha256Digest().digest(value);
    }

    /**
     * Computes SHA-256 hash of the given string and returns it as a hexadecimal string.
     *
     * @param value the string to hash
     * @return SHA-256 hash as lowercase hexadecimal string
     * @throws IllegalArgumentException if value is null
     */
    public static String sha256Hex(String value) {
        return Hex.encodeHexString(sha256(value));
    }

    /**
     * Computes SHA-256 hash of the given byte array and returns it as a hexadecimal string.
     *
     * @param value the byte array to hash
     * @return SHA-256 hash as lowercase hexadecimal string
     * @throws IllegalArgumentException if value is null
     */
    public static String sha256Hex(byte[] value) {
        return Hex.encodeHexString(sha256(value));
    }
}
