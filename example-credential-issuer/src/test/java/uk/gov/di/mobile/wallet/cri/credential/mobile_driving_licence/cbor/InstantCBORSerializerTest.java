package uk.gov.di.mobile.wallet.cri.credential.mobile_driving_licence.cbor;

import com.fasterxml.jackson.databind.JsonMappingException;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.module.SimpleModule;
import com.fasterxml.jackson.dataformat.cbor.CBORFactory;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.io.IOException;
import java.time.Instant;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;

class InstantCBORSerializerTest {

    private ObjectMapper cborObjectMapper;
    private InstantCBORSerializer serializer;

    @BeforeEach
    void setUp() {
        CBORFactory cborFactory = new CBORFactory();
        cborObjectMapper = new ObjectMapper(cborFactory);
        SimpleModule module = new SimpleModule();
        serializer = new InstantCBORSerializer();
        module.addSerializer(Instant.class, serializer);
        cborObjectMapper.registerModule(module);
    }

    @Test
    void Should_SerializeInstantWithCBORGenerator() throws IOException {
        Instant testDate = Instant.parse("2025-06-27T12:42:52.123178Z");
        String expectedDateString = "2025-06-27T12:42:52Z";

        byte[] result = cborObjectMapper.writeValueAsBytes(testDate);

        // Verify CBOR tag 0 is present at the start of the array of bytes
        assertEquals((byte) 0xC0, result[0]);

        // Parse back the CBOR to verify the content
        CBORFactory cborFactory = new CBORFactory();
        ObjectMapper parser = new ObjectMapper(cborFactory);
        JsonNode parsedResult = parser.readTree(result);

        assertEquals(expectedDateString, parsedResult.asText());
    }

    @Test
    void Should_ThrowIllegalArgumentException_When_SerializerIsNonCBORGenerator() {
        // Arrange: Create a regular JSON ObjectMapper
        ObjectMapper jsonObjectMapper = new ObjectMapper();
        SimpleModule module = new SimpleModule();
        module.addSerializer(Instant.class, serializer);
        jsonObjectMapper.registerModule(module);

        Instant testDate = Instant.now();

        // Act & Assert: Expect JsonMappingException which wraps the IllegalArgumentException
        JsonMappingException exception =
                assertThrows(
                        JsonMappingException.class,
                        () -> jsonObjectMapper.writeValueAsBytes(testDate));

        // Verify the root cause is IllegalArgumentException with the expected message
        assertEquals(IllegalArgumentException.class, exception.getCause().getClass());
        assertEquals("Requires CBORGenerator", exception.getCause().getMessage());
    }
}
