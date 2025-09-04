package uk.gov.di.mobile.wallet.cri.credential.mobile_driving_licence.did_key;

import org.junit.jupiter.api.Test;
import uk.gov.di.mobile.wallet.cri.credential.jwt.did_key.HexUtils;

import static org.junit.jupiter.api.Assertions.assertEquals;

class HexUtilsTest {

    @Test
    void should_Hex_Encode_A_BytesArray() {
        assertEquals("54657374537472696E67", HexUtils.bytesToHex("TestString".getBytes()));
    }

    @Test
    void should_Convert_Hex_To_Varint() {
        assertEquals("8024", HexUtils.hexToVarintHex("1200"));
    }
}
