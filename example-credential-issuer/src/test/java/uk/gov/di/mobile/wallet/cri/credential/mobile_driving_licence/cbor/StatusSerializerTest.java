package uk.gov.di.mobile.wallet.cri.credential.mobile_driving_licence.cbor;

import com.fasterxml.jackson.databind.JsonMappingException;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.module.SimpleModule;
import com.fasterxml.jackson.dataformat.cbor.CBORFactory;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import uk.gov.di.mobile.wallet.cri.credential.mobile_driving_licence.mdoc.Status;
import uk.gov.di.mobile.wallet.cri.credential.mobile_driving_licence.mdoc.StatusList;

import java.io.IOException;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

class StatusSerializerTest {

    private ObjectMapper cborObjectMapper;

    @BeforeEach
    void setUp() {
        CBORFactory cborFactory = new CBORFactory();
        cborObjectMapper = new ObjectMapper(cborFactory);
        SimpleModule module = new SimpleModule();
        module.addSerializer(new StatusSerializer());
        cborObjectMapper.registerModule(module);
    }

    @Test
    void Should_SerializeStatus_AsDefiniteLengthMap() throws IOException {
        Status valueToSerialize = new Status(new StatusList(5, "https://test-status-list/123"));

        byte[] cborBytes = cborObjectMapper.writeValueAsBytes(valueToSerialize);

        // Definite-length, 1-entry outer map -> 0xA1
        assertEquals((byte) 0xa1, cborBytes[0]);
        int innerMapStart = 2 + 11;
        // Definite-length, 2-entry inner map -> 0xA2
        assertEquals((byte) 0xA2, cborBytes[innerMapStart]);
    }

    @Test
    void Should_SerializeStatus_ContentRoundtrip_InSnakeCase() throws IOException {
        int index = 5;
        String uri = "https://test-status-list/123";
        Status valueToSerialize = new Status(new StatusList(index, uri));
        String expectedStatusListKey = "status_list";

        byte[] cborBytes = cborObjectMapper.writeValueAsBytes(valueToSerialize);

        ObjectMapper parser = new ObjectMapper(new CBORFactory());
        JsonNode node = parser.readTree(cborBytes);
        assertTrue(node.has(expectedStatusListKey));
        JsonNode statusList = node.get(expectedStatusListKey);
        assertEquals(index, statusList.get("idx").asInt());
        assertEquals(uri, statusList.get("uri").asText());
    }

    @Test
    void Should_ThrowIllegalArgumentException_When_SerializerIsNonCBORGenerator() {
        // Create a JSON ObjectMapper (not CBOR) so the serializer sees a non-CBOR generator
        ObjectMapper jsonMapper = new ObjectMapper();
        SimpleModule module = new SimpleModule();
        module.addSerializer(new StatusSerializer());
        jsonMapper.registerModule(module);
        Status valueToSerialize = new Status(new StatusList(1, "u"));

        JsonMappingException exception =
                assertThrows(
                        JsonMappingException.class,
                        () -> jsonMapper.writeValueAsBytes(valueToSerialize));
        assertEquals(IllegalArgumentException.class, exception.getCause().getClass());
        assertEquals("Requires CBORGenerator", exception.getCause().getMessage());
    }
}
