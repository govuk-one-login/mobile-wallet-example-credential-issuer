package uk.gov.di.mobile.wallet.cri.util;

import org.junit.jupiter.api.Test;

import java.nio.charset.StandardCharsets;

import static org.junit.jupiter.api.Assertions.assertArrayEquals;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

class HashUtilTest {

    private static final String ABC = "abc";
    private static final String ABC_SHA256_HEX =
            "ba7816bf8f01cfea414140de5dae2223b00361a396177a9cb410ff61f20015ad";

    @Test
    void Should_ReturnCorrectSha256HashForString() {
        byte[] hash = HashUtil.sha256(ABC);
        assertNotNull(hash);
        assertEquals(32, hash.length); // SHA-256 produces 32 bytes
    }

    @Test
    void Should_ReturnCorrectSha256HashForByteArray() {
        byte[] input = ABC.getBytes(StandardCharsets.UTF_8);
        byte[] hash = HashUtil.sha256(input);
        assertNotNull(hash);
        assertEquals(32, hash.length);
    }

    @Test
    void Should_ReturnCorrectHexStringForString() {
        String hexHash = HashUtil.sha256Hex(ABC);
        assertEquals(ABC_SHA256_HEX, hexHash);
        assertEquals(64, hexHash.length()); // 32 bytes = 64 hex characters
        assertTrue(hexHash.matches("^[a-f0-9]+$")); // Only lowercase hex characters
    }

    @Test
    void Should_ReturnCorrectHexStringForByteArray() {
        byte[] input = ABC.getBytes(StandardCharsets.UTF_8);
        String hexHash = HashUtil.sha256Hex(input);
        assertEquals(ABC_SHA256_HEX, hexHash);
        assertTrue(hexHash.matches("^[a-f0-9]+$"));
    }

    @Test
    void Should_ThrowExceptionForNullString() {
        IllegalArgumentException exception =
                assertThrows(IllegalArgumentException.class, () -> HashUtil.sha256((String) null));
        assertEquals("Input string cannot be null", exception.getMessage());
    }

    @Test
    void Should_ThrowExceptionForNullByteArray() {
        IllegalArgumentException exception =
                assertThrows(IllegalArgumentException.class, () -> HashUtil.sha256((byte[]) null));
        assertEquals("Input byte array cannot be null", exception.getMessage());
    }

    @Test
    void Should_ProduceSameResultForStringAndByteArray() {
        byte[] stringHash = HashUtil.sha256(ABC);
        byte[] byteArrayHash = HashUtil.sha256(ABC.getBytes(StandardCharsets.UTF_8));
        assertArrayEquals(stringHash, byteArrayHash);
    }

    @Test
    void Should_ProduceDifferentHashesForDifferentInputs() {
        byte[] hash1 = HashUtil.sha256("Hello");
        byte[] hash2 = HashUtil.sha256("hello");
        assertFalse(java.util.Arrays.equals(hash1, hash2));
    }
}
