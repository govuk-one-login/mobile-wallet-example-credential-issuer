package uk.gov.di.mobile.wallet.cri.credential;

import java.math.BigInteger;
import java.util.ArrayList;
import java.util.List;

public class HexUtils {

    private HexUtils() {
        throw new UnsupportedOperationException(
                "This is a utility class and cannot be instantiated");
    }

    /**
     * Converts a byte array to a hexadecimal value.
     *
     * @param bytes The bytes to convert
     * @return The bytes hexadecimal value in the form of a string
     */
    public static String bytesToHex(byte[] bytes) {
        StringBuilder result = new StringBuilder();
        for (byte aByte : bytes) {
            result.append(String.format("%02X", aByte));
        }
        return result.toString();
    }

    /**
     * An unsigned varint is typically used in over-the-wire transmission of integers. The use less
     * storage than a standard binary representation for small values, and more storage for larger
     * values. This is used as the vast majority of integer values used in computer systems are
     * small.
     *
     * <ol>
     *   <li>Convert the integer value to binary representation.
     *   <li>Group the binary digits into groups of 7 bits, with the most significant bit (MSB) of
     *       each group indicating whether more bytes follow.
     *   <li>Write the groups of 7 bits, with the MSB set to 1 for all groups except the last one.
     *   <li>If necessary, pad the last group with zero bits to make it a full 7 bits.
     *   <li>Convert each group to its hexadecimal representation.
     *   <li>Concatenate the hexadecimal representations to form the varint.
     * </ol>
     *
     * As an example:
     *
     * <ol>
     *   <li>Convert 0x1200 to binary: 0001 0010 0000 0000
     *   <li>Group into 7-bit groups: 0000100, 1000000
     *   <li>Add MSB for each group (except the last one): 10000100, 11000000
     *   <li>Convert to hexadecimal: 0x84, 0xC0
     *   <li>Concatenate: 0x84C0
     * </ol>
     *
     * @param hex The hex value to convert
     * @return The varint value of the hex
     */
    public static String hexToVarintHex(String hex) {
        BigInteger bigInteger = new BigInteger(hex, 16);
        List<Byte> varintBytes = new ArrayList<>();

        while (true) {
            byte b = bigInteger.byteValue(); // Extract the least significant byte
            bigInteger = bigInteger.shiftRight(7); // Right shift by 7 bits
            if (bigInteger.equals(BigInteger.ZERO)) { // If no more bytes are needed
                varintBytes.add(b);
                break;
            } else {
                varintBytes.add((byte) (b | 0x80)); // Set the most significant bit to 1
            }
        }

        StringBuilder varintHexBuilder = new StringBuilder();
        for (Byte varintByte : varintBytes) {
            varintHexBuilder.append(String.format("%02X", varintByte));
        }

        return varintHexBuilder.toString();
    }
}
