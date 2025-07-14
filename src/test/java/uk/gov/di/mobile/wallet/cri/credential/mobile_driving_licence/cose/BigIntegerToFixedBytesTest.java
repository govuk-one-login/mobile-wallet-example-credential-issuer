package uk.gov.di.mobile.wallet.cri.credential.mobile_driving_licence.cose;

import org.junit.jupiter.api.Test;

import java.math.BigInteger;

import static org.junit.jupiter.api.Assertions.assertArrayEquals;
import static org.junit.jupiter.api.Assertions.assertEquals;

class BigIntegerToFixedBytesTest {

    /** Test that a value whose byte array matches the target length is returned as-is. */
    @Test
    void Should_HandleExactSizeMatch() {
        BigInteger value = new BigInteger("ABCD", 16); // [0xAB, 0xCD]
        int curveSizeBits = 16; // 2 bytes

        byte[] result = BigIntegerToFixedBytes.bigIntegerToFixedBytes(value, curveSizeBits);

        assertEquals(2, result.length);
        assertArrayEquals(new byte[] {(byte) 0xAB, (byte) 0xCD}, result);
    }

    /** Test that a value shorter than the target length is padded with leading zeros. */
    @Test
    void Should_PadWithLeadingZeros_When_ValueIsTooShort() {
        BigInteger value = new BigInteger("AB", 16); // [0xAB]
        int curveSizeBits = 32; // 4 bytes

        byte[] result = BigIntegerToFixedBytes.bigIntegerToFixedBytes(value, curveSizeBits);

        assertEquals(4, result.length);
        assertArrayEquals(new byte[] {0x00, 0x00, 0x00, (byte) 0xAB}, result);
    }

    /**
     * Test that a value longer than the target length is truncated from the left (most significant
     * bytes).
     */
    @Test
    void Should_TruncateFromLeft_When_ValueIsTooLarge() {
        BigInteger value = new BigInteger("ABCDEF12", 16); // [0xAB, 0xCD, 0xEF, 0x12]
        int curveSizeBits = 16; // 2 bytes

        byte[] result = BigIntegerToFixedBytes.bigIntegerToFixedBytes(value, curveSizeBits);

        assertEquals(2, result.length);
        assertArrayEquals(new byte[] {(byte) 0xEF, 0x12}, result);
    }

    /** Test that the method correctly pads to a typical 256-bit curve length. */
    @Test
    void Should_HandleP256CurveSize() {
        BigInteger value = new BigInteger("1234", 16); // [0x12, 0x34]
        int curveSizeBits = 256; // 32 bytes

        byte[] result = BigIntegerToFixedBytes.bigIntegerToFixedBytes(value, curveSizeBits);

        assertEquals(32, result.length);
        // Should be padded with 30 leading zeros followed by 0x12, 0x34
        for (int i = 0; i < 30; i++) {
            assertEquals(0, result[i]);
        }
        assertEquals(0x12, result[30] & 0xFF);
        assertEquals(0x34, result[31] & 0xFF);
    }
}
