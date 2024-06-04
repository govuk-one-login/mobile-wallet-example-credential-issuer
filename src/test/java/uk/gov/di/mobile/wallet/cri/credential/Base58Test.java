package uk.gov.di.mobile.wallet.cri.credential;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertArrayEquals;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;

public class Base58Test {

    @Test
    void shouldDecodeBase58EncodedInput() throws AddressFormatException {
        assertArrayEquals("Test String".getBytes(), Base58.decode("MvqLnZTzYSp7V4A"));
        assertArrayEquals(new byte[1], Base58.decode("1"));
        assertArrayEquals(new byte[4], Base58.decode("1111"));
    }

    @Test
    void shouldReturnAnEmptyByteArrayWhenInputIsAnEmptyString() throws AddressFormatException {
        assertArrayEquals(new byte[0], Base58.decode(""));
    }

    @Test
    void shouldThrowAddressFormatExceptionWhenInputIsNotBase58Encoded() {
        AddressFormatException thrown =
                assertThrows(
                        AddressFormatException.class,
                        () -> Base58.decode("Not a valid base58 encoded string"));
        assertEquals("Illegal character   at 3", thrown.getMessage());
    }
}
