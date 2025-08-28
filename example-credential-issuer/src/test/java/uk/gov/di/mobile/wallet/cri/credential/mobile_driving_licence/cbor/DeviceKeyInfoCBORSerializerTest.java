package uk.gov.di.mobile.wallet.cri.credential.mobile_driving_licence.cbor;

import com.fasterxml.jackson.databind.JsonMappingException;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.module.SimpleModule;
import com.fasterxml.jackson.dataformat.cbor.CBORFactory;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import uk.gov.di.mobile.wallet.cri.credential.mobile_driving_licence.cose.COSEKey;
import uk.gov.di.mobile.wallet.cri.credential.mobile_driving_licence.mdoc.DeviceKeyInfo;
import uk.gov.di.mobile.wallet.cri.credential.mobile_driving_licence.mdoc.KeyAuthorizations;

import java.io.IOException;
import java.util.HashMap;
import java.util.Map;
import java.util.Set;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

class DeviceKeyInfoCBORSerializerTest {

    private ObjectMapper cborObjectMapper;
    private DeviceKeyInfoCBORSerializer serializer;

    @BeforeEach
    void setUp() {
        CBORFactory cborFactory = new CBORFactory();
        cborObjectMapper = new ObjectMapper(cborFactory);
        SimpleModule module = new SimpleModule();
        serializer = new DeviceKeyInfoCBORSerializer();
        module.addSerializer(DeviceKeyInfo.class, serializer);
        cborObjectMapper.registerModule(module);
    }

    @Test
    void Should_SerializeDeviceKeyInfoWithCBORGenerator() throws IOException {
        Map<Integer, Object> coseKeyParams = new HashMap<>();
        coseKeyParams.put(1, "testKeyType");
        coseKeyParams.put(-1, "testAlgorithm");
        COSEKey coseKey = new COSEKey(coseKeyParams);
        KeyAuthorizations keyAuthorizations =
                new KeyAuthorizations(Set.of("namespace1", "namespace2"));
        DeviceKeyInfo deviceKeyInfo = new DeviceKeyInfo(coseKey, keyAuthorizations);

        byte[] result = cborObjectMapper.writeValueAsBytes(deviceKeyInfo);

        // Verify definite length map (2 items should start with 0xA2)
        assertEquals((byte) 0xA2, result[0]);

        // Parse back the CBOR to verify structure
        CBORFactory cborFactory = new CBORFactory();
        ObjectMapper parser = new ObjectMapper(cborFactory);
        JsonNode parsedResult = parser.readTree(result);

        assertTrue(parsedResult.isObject());
        assertEquals(2, parsedResult.size());
        assertTrue(parsedResult.has("deviceKey"));
        assertTrue(parsedResult.has("keyAuthorizations"));
        JsonNode keyAuthNode = parsedResult.get("keyAuthorizations");
        assertTrue(keyAuthNode.isObject());
        assertEquals(1, keyAuthNode.size());
        assertTrue(keyAuthNode.has("nameSpaces"));
    }

    @Test
    void Should_ThrowIllegalArgumentException_When_SerializerIsNonCBORGenerator() {
        // Arrange: Create a regular JSON ObjectMapper
        ObjectMapper jsonObjectMapper = new ObjectMapper();
        SimpleModule module = new SimpleModule();
        module.addSerializer(DeviceKeyInfo.class, serializer);
        jsonObjectMapper.registerModule(module);

        Map<Integer, Object> coseKeyParams = new HashMap<>();
        coseKeyParams.put(1, "test");
        COSEKey coseKey = new COSEKey(coseKeyParams);
        KeyAuthorizations keyAuthorizations = new KeyAuthorizations(Set.of("test"));
        DeviceKeyInfo deviceKeyInfo = new DeviceKeyInfo(coseKey, keyAuthorizations);

        // Act & Assert: Expect JsonMappingException which wraps the IllegalArgumentException
        JsonMappingException exception =
                assertThrows(
                        JsonMappingException.class,
                        () -> jsonObjectMapper.writeValueAsBytes(deviceKeyInfo));

        // Verify the root cause is IllegalArgumentException with the expected message
        assertEquals(IllegalArgumentException.class, exception.getCause().getClass());
        assertEquals("Requires CBORGenerator", exception.getCause().getMessage());
    }
}
