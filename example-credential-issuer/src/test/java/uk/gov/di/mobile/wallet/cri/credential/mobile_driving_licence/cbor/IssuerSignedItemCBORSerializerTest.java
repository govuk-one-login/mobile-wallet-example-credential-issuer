package uk.gov.di.mobile.wallet.cri.credential.mobile_driving_licence.cbor;

import com.fasterxml.jackson.databind.JsonMappingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.module.SimpleModule;
import com.fasterxml.jackson.dataformat.cbor.CBORFactory;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import uk.gov.di.mobile.wallet.cri.credential.mobile_driving_licence.mdoc.IssuerSignedItem;

import java.io.IOException;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;

class IssuerSignedItemCBORSerializerTest {

    private ObjectMapper cborObjectMapper;
    private IssuerSignedItemCBORSerializer serializer;

    @BeforeEach
    void setUp() {
        CBORFactory cborFactory = new CBORFactory();
        cborObjectMapper = new ObjectMapper(cborFactory);
        SimpleModule module = new SimpleModule();
        serializer = new IssuerSignedItemCBORSerializer();
        module.addSerializer(IssuerSignedItem.class, serializer);
        cborObjectMapper.registerModule(module);
    }

    @Test
    void Should_SerializeIssuerSignedItemWithCBORGenerator() throws IOException {
        IssuerSignedItem issuerSignedItem =
                new IssuerSignedItem(1, new byte[] {0x01, 0x02, 0x03}, "testElement", "testValue");

        byte[] result = cborObjectMapper.writeValueAsBytes(issuerSignedItem);

        assertEquals((byte) 0xD8, result[0]); // CBOR tag 24 byte 1
        assertEquals((byte) 0x18, result[1]); // CBOR tag 24 byte 2
    }

    @Test
    void Should_ThrowIllegalArgumentException_When_SerializerIsNonCBORGenerator()
            throws IOException {
        // Arrange: Create a regular JSON ObjectMapper
        ObjectMapper jsonObjectMapper = new ObjectMapper();
        SimpleModule module = new SimpleModule();
        module.addSerializer(IssuerSignedItem.class, serializer);
        jsonObjectMapper.registerModule(module);

        IssuerSignedItem issuerSignedItem =
                new IssuerSignedItem(1, new byte[] {0x01}, "test", "value");

        // Act & Assert: Expect JsonMappingException which wraps the IllegalArgumentException
        JsonMappingException exception =
                assertThrows(
                        JsonMappingException.class,
                        () -> jsonObjectMapper.writeValueAsBytes(issuerSignedItem));

        // Verify the root cause is IllegalArgumentException with the expected message
        assertEquals(IllegalArgumentException.class, exception.getCause().getClass());
        assertEquals("Requires CBORGenerator", exception.getCause().getMessage());
    }
}
