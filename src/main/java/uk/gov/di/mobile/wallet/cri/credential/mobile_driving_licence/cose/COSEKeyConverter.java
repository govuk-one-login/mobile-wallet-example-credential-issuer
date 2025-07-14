package uk.gov.di.mobile.wallet.cri.credential.mobile_driving_licence.cose;

import uk.gov.di.mobile.wallet.cri.credential.mobile_driving_licence.cose.constants.COSEEllipticCurves;
import uk.gov.di.mobile.wallet.cri.credential.mobile_driving_licence.cose.constants.COSEKeyTypes;

import java.math.BigInteger;
import java.security.interfaces.ECPublicKey;
import java.security.spec.ECParameterSpec;
import java.security.spec.ECPoint;

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

    /**
     * Converts a BigInteger to a fixed-length byte array using truncation/padding approach.
     *
     * <p>This method preserves all leading zeros in the BigInteger representation and uses
     * truncation for oversized values (keeping the least significant bytes). This approach is
     * required because exact byte lengths are required in COSE keys.
     *
     * <p><strong>Handling scenarios:</strong>
     *
     * <ul>
     *   <li><strong>Exact match:</strong> Returns the array as-is
     *   <li><strong>Too long:</strong> Takes the rightmost bytes (truncates from left)
     *   <li><strong>Too short:</strong> Pads with leading zeros (big-endian format)
     * </ul>
     *
     * <p><strong>Example:</strong>
     *
     * <pre>{@code
     * // P-256 curve coordinate (32 bytes needed)
     * BigInteger coord = new BigInteger("AB12", 16);
     * byte[] result = bigIntegerToFixedBytes(coord, 256);
     * // Result: [0x00, 0x00, ..., 0xAB, 0x12] (32 bytes total)
     * }</pre>
     *
     * @param value The BigInteger to convert to a byte array
     * @param curveSizeBits The curve size in bits (will be converted to bytes using (bits + 7) / 8)
     * @return A byte array of exactly the calculated byte length representing the BigInteger value
     */
    private static byte[] bigIntegerToFixedBytes(BigInteger value, int curveSizeBits) {
        // Calculate the number of bytes needed for this curve size
        // The (curveSizeBits + 7) / 8 formula rounds up to the next byte boundary
        int targetBytes = (curveSizeBits + 7) / 8;

        // Create the result array of the exact size needed
        byte[] result = new byte[targetBytes];

        // Get the raw byte representation from BigInteger
        byte[] sourceBytes = value.toByteArray();

        if (result.length == sourceBytes.length) {
            // Match - return the BigInteger bytes
            return sourceBytes;
        } else if (sourceBytes.length > result.length) {
            // Source is too long - truncate by taking the rightmost bytes
            // This preserves the least significant bytes (big-endian format)
            System.arraycopy(
                    sourceBytes, sourceBytes.length - result.length, result, 0, result.length);
        } else {
            // Source is too short - pad with leading zeros
            // Copy source bytes to the right side of the result array
            System.arraycopy(
                    sourceBytes, 0, result, result.length - sourceBytes.length, sourceBytes.length);
            // The left side is automatically filled with zeros
        }
        return result;
    }
}
