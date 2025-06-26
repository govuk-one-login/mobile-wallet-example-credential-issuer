package uk.gov.di.mobile.wallet.cri.util;

import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;

public class HashUtil {

    public static byte[] getHashSha256(String value) {

        MessageDigest digest;
        try {
            digest = MessageDigest.getInstance("SHA-256");
        } catch (NoSuchAlgorithmException exception) {
            throw new RuntimeException(exception);
        }
        return digest.digest(value.getBytes(StandardCharsets.UTF_8));
    }

    public static byte[] getHashSha256(byte[] value) {

        MessageDigest digest;
        try {
            digest = MessageDigest.getInstance("SHA-256");
        } catch (NoSuchAlgorithmException exception) {
            throw new RuntimeException(exception);
        }
        return digest.digest(value);
    }

    //  public static String hashSha256String(String value) {
    //
    //    MessageDigest digest;
    //    try {
    //      digest = MessageDigest.getInstance("SHA-256");
    //    } catch (NoSuchAlgorithmException exception) {
    //      throw new RuntimeException(exception);
    //    }
    //    return (Hex.encodeHexString(digest.digest(value.getBytes(StandardCharsets.UTF_8))));
    //  }
}
