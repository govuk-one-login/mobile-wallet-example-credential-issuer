package uk.gov.di.mobile.wallet.cri.credential.mobile_driving_licence.cbor;

import com.fasterxml.jackson.databind.JsonMappingException;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.module.SimpleModule;
import com.fasterxml.jackson.dataformat.cbor.CBORFactory;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import uk.gov.di.mobile.wallet.cri.credential.mobile_driving_licence.mdoc.ValidityInfo;

import java.io.IOException;
import java.time.Instant;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

class ValidityInfoSerializerTest {

    private ObjectMapper cborObjectMapper;

    @BeforeEach
    void setUp() {
        CBORFactory cborFactory = new CBORFactory();
        cborObjectMapper = new ObjectMapper(cborFactory);
        SimpleModule module = new SimpleModule();
        module.addSerializer(new ValidityInfoSerializer());
        module.addSerializer(new InstantCBORSerializer()); // Required for ValidityInfo
        cborObjectMapper.registerModule(module);
    }

    @Test
    void Should_SerializeValidityInfo() throws IOException {
        ValidityInfo valueToSerialize =
                new ValidityInfo(
                        Instant.parse("2025-01-01T00:00:00Z"),
                        Instant.parse("2025-01-01T00:00:00Z"),
                        Instant.parse("2025-12-31T23:59:59Z"));

        byte[] cborBytes = cborObjectMapper.writeValueAsBytes(valueToSerialize);

        // Definite-length, 3-entry map
        assertEquals((byte) 0xA3, cborBytes[0]);
        // Tag 0 (0xC0) used for each Instant
        int tag0Count = 0;
        for (byte b : cborBytes) {
            if ((b & 0xFF) == 0xC0) tag0Count++;
        }
        assertEquals(3, tag0Count, "Each of signed, validFrom, validUntil should have CBOR tag 0");
    }

    @Test
    void Should_SerializeValidityInfo_ContentRoundtrip() throws IOException {
        ValidityInfo valueToSerialize =
                new ValidityInfo(
                        Instant.parse("2025-01-01T00:00:00Z"),
                        Instant.parse("2025-01-01T00:00:00Z"),
                        Instant.parse("2025-12-31T23:59:59Z"));

        byte[] bytes = cborObjectMapper.writeValueAsBytes(valueToSerialize);

        ObjectMapper parser = new ObjectMapper(new CBORFactory());
        JsonNode node = parser.readTree(bytes);
        assertTrue(node.isObject());
        assertEquals("2025-01-01T00:00:00Z", node.get("signed").asText());
        assertEquals("2025-01-01T00:00:00Z", node.get("validFrom").asText());
        assertEquals("2025-12-31T23:59:59Z", node.get("validUntil").asText());
    }

    @Test
    void Should_ThrowIllegalArgumentException_When_SerializerIsNonCBORGenerator() {
        // Create a JSON ObjectMapper (not CBOR) so the serializer sees a non-CBOR generator
        ObjectMapper jsonMapper = new ObjectMapper();
        SimpleModule module = new SimpleModule();
        module.addSerializer(new ValidityInfoSerializer());
        jsonMapper.registerModule(module);
        ValidityInfo valueToSerialize =
                new ValidityInfo(
                        Instant.parse("2025-01-01T00:00:00Z"),
                        Instant.parse("2025-01-01T00:00:00Z"),
                        Instant.parse("2025-12-31T23:59:59Z"));

        JsonMappingException exception =
                assertThrows(
                        JsonMappingException.class,
                        () -> jsonMapper.writeValueAsBytes(valueToSerialize));
        assertEquals(IllegalArgumentException.class, exception.getCause().getClass());
        assertEquals("Requires CBORGenerator", exception.getCause().getMessage());
    }
}
