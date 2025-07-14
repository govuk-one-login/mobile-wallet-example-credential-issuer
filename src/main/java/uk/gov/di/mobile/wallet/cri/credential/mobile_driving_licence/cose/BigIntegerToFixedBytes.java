package uk.gov.di.mobile.wallet.cri.credential.mobile_driving_licence.cose;

import java.math.BigInteger;

public class BigIntegerToFixedBytes {

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
    public static byte[] bigIntegerToFixedBytes(BigInteger value, int curveSizeBits) {
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
