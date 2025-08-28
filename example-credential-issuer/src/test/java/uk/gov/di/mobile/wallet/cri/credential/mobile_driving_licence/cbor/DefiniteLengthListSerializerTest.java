package uk.gov.di.mobile.wallet.cri.credential.mobile_driving_licence.cbor;

import com.fasterxml.jackson.databind.JsonMappingException;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.module.SimpleModule;
import com.fasterxml.jackson.dataformat.cbor.CBORFactory;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.io.IOException;
import java.util.Arrays;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

class DefiniteLengthListSerializerTest {

    private ObjectMapper cborObjectMapper;
    private DefiniteLengthListSerializer serializer;

    @BeforeEach
    void setUp() {
        CBORFactory cborFactory = new CBORFactory();
        cborObjectMapper = new ObjectMapper(cborFactory);
        SimpleModule module = new SimpleModule();
        serializer = new DefiniteLengthListSerializer();
        module.addSerializer(List.class, serializer);

        cborObjectMapper.registerModule(module);
    }

    @Test
    void Should_SerializeListWithCBORGenerator() throws IOException {
        List<String> testList = Arrays.asList("item1", "item2", "item3");

        byte[] result = cborObjectMapper.writeValueAsBytes(testList);

        // Parse back the CBOR to verify structure
        CBORFactory cborFactory = new CBORFactory();
        ObjectMapper parser = new ObjectMapper(cborFactory);
        JsonNode parsedResult = parser.readTree(result);

        assertTrue(parsedResult.isArray());
        assertEquals(3, parsedResult.size());
        assertEquals("item1", parsedResult.get(0).asText());
        assertEquals("item2", parsedResult.get(1).asText());
        assertEquals("item3", parsedResult.get(2).asText());
    }

    @Test
    void Should_SerializeMixedTypeListWithCBORGenerator() throws IOException {
        List<Object> mixedList = Arrays.asList("string", 42, true);

        byte[] result = cborObjectMapper.writeValueAsBytes(mixedList);

        // Verify definite length array (3 items should start with 0x83)
        assertEquals((byte) 0x83, result[0]);

        // Parse back the CBOR to verify structure
        CBORFactory cborFactory = new CBORFactory();
        ObjectMapper parser = new ObjectMapper(cborFactory);
        JsonNode parsedResult = parser.readTree(result);

        assertTrue(parsedResult.isArray());
        assertEquals(3, parsedResult.size());
        assertEquals("string", parsedResult.get(0).asText());
        assertEquals(42, parsedResult.get(1).asInt());
        assertTrue(parsedResult.get(2).asBoolean());
    }

    @Test
    void Should_ThrowIllegalArgumentException_When_SerializerIsNonCBORGenerator() {
        // Arrange: Create a regular JSON ObjectMapper
        ObjectMapper jsonObjectMapper = new ObjectMapper();
        SimpleModule module = new SimpleModule();
        module.addSerializer(List.class, serializer);
        jsonObjectMapper.registerModule(module);

        List<String> testList = List.of("test");

        // Act & Assert: Expect JsonMappingException which wraps the IllegalArgumentException
        JsonMappingException exception =
                assertThrows(
                        JsonMappingException.class,
                        () -> jsonObjectMapper.writeValueAsBytes(testList));

        // Verify the root cause is IllegalArgumentException with the expected message
        assertEquals(IllegalArgumentException.class, exception.getCause().getClass());
        assertEquals("Requires CBORGenerator", exception.getCause().getMessage());
    }
}
