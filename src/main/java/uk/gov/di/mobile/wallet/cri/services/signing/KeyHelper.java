package uk.gov.di.mobile.wallet.cri.services.signing;

import org.apache.hc.client5.http.utils.Hex;

import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;

public class KeyHelper {

    private KeyHelper() {
        // Do nothing
    }

    public static String hashKeyId(String keyId, String hashingAlgorithm)
            throws NoSuchAlgorithmException {
        MessageDigest messageDigest = MessageDigest.getInstance(hashingAlgorithm);
        return Hex.encodeHexString(messageDigest.digest(keyId.getBytes(StandardCharsets.UTF_8)));
    }
}
