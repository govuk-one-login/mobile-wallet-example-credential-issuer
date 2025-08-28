package uk.gov.di.mobile.wallet.cri.credential.mobile_driving_licence.cbor;

import com.fasterxml.jackson.databind.JsonMappingException;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.module.SimpleModule;
import com.fasterxml.jackson.dataformat.cbor.CBORFactory;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.io.IOException;
import java.util.HashMap;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

class DefiniteLengthMapSerializerTest {

    private ObjectMapper cborObjectMapper;
    private DefiniteLengthMapSerializer serializer;

    @BeforeEach
    void setUp() {
        CBORFactory cborFactory = new CBORFactory();
        cborObjectMapper = new ObjectMapper(cborFactory);
        SimpleModule module = new SimpleModule();
        serializer = new DefiniteLengthMapSerializer();
        module.addSerializer(Map.class, serializer);
        cborObjectMapper.registerModule(module);
    }

    @Test
    void Should_SerializeMapWithStringKeysWithCBORGenerator() throws IOException {
        Map<String, Object> testMap = new HashMap<>();
        testMap.put("key1", "string");
        testMap.put("key2", 42);
        testMap.put("key3", true);

        byte[] result = cborObjectMapper.writeValueAsBytes(testMap);

        // Verify definite length map (3 items should start with 0xA3)
        assertEquals((byte) 0xA3, result[0]);

        // Parse back the CBOR to verify structure
        CBORFactory cborFactory = new CBORFactory();
        ObjectMapper parser = new ObjectMapper(cborFactory);
        JsonNode parsedResult = parser.readTree(result);

        assertTrue(parsedResult.isObject());
        assertEquals(3, parsedResult.size());
        assertTrue(parsedResult.has("key1"));
        assertTrue(parsedResult.has("key2"));
        assertTrue(parsedResult.has("key3"));
        assertEquals("string", parsedResult.get("key1").asText());
        assertEquals(42, parsedResult.get("key2").asInt());
        assertTrue(parsedResult.get("key3").asBoolean());
    }

    @Test
    void Should_ThrowIllegalArgumentException_When_SerializerIsNonCBORGenerator() {
        // Arrange: Create a regular JSON ObjectMapper
        ObjectMapper jsonObjectMapper = new ObjectMapper();
        SimpleModule module = new SimpleModule();
        module.addSerializer(Map.class, serializer);
        jsonObjectMapper.registerModule(module);

        Map<String, String> testMap = new HashMap<>();
        testMap.put("key", "value");

        // Act & Assert: Expect JsonMappingException which wraps the IllegalArgumentException
        JsonMappingException exception =
                assertThrows(
                        JsonMappingException.class,
                        () -> jsonObjectMapper.writeValueAsBytes(testMap));

        // Verify the root cause is IllegalArgumentException with the expected message
        assertEquals(IllegalArgumentException.class, exception.getCause().getClass());
        assertEquals("Requires CBORGenerator", exception.getCause().getMessage());
    }
}
