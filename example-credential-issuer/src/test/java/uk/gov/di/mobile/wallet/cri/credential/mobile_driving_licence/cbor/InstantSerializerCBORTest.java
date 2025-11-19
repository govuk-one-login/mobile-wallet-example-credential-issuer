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

class InstantSerializerTest {

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
        // Serializer truncates fractional seconds
        String expectedDateString = "2025-06-27T12:42:52Z";

        byte[] result = cborObjectMapper.writeValueAsBytes(testDate);

        // Verify CBOR tag 0 at the start of the byte array
        // 0xC0 == CBOR tag 0 for date/time string
        assertEquals((byte) 0xC0, result[0]);
        // Parse the CBOR back to verify the textual content matches the expected truncation
        CBORFactory cborFactory = new CBORFactory();
        ObjectMapper parser = new ObjectMapper(cborFactory);
        JsonNode parsedResult = parser.readTree(result);
        assertEquals(expectedDateString, parsedResult.asText());
    }

    @Test
    void Should_ThrowIllegalArgumentException_When_SerializerIsNonCBORGenerator() {
        // Create a JSON ObjectMapper (not CBOR) so the serializer sees a non-CBOR generator
        ObjectMapper jsonObjectMapper = new ObjectMapper();
        SimpleModule module = new SimpleModule();
        module.addSerializer(Instant.class, serializer);
        jsonObjectMapper.registerModule(module);

        Instant testDate = Instant.now();

        JsonMappingException exception =
                assertThrows(
                        JsonMappingException.class,
                        () -> jsonObjectMapper.writeValueAsBytes(testDate));
        assertEquals(IllegalArgumentException.class, exception.getCause().getClass());
        assertEquals("Requires CBORGenerator", exception.getCause().getMessage());
    }
}
