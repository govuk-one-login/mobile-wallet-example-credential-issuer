package uk.gov.di.mobile.wallet.cri.services.signing;

import lombok.experimental.UtilityClass;
import org.apache.hc.client5.http.utils.Hex;

import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;

@UtilityClass
public class KeyHelper {

    private static final String HASHING_ALGORITHM = "SHA-256";

    public static String hashKeyId(String keyId) throws NoSuchAlgorithmException {
        MessageDigest messageDigest = MessageDigest.getInstance(HASHING_ALGORITHM);
        return Hex.encodeHexString(messageDigest.digest(keyId.getBytes(StandardCharsets.UTF_8)));
    }
}
