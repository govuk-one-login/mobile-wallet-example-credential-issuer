package uk.gov.di.mobile.wallet.cri.credential;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;

public class HexUtilsTest {

    @Test
    void shouldHexEncodeABytesArray() {
        assertEquals("54657374537472696E67", HexUtils.bytesToHex("TestString".getBytes()));
    }

    @Test
    void shouldConvertHexToVarint() {
        assertEquals("8024", HexUtils.hexToVarintHex("1200"));
    }
}
