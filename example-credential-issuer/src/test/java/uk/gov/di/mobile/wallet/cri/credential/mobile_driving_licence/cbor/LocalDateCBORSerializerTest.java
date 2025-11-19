package uk.gov.di.mobile.wallet.cri.credential.mobile_driving_licence.cbor;

import com.fasterxml.jackson.databind.JsonMappingException;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.module.SimpleModule;
import com.fasterxml.jackson.dataformat.cbor.CBORFactory;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.io.IOException;
import java.time.LocalDate;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;

class LocalDateCBORSerializerTest {

    private ObjectMapper cborObjectMapper;

    @BeforeEach
    void setUp() {
        CBORFactory cborFactory = new CBORFactory();
        cborObjectMapper = new ObjectMapper(cborFactory);
        SimpleModule module = new SimpleModule();
        module.addSerializer(new LocalDateCBORSerializer());
        cborObjectMapper.registerModule(module);
    }

    @Test
    void Should_SerializeLocalDate() throws IOException {
        LocalDate valueToSerialize = LocalDate.of(2025, 4, 4);

        byte[] result = cborObjectMapper.writeValueAsBytes(valueToSerialize);

        // Tag 1004 (0xD9 0x03 0xEC) used for full-date string
        assertEquals((byte) 0xD9, result[0]);
        assertEquals((byte) 0x03, result[1]);
        assertEquals((byte) 0xEC, result[2]);
    }

    @Test
    void Should_SerializeLocalDate_ContentRoundtrip() throws IOException {
        LocalDate valueToSerialize = LocalDate.of(2025, 4, 4);
        String expectedDateString = "2025-04-04";

        byte[] result = cborObjectMapper.writeValueAsBytes(valueToSerialize);

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
        module.addSerializer(new LocalDateCBORSerializer());
        jsonObjectMapper.registerModule(module);
        LocalDate valueToSerialize = LocalDate.now();

        JsonMappingException exception =
                assertThrows(
                        JsonMappingException.class,
                        () -> jsonObjectMapper.writeValueAsBytes(valueToSerialize));
        assertEquals(IllegalArgumentException.class, exception.getCause().getClass());
        assertEquals("Requires CBORGenerator", exception.getCause().getMessage());
    }
}
