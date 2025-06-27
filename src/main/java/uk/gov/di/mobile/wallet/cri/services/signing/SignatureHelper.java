package uk.gov.di.mobile.wallet.cri.services.signing;

import com.nimbusds.jose.JOSEException;
import com.nimbusds.jose.JWSAlgorithm;
import com.nimbusds.jose.crypto.impl.ECDSA;
import software.amazon.awssdk.services.kms.model.SignResponse;

import java.util.Base64;

public final class SignatureHelper {
    private static final JWSAlgorithm SIGNING_ALGORITHM = JWSAlgorithm.ES256;

    /**
     * Returns the encoded signature as a Base64URL-encoded string.
     *
     * @param signResult The signing result containing the signature bytes.
     * @return The encoded signature as a string.
     * @throws JOSEException If the signature is invalid or the algorithm is not supported.
     */
    public static String toBase64UrlEncodedSignature(SignResponse signResult) throws JOSEException {
        return Base64.getUrlEncoder()
                .withoutPadding()
                .encodeToString(
                        ECDSA.transcodeSignatureToConcat(
                                signResult.signature().asByteArray(),
                                ECDSA.getSignatureByteArrayLength(SIGNING_ALGORITHM)));
    }

    /**
     * Returns the signature as raw bytes.
     *
     * @param signResult The signing result containing the signature bytes.
     * @return The signature as a byte array.
     */
    public static byte[] getSignatureAsBytes(SignResponse signResult) {
        return signResult.signature().asByteArray();
    }
}
