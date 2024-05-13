package uk.gov.di.mobile.wallet.cri.did_key;

import org.bouncycastle.util.encoders.Hex;

import java.math.BigInteger;
import java.util.Arrays;

public class Base58Utils {
    public static final char[] BASE58_ALPHABET =
            "123456789ABCDEFGHJKLMNPQRSTUVWXYZabcdefghijkmnopqrstuvwxyz".toCharArray();
    private static final char BASE58_ENCODED_ZERO = BASE58_ALPHABET[0];
    private static final int[] INDEXES = new int[128];

    static {
        Arrays.fill(INDEXES, -1);
        for (int i = 0; i < BASE58_ALPHABET.length; i++) {
            INDEXES[BASE58_ALPHABET[i]] = i;
        }
    }

    /**
     * Encodes the given bytes as a base58 string (no checksum is appended).
     *
     * @param input the bytes to encode
     * @return the base58-encoded string
     */
    public static String encode(byte[] input) {
        System.out.println(Hex.toHexString(input));
        System.out.println(input.length);

        if (input.length == 0) {
            return "";
        }

        // count how many leading zeroes in byte array to encode
        int leadingZeroesCount = 0;
        while (leadingZeroesCount < input.length && input[leadingZeroesCount] == 0) {
            ++leadingZeroesCount;
        }

        input = Arrays.copyOf(input, input.length); // since we modify it in-place

        char[] encoded = new char[input.length * 2]; // upper bound

        int startEncodingAt = leadingZeroesCount;
        int index = encoded.length;

        while (startEncodingAt < input.length) {
            System.out.println("startEncodingAt " + startEncodingAt);
            System.out.println("index " + index);
            encoded[--index] = BASE58_ALPHABET[divmod(input, startEncodingAt, 256, 58)];
            if (input[startEncodingAt] == 0) {
                System.out.println("it is 0");
                ++startEncodingAt; // optimization - skip leading zeros
            }
            System.out.println(encoded);
        }
        // Preserve exactly as many leading encoded zeros in output as there were leading zeros in
        // input.
        while (index < encoded.length && encoded[index] == BASE58_ENCODED_ZERO) {
            ++index;
        }
        while (--leadingZeroesCount >= 0) {
            encoded[--index] = BASE58_ENCODED_ZERO;
        }
        // Return encoded string (including encoded leading zeros).
        return new String(encoded, index, encoded.length - index);
    }

    /**
     * Decodes the given base58 string into the original data bytes.
     *
     * @param input the base58-encoded string to decode
     * @return the decoded data bytes
     */
    public static byte[] decode(String input) {
        if (input.length() == 0) {
            return new byte[0];
        }
        // Convert the base58-encoded ASCII chars to a base58 byte sequence (base58 digits).
        byte[] input58 = new byte[input.length()];
        for (int i = 0; i < input.length(); ++i) {
            char c = input.charAt(i);
            int digit = c < 128 ? INDEXES[c] : -1;
            if (digit < 0) {
                throw new IllegalArgumentException(
                        String.format("Invalid character in Base58: 0x%04x", (int) c));
            }
            input58[i] = (byte) digit;
        }
        // Count leading zeros.
        int zeros = 0;
        while (zeros < input58.length && input58[zeros] == 0) {
            ++zeros;
        }
        // Convert base-58 digits to base-256 digits.
        byte[] decoded = new byte[input.length()];
        int outputStart = decoded.length;
        for (int inputStart = zeros; inputStart < input58.length; ) {
            decoded[--outputStart] = divmod(input58, inputStart, 58, 256);
            if (input58[inputStart] == 0) {
                ++inputStart; // optimization - skip leading zeros
            }
        }
        // Ignore extra leading zeroes that were added during the calculation.
        while (outputStart < decoded.length && decoded[outputStart] == 0) {
            ++outputStart;
        }
        // Return decoded data (including original number of leading zeros).
        return Arrays.copyOfRange(decoded, outputStart - zeros, decoded.length);
    }

    public static BigInteger decodeToBigInteger(String input) {
        return new BigInteger(1, decode(input));
    }

    /**
     * Divides a number, represented as an array of bytes each containing a single digit in the
     * specified base, by the given divisor. The given number is modified in-place to contain the
     * quotient, and the return value is the remainder.
     *
     * @param number the number to divide
     * @param firstDigit the index within the array of the first non-zero digit (this is used for
     *     optimization by skipping the leading zeros)
     * @param base the base in which the number's digits are represented (up to 256)
     * @param divisor the number to divide by (up to 256)
     * @return the remainder of the division operation
     */
    private static byte divmod(byte[] number, int firstDigit, int base, int divisor) {
        // this is just long division which accounts for the base of the input digits
        int remainder = 0;
        for (int i = firstDigit; i < number.length; i++) {
            int digit = (int) number[i] & 0xFF;
            int temp = remainder * base + digit;
            number[i] = (byte) (temp / divisor);
            remainder = temp % divisor;
        }
        return (byte) remainder;
    }
}
