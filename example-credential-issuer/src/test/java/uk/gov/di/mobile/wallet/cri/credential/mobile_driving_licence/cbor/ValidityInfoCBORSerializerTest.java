package uk.gov.di.mobile.wallet.cri.credential.mobile_driving_licence.cbor;

import com.fasterxml.jackson.databind.JsonMappingException;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.SerializationFeature;
import com.fasterxml.jackson.databind.module.SimpleModule;
import com.fasterxml.jackson.dataformat.cbor.CBORFactory;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import uk.gov.di.mobile.wallet.cri.credential.mobile_driving_licence.mdoc.ValidityInfo;

import java.io.IOException;
import java.time.Instant;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

class ValidityInfoCBORSerializerTest {

    private ObjectMapper cborObjectMapper;
    private ValidityInfoCBORSerializer serializer;

    @BeforeEach
    void setUp() {
        CBORFactory cborFactory = new CBORFactory();
        cborObjectMapper = new ObjectMapper(cborFactory);
        SimpleModule module = new SimpleModule();
        serializer = new ValidityInfoCBORSerializer();
        module.addSerializer(ValidityInfo.class, serializer);
        module.addSerializer(Instant.class, new InstantCBORSerializer());

        cborObjectMapper
                .registerModule(module)
                .registerModule(new JavaTimeModule())
                .disable(SerializationFeature.WRITE_DATES_AS_TIMESTAMPS);
    }

    @Test
    void Should_SerializeValidityInfoWithCBORGenerator() throws IOException {
        ValidityInfo validityInfo =
                new ValidityInfo(
                        Instant.parse("2025-06-27T12:00:00Z"),
                        Instant.parse("2025-06-27T12:00:00Z"),
                        Instant.parse("2026-06-27T12:00:00Z"));

        byte[] result = cborObjectMapper.writeValueAsBytes(validityInfo);

        // Parse back the CBOR to verify structure
        CBORFactory cborFactory = new CBORFactory();
        ObjectMapper parser = new ObjectMapper(cborFactory);
        JsonNode parsedResult = parser.readTree(result);

        assertTrue(parsedResult.isObject());
        assertEquals(3, parsedResult.size());
        assertTrue(parsedResult.has("signed"));
        assertTrue(parsedResult.has("validFrom"));
        assertTrue(parsedResult.has("validTo"));
    }

    @Test
    void Should_ThrowIllegalArgumentException_When_SerializerIsNonCBORGenerator() {
        // Arrange: Create a regular JSON ObjectMapper
        ObjectMapper jsonObjectMapper = new ObjectMapper();
        SimpleModule module = new SimpleModule();
        module.addSerializer(ValidityInfo.class, serializer);
        jsonObjectMapper.registerModule(module);

        ValidityInfo validityInfo =
                new ValidityInfo(Instant.now(), Instant.now(), Instant.now().plusSeconds(3600));

        // Act & Assert: Expect JsonMappingException which wraps the IllegalArgumentException
        JsonMappingException exception =
                assertThrows(
                        JsonMappingException.class,
                        () -> jsonObjectMapper.writeValueAsBytes(validityInfo));

        // Verify the root cause is IllegalArgumentException with the expected message
        assertEquals(IllegalArgumentException.class, exception.getCause().getClass());
        assertEquals("Requires CBORGenerator", exception.getCause().getMessage());
    }
}
