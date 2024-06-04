package uk.gov.di.mobile.wallet.cri.credential;

import org.junit.jupiter.api.Test;

import java.util.Arrays;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

public class Base58Test {

    @Test
    void shouldDecodeBase58EncodedInput() throws AddressFormatException {
        assertTrue(
                Arrays.equals(Base58.decode("MvqLnZTzYSp7V4A"), "Test String".getBytes()),
                new String(Base58.decode("MvqLnZTzYSp7V4A")));
        assertTrue(Arrays.equals(Base58.decode("1"), new byte[1]));
        assertTrue(Arrays.equals(Base58.decode("1111"), new byte[4]));
    }

    @Test
    void shouldReturnAnEmptyByteArrayWhenInputIsAnEmptyString() throws AddressFormatException {
        assertTrue(Arrays.equals(Base58.decode(""), new byte[0]));
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
