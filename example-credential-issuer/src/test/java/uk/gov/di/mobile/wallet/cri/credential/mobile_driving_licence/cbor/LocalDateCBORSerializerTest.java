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
import java.time.format.DateTimeFormatter;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;

class LocalDateSerializerTest {

    private ObjectMapper cborObjectMapper;
    private LocalDateCBORSerializer serializer;

    @BeforeEach
    void setUp() {
        CBORFactory cborFactory = new CBORFactory();
        cborObjectMapper = new ObjectMapper(cborFactory);
        SimpleModule module = new SimpleModule();
        serializer = new LocalDateCBORSerializer();
        module.addSerializer(LocalDate.class, serializer);
        cborObjectMapper.registerModule(module);
    }

    @Test
    void Should_SerializeInstantWithCBORGenerator() throws IOException {
        LocalDate testDate = LocalDate.of(2025, 4, 4);
        String expectedDateString = testDate.format(DateTimeFormatter.ISO_LOCAL_DATE);

        byte[] result = cborObjectMapper.writeValueAsBytes(testDate);

        // Verify CBOR tag 1004 at the start of the byte array
        // 0xD9 0x03 0xEC == CBOR tag 1004 for full-date string
        assertEquals((byte) 0xD9, result[0]);
        assertEquals((byte) 0x03, result[1]);
        assertEquals((byte) 0xEC, result[2]);
        // Parse the CBOR back to verify the textual content matches the expected date
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
        module.addSerializer(LocalDate.class, serializer);
        jsonObjectMapper.registerModule(module);

        LocalDate testDate = LocalDate.now();

        JsonMappingException exception =
                assertThrows(
                        JsonMappingException.class,
                        () -> jsonObjectMapper.writeValueAsBytes(testDate));
        assertEquals(IllegalArgumentException.class, exception.getCause().getClass());
        assertEquals("Requires CBORGenerator", exception.getCause().getMessage());
    }
}