package uk.gov.di.mobile.wallet.cri.credential.mobile_driving_licence.cbor;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.dataformat.cbor.CBORFactory;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import uk.gov.di.mobile.wallet.cri.credential.mobile_driving_licence.mdoc.IssuerSignedItem;

import java.io.IOException;

import static org.junit.jupiter.api.Assertions.assertArrayEquals;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

class IssuerSignedItemEncoderTest {

    private ObjectMapper cborObjectMapper;

    @BeforeEach
    void setUp() {
        CBORFactory cborFactory = new CBORFactory();
        cborObjectMapper = new ObjectMapper(cborFactory);
    }

    @Test
    void Should_EncodeIssuerSignedItemToCBORBytes() throws IOException {
        IssuerSignedItem issuerSignedItem =
                new IssuerSignedItem(1, new byte[] {0x01, 0x02, 0x03}, "testElement", "testValue");

        byte[] result = IssuerSignedItemEncoder.encode(issuerSignedItem, cborObjectMapper);

        // Verify definite length map (4 items should start with 0xA3)
        assertEquals((byte) 0xA4, result[0]);

        // Parse back the CBOR to verify structure
        CBORFactory cborFactory = new CBORFactory();
        ObjectMapper parser = new ObjectMapper(cborFactory);
        JsonNode parsedResult = parser.readTree(result);

        assertTrue(parsedResult.isObject());
        assertEquals(4, parsedResult.size());
        assertTrue(parsedResult.has("digestID"));
        assertTrue(parsedResult.has("random"));
        assertTrue(parsedResult.has("elementIdentifier"));
        assertTrue(parsedResult.has("elementValue"));

        assertEquals(1, parsedResult.get("digestID").asInt());
        assertArrayEquals(new byte[] {0x01, 0x02, 0x03}, parsedResult.get("random").binaryValue());
        assertEquals("testElement", parsedResult.get("elementIdentifier").asText());
        assertEquals("testValue", parsedResult.get("elementValue").asText());
    }
}
