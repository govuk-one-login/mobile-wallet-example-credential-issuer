package uk.gov.di.mobile.wallet.cri.credential.mobile_driving_licence.cose;

import uk.gov.di.mobile.wallet.cri.credential.mobile_driving_licence.cose.constants.COSEEllipticCurves;
import uk.gov.di.mobile.wallet.cri.credential.mobile_driving_licence.cose.constants.COSEKeyTypes;

import java.math.BigInteger;
import java.security.interfaces.ECPublicKey;
import java.security.spec.ECParameterSpec;
import java.security.spec.ECPoint;

import static uk.gov.di.mobile.wallet.cri.credential.mobile_driving_licence.cose.BigIntegerToFixedBytes.bigIntegerToFixedBytes;

/**
 * Utility class for converting EC public keys to COSE key format.
 *
 * <p>This class provides methods to convert Java's {@link ECPublicKey} objects into COSE_Key (CBOR
 * Object Signing and Encryption key) format, which is required for mobile security objects.
 */
public class COSEKeyConverter {

    /**
     * Converts an EC public key to COSE key format.
     *
     * <p>This method validates that the provided key uses the P-256 curve and extracts the x and y
     * coordinates to create a COSE key.
     *
     * @param publicKey The EC public key to convert. Must use the P-256 curve.
     * @return A {@link COSEKey} object representing the public key in COSE_Key format.
     * @throws IllegalArgumentException If the key does not use the P-256 curve.
     */
    public static COSEKey fromECPublicKey(ECPublicKey publicKey) {
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

        // Convert to bytes
        byte[] xBytes = bigIntegerToFixedBytes(x, curveSizeBits);
        byte[] yBytes = bigIntegerToFixedBytes(y, curveSizeBits);

        return new COSEKeyBuilder()
                .keyType(COSEKeyTypes.EC2)
                .curve(COSEEllipticCurves.P256)
                .xCoordinate(xBytes)
                .yCoordinate(yBytes)
                .build();
    }
}
