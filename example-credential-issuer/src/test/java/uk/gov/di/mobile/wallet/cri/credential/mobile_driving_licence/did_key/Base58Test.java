package uk.gov.di.mobile.wallet.cri.credential.mobile_driving_licence.did_key;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import uk.gov.di.mobile.wallet.cri.credential.did_key.AddressFormatException;
import uk.gov.di.mobile.wallet.cri.credential.did_key.Base58;

import static org.junit.jupiter.api.Assertions.assertArrayEquals;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;

class Base58Test {

    @Test
    @DisplayName("Should decode Base58 Encoded input")
    void should_Decode_Base58Encoded_Input() throws AddressFormatException {
        assertArrayEquals("Test String".getBytes(), Base58.decode("MvqLnZTzYSp7V4A"));
        assertArrayEquals(new byte[1], Base58.decode("1"));
        assertArrayEquals(new byte[4], Base58.decode("1111"));
    }

    @Test
    @DisplayName("should return an empty Byte Array when Input is an empty string")
    void should_Return_EmptyByteArray_When_Input_Is_Empty_String() throws AddressFormatException {
        assertArrayEquals(new byte[0], Base58.decode(""));
    }

    @Test
    @DisplayName("should Throw Address Format Exception when input is not Base58 Encoded")
    void should_ThrowException_When_Input_Is_Not_Base58Encoded() {
        AddressFormatException thrown =
                assertThrows(
                        AddressFormatException.class,
                        () -> Base58.decode("NotAValidBase58EncodedString"));
        assertEquals("Invalid character 'l' at position 6", thrown.getMessage());
    }
}
