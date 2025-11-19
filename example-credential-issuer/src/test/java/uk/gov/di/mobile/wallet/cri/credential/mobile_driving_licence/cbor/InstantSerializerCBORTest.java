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

class InstantSerializerCBORTest {

    private ObjectMapper cborObjectMapper;

    @BeforeEach
    void setUp() {
        CBORFactory cborFactory = new CBORFactory();
        cborObjectMapper = new ObjectMapper(cborFactory);
        SimpleModule module = new SimpleModule();
        module.addSerializer(new InstantCBORSerializer());
        cborObjectMapper.registerModule(module);
    }

    @Test
    void Should_SerializeInstant() throws IOException {
        Instant valueToSerialize = Instant.parse("2025-06-27T12:42:52.123178Z");

        byte[] cborBytes = cborObjectMapper.writeValueAsBytes(valueToSerialize);

        // Tag 0 (0xC0) used for Instant
        assertEquals((byte) 0xC0, cborBytes[0]);
    }

    @Test
    void Should_SerializeInstant_ContentRoundtrip() throws IOException {
        Instant valueToSerialize = Instant.parse("2025-06-27T12:42:52.123178Z");
        String expectedDateString = "2025-06-27T12:42:52Z";

        byte[] cborBytes = cborObjectMapper.writeValueAsBytes(valueToSerialize);

        CBORFactory cborFactory = new CBORFactory();
        ObjectMapper parser = new ObjectMapper(cborFactory);
        JsonNode parsedResult = parser.readTree(cborBytes);
        assertEquals(expectedDateString, parsedResult.asText());
    }

    @Test
    void Should_ThrowIllegalArgumentException_When_SerializerIsNonCBORGenerator() {
        // Create a JSON ObjectMapper (not CBOR) so the serializer sees a non-CBOR generator
        ObjectMapper jsonMapper = new ObjectMapper();
        SimpleModule module = new SimpleModule();
        module.addSerializer(new InstantCBORSerializer());
        jsonMapper.registerModule(module);
        Instant valueToSerialize = Instant.now();

        JsonMappingException exception =
                assertThrows(
                        JsonMappingException.class,
                        () -> jsonMapper.writeValueAsBytes(valueToSerialize));
        assertEquals(IllegalArgumentException.class, exception.getCause().getClass());
        assertEquals("Requires CBORGenerator", exception.getCause().getMessage());
    }
}
