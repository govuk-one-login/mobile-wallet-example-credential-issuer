package uk.gov.di.mobile.wallet.cri.credential.mobile_driving_licence.cbor;

import com.fasterxml.jackson.databind.JsonMappingException;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.module.SimpleModule;
import com.fasterxml.jackson.dataformat.cbor.CBORFactory;
import com.fasterxml.jackson.dataformat.cbor.CBORParser;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import uk.gov.di.mobile.wallet.cri.credential.mobile_driving_licence.mdoc.IssuerSignedItem;

import java.io.ByteArrayInputStream;
import java.io.IOException;

import static org.junit.jupiter.api.Assertions.assertArrayEquals;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

class IssuerSignedItemCBORSerializerTest {

    private ObjectMapper cborObjectMapper;
    private IssuerSignedItemCBORSerializer serializer;

    @BeforeEach
    void setUp() {
        CBORFactory cborFactory = new CBORFactory();
        cborObjectMapper = new ObjectMapper(cborFactory);
        SimpleModule module = new SimpleModule();
        serializer = new IssuerSignedItemCBORSerializer();
        module.addSerializer((Class) IssuerSignedItem.class, serializer);
        cborObjectMapper.registerModule(module);
    }

    @Test
    void Should_SerializeIssuerSignedItemWithCBORGenerator() throws IOException {
        IssuerSignedItem<?> issuerSignedItem =
                new IssuerSignedItem<>(
                        1, new byte[] {0x01, 0x02, 0x03}, "testElement", "testValue");

        byte[] result = cborObjectMapper.writeValueAsBytes(issuerSignedItem);

        // Outer bytes should start with tag 24 (0xD8 0x18)
        assertEquals((byte) 0xD8, result[0]); // CBOR tag 24 byte 1
        assertEquals((byte) 0x18, result[1]); // CBOR tag 24 byte 2
    }

    @Test
    void Should_EncodeIssuerSignedItemStructureInsideEmbeddedCBOR() throws IOException {
        IssuerSignedItem<?> issuerSignedItem =
                new IssuerSignedItem<>(
                        1, new byte[] {0x01, 0x02, 0x03}, "testElement", "testValue");

        byte[] outer = cborObjectMapper.writeValueAsBytes(issuerSignedItem);

        // Extract the inner embedded CBOR from the tag-24 wrapped byte string
        CBORFactory factory = new CBORFactory();
        byte[] embedded;
        try (CBORParser parser = factory.createParser(new ByteArrayInputStream(outer))) {
            parser.nextToken(); // VALUE_EMBEDDED_OBJECT representing the tag-24 + bstr
            embedded = parser.getBinaryValue();
        }

        // Parse the embedded CBOR structure to verify fields and values
        ObjectMapper innerMapper = new ObjectMapper(new CBORFactory());
        JsonNode parsed = innerMapper.readTree(embedded);

        assertTrue(parsed.isObject());
        assertEquals(4, parsed.size());
        assertTrue(parsed.has("digestID"));
        assertTrue(parsed.has("random"));
        assertTrue(parsed.has("elementIdentifier"));
        assertTrue(parsed.has("elementValue"));

        assertEquals(1, parsed.get("digestID").asInt());
        assertArrayEquals(new byte[] {0x01, 0x02, 0x03}, parsed.get("random").binaryValue());
        assertEquals("testElement", parsed.get("elementIdentifier").asText());
        assertEquals("testValue", parsed.get("elementValue").asText());
    }

    @Test
    void Should_ThrowIllegalArgumentException_When_SerializerIsNonCBORGenerator() {
        ObjectMapper jsonObjectMapper = new ObjectMapper();
        SimpleModule module = new SimpleModule();
        module.addSerializer((Class) IssuerSignedItem.class, serializer);
        jsonObjectMapper.registerModule(module);

        IssuerSignedItem<?> issuerSignedItem =
                new IssuerSignedItem<>(1, new byte[] {0x01}, "test", "value");

        JsonMappingException exception =
                assertThrows(
                        JsonMappingException.class,
                        () -> jsonObjectMapper.writeValueAsBytes(issuerSignedItem));
        assertEquals(IllegalArgumentException.class, exception.getCause().getClass());
        assertEquals("Requires CBORGenerator", exception.getCause().getMessage());
    }
}
