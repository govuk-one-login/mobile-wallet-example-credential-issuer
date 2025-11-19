package uk.gov.di.mobile.wallet.cri.credential.mobile_driving_licence.cbor;

import com.fasterxml.jackson.databind.JsonMappingException;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.module.SimpleModule;
import com.fasterxml.jackson.dataformat.cbor.CBORFactory;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import uk.gov.di.mobile.wallet.cri.credential.mobile_driving_licence.cose.COSEProtectedHeader;
import uk.gov.di.mobile.wallet.cri.credential.mobile_driving_licence.cose.COSEProtectedHeaderBuilder;

import java.io.IOException;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

class COSEProtectedHeaderSerializerTest {

    private ObjectMapper cborObjectMapper;

    @BeforeEach
    void setUp() {
        CBORFactory cborFactory = new CBORFactory();
        cborObjectMapper = new ObjectMapper(cborFactory);
        SimpleModule module = new SimpleModule();
        module.addSerializer(new COSEProtectedHeaderSerializer());
        cborObjectMapper.registerModule(module);
    }

    @Test
    void Should_SerializeCOSEProtectedHeader() throws IOException {
        COSEProtectedHeader valueToSerialize = new COSEProtectedHeaderBuilder().alg(-7).build();

        byte[] cborBytes = cborObjectMapper.writeValueAsBytes(valueToSerialize);

        // Definite-length, 1-entry map
        assertEquals((byte) 0xA1, cborBytes[0]);
        // Key 1 as CBOR integer → 0x01
        assertEquals((byte) 0x01, cborBytes[1]);
        // Value -7 as CBOR negative integer -> 0x26
        assertEquals((byte) 0x26, cborBytes[2]);
    }

    @Test
    void Should_SerializeCOSEProtectedHeader_ContentRoundtrip() throws IOException {
        int signingAlgorithm = -7; // -7="ES256"
        COSEProtectedHeader valueToSerialize =
                new COSEProtectedHeaderBuilder().alg(signingAlgorithm).build();
        int expectedAlgKey = 1; // 1="alg"

        byte[] cborBytes = cborObjectMapper.writeValueAsBytes(valueToSerialize);

        JsonNode node = cborObjectMapper.readTree(cborBytes);
        assertTrue(node.isObject());
        assertEquals(1, node.size());
        assertTrue(node.has(String.valueOf(expectedAlgKey)));
        assertEquals(signingAlgorithm, node.get(String.valueOf(expectedAlgKey)).asInt());
    }

    @Test
    void Should_ThrowIllegalArgumentException_When_SerializerIsNonCBORGenerator() {
        // Create a JSON ObjectMapper (not CBOR) so the serializer sees a non-CBOR generator
        ObjectMapper jsonMapper = new ObjectMapper();
        SimpleModule module = new SimpleModule();
        module.addSerializer(new COSEProtectedHeaderSerializer());
        jsonMapper.registerModule(module);
        COSEProtectedHeader valueToSerialize = new COSEProtectedHeaderBuilder().alg(-7).build();

        JsonMappingException exception =
                assertThrows(
                        JsonMappingException.class,
                        () -> jsonMapper.writeValueAsBytes(valueToSerialize));
        assertEquals(IllegalArgumentException.class, exception.getCause().getClass());
        assertEquals("Requires CBORGenerator", exception.getCause().getMessage());
    }
}
