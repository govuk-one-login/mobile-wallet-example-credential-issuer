package uk.gov.di.mobile.wallet.cri.credential.mobile_driving_licence.cose;

import uk.gov.di.mobile.wallet.cri.credential.mobile_driving_licence.cose.constants.COSEEllipticCurves;
import uk.gov.di.mobile.wallet.cri.credential.mobile_driving_licence.cose.constants.COSEKeyTypes;

import java.math.BigInteger;
import java.security.interfaces.ECPublicKey;
import java.security.spec.ECParameterSpec;
import java.security.spec.ECPoint;
import java.util.LinkedHashMap;
import java.util.Map;

import static uk.gov.di.mobile.wallet.cri.credential.mobile_driving_licence.cose.BigIntegerToFixedBytes.bigIntegerToFixedBytes;

/**
 * Factory for creating {@link COSEKey} instance from EC public keys.
 *
 * <p>This factory encapsulates the logic for converting a Java {@link ECPublicKey} (specifically on
 * the P-256 curve) into a COSE_Key (CBOR Object Signing and Encryption key) format, as required for
 * mobile security objects.
 */
public class COSEKeyFactory {

    /**
     * Creates a {@link COSEKey} from the given EC public key.
     *
     * <p>This method validates that the provided key uses the P-256 curve, extracts the x and y
     * coordinates, and encodes them as fixed-length byte arrays to construct the {@link COSEKey}
     * object.
     *
     * @param publicKey the EC public key to convert.
     * @return The {@link COSEKey} representation of the public key.
     * @throws IllegalArgumentException If the key does not use the P-256 curve.
     */
    public COSEKey fromECPublicKey(ECPublicKey publicKey) {
        // Validate curve is P-256
        ECParameterSpec params = publicKey.getParams();
        int curveSizeBits = params.getCurve().getField().getFieldSize();
        if (curveSizeBits != 256) {
            throw new IllegalArgumentException("Invalid key curve - expected P-256");
        }

        // Extract x and y coordinates
        ECPoint point = publicKey.getW();
        BigInteger x = point.getAffineX();
        BigInteger y = point.getAffineY();

        // Convert BigInteger to fixed-length byte array
        byte[] xBytes = bigIntegerToFixedBytes(x, curveSizeBits);
        byte[] yBytes = bigIntegerToFixedBytes(y, curveSizeBits);

        // Build the COSE key map
        Map<Integer, Object> coseKeyMap = new LinkedHashMap<>();
        coseKeyMap.put(1, COSEKeyTypes.EC2); // Key type: EC2
        coseKeyMap.put(-1, COSEEllipticCurves.P256); // Curve: P-256
        coseKeyMap.put(-2, xBytes); // x-coordinate
        coseKeyMap.put(-3, yBytes); // y-coordinate

        return new COSEKey(coseKeyMap);
    }
}
