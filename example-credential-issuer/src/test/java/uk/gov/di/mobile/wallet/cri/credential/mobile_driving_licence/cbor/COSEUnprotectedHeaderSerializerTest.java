package uk.gov.di.mobile.wallet.cri.credential.mobile_driving_licence.cbor;

import com.fasterxml.jackson.databind.JsonMappingException;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.module.SimpleModule;
import com.fasterxml.jackson.dataformat.cbor.CBORFactory;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import uk.gov.di.mobile.wallet.cri.credential.mobile_driving_licence.cose.COSEUnprotectedHeader;
import uk.gov.di.mobile.wallet.cri.credential.mobile_driving_licence.cose.COSEUnprotectedHeaderBuilder;

import java.io.IOException;

import static org.junit.jupiter.api.Assertions.assertArrayEquals;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

class COSEUnprotectedHeaderSerializerTest {

    private ObjectMapper cborObjectMapper;

    @BeforeEach
    void setUp() {
        CBORFactory cborFactory = new CBORFactory();
        cborObjectMapper = new ObjectMapper(cborFactory);
        SimpleModule module = new SimpleModule();
        module.addSerializer(new COSEUnprotectedHeaderSerializer());
        cborObjectMapper.registerModule(module);
    }

    @Test
    void Should_SerializeCOSEUnprotectedHeader() throws IOException {
        byte[] x5chain = new byte[] {1, 2, 3};
        COSEUnprotectedHeader valueToSerialize =
                new COSEUnprotectedHeaderBuilder().x5chain(x5chain).build();

        byte[] cborBytes = cborObjectMapper.writeValueAsBytes(valueToSerialize);

        // Definite-length, 1-entry map -> 0xA1
        assertEquals((byte) 0xA1, cborBytes[0]);
        // Key 33 as CBOR integer → 0x18 0x21
        assertEquals((byte) 0x18, cborBytes[1]);
        assertEquals((byte) 0x21, cborBytes[2]);
        // Value {1, 2, 3} as CBOR byte string of length 3 → 0x43, then the 3 bytes
        assertEquals((byte) 0x43, cborBytes[3]);
        assertEquals((byte) 0x01, cborBytes[4]);
        assertEquals((byte) 0x02, cborBytes[5]);
        assertEquals((byte) 0x03, cborBytes[6]);
    }

    @Test
    void Should_SerializeCOSEProtectedHeader_ContentRoundtrip() throws IOException {
        byte[] certificateBytes = new byte[] {1, 2, 3};
        COSEUnprotectedHeader valueToSerialize =
                new COSEUnprotectedHeaderBuilder().x5chain(certificateBytes).build();
        int expectedX5chainKey = 33; // 33="x5chain"

        byte[] cborBytes = cborObjectMapper.writeValueAsBytes(valueToSerialize);

        JsonNode node = cborObjectMapper.readTree(cborBytes);
        assertTrue(node.isObject());
        assertEquals(1, node.size());
        assertTrue(node.has(String.valueOf(expectedX5chainKey)));
        assertArrayEquals(
                certificateBytes, node.get(String.valueOf(expectedX5chainKey)).binaryValue());
    }

    @Test
    void Should_ThrowIllegalArgumentException_When_SerializerIsNonCBORGenerator() {
        // Create a JSON ObjectMapper (not CBOR) so the serializer sees a non-CBOR generator
        ObjectMapper jsonMapper = new ObjectMapper();
        SimpleModule module = new SimpleModule();
        module.addSerializer(new COSEUnprotectedHeaderSerializer());
        jsonMapper.registerModule(module);
        COSEUnprotectedHeader valueToSerialize =
                new COSEUnprotectedHeaderBuilder().x5chain(new byte[] {1, 2, 3}).build();

        JsonMappingException exception =
                assertThrows(
                        JsonMappingException.class,
                        () -> jsonMapper.writeValueAsBytes(valueToSerialize));
        assertEquals(IllegalArgumentException.class, exception.getCause().getClass());
        assertEquals("Requires CBORGenerator", exception.getCause().getMessage());
    }
}
