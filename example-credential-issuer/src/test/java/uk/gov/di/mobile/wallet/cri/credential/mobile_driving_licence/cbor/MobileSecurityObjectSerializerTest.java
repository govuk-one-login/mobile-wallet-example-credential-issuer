package uk.gov.di.mobile.wallet.cri.credential.mobile_driving_licence.cbor;

import com.fasterxml.jackson.databind.JsonMappingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.module.SimpleModule;
import com.fasterxml.jackson.dataformat.cbor.CBORFactory;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import uk.gov.di.mobile.wallet.cri.credential.mobile_driving_licence.cose.COSEKey;
import uk.gov.di.mobile.wallet.cri.credential.mobile_driving_licence.cose.constants.COSEEllipticCurves;
import uk.gov.di.mobile.wallet.cri.credential.mobile_driving_licence.cose.constants.COSEKeyTypes;
import uk.gov.di.mobile.wallet.cri.credential.mobile_driving_licence.mdoc.DeviceKeyInfo;
import uk.gov.di.mobile.wallet.cri.credential.mobile_driving_licence.mdoc.KeyAuthorizations;
import uk.gov.di.mobile.wallet.cri.credential.mobile_driving_licence.mdoc.MobileSecurityObject;
import uk.gov.di.mobile.wallet.cri.credential.mobile_driving_licence.mdoc.Status;
import uk.gov.di.mobile.wallet.cri.credential.mobile_driving_licence.mdoc.StatusList;
import uk.gov.di.mobile.wallet.cri.credential.mobile_driving_licence.mdoc.ValidityInfo;
import uk.gov.di.mobile.wallet.cri.credential.mobile_driving_licence.mdoc.ValueDigests;

import java.io.IOException;
import java.time.Instant;
import java.util.HashMap;
import java.util.Map;
import java.util.Set;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.Mockito.mock;

class MobileSecurityObjectSerializerTest {

    private ObjectMapper cborObjectMapper;
    private MobileSecurityObjectSerializer serializer;

    @BeforeEach
    void setUp() {
        CBORFactory cborFactory = new CBORFactory();
        cborObjectMapper = new ObjectMapper(cborFactory);
        SimpleModule module = new SimpleModule();
        serializer = new MobileSecurityObjectSerializer();
        module.addSerializer(MobileSecurityObject.class, serializer);
        cborObjectMapper.registerModule(module).registerModule(new JavaTimeModule());
    }

    @Test
    void Should_SerializeMobileSecurityObjectWithCBORGenerator() throws IOException {
        byte[] x = new byte[] {0x01, 0x02, 0x03};
        byte[] y = new byte[] {0x04, 0x05, 0x06};
        COSEKey coseKey = new COSEKey(COSEKeyTypes.EC2, COSEEllipticCurves.P256, x, y);
        KeyAuthorizations keyAuthorizations =
                new KeyAuthorizations(Set.of("testNamespace1", "testNamespace2"));
        DeviceKeyInfo deviceKeyInfo = new DeviceKeyInfo(coseKey, keyAuthorizations);

        // Arrange: Prepare ValueDigests
        Map<Integer, byte[]> digestMap = new HashMap<>();
        digestMap.put(1, new byte[] {0x01, 0x02, 0x03});
        Map<String, Map<Integer, byte[]>> valueDigestsMap = new HashMap<>();
        valueDigestsMap.put("org.iso.18013.5.1", digestMap);
        ValueDigests valueDigests = new ValueDigests(valueDigestsMap);

        // Arrange: Prepare ValidityInfo
        ValidityInfo validityInfo =
                new ValidityInfo(
                        Instant.parse("2025-06-27T12:00:00Z"),
                        Instant.parse("2025-06-27T12:00:00Z"),
                        Instant.parse("2026-06-27T12:00:00Z"));

        // Arrange: Prepare Status
        StatusList statusList = new StatusList(0, "https://test-status-list.gov.uk/t/3B0F3BD087A7");
        Status status = new Status(statusList);

        // Arrange: Create the test object
        MobileSecurityObject testObject =
                new MobileSecurityObject(
                        "1.0",
                        "SHA-256",
                        deviceKeyInfo,
                        valueDigests,
                        "org.iso.18013.5.1.mDL",
                        validityInfo,
                        status);

        // Act: Serialize the object using ObjectMapper
        byte[] result = cborObjectMapper.writeValueAsBytes(testObject);

        // Assert: The result should start with CBOR tag 24
        assertEquals(((byte) 0xD8), result[0]);
        assertEquals(((byte) 0x18), result[1]);
    }

    @Test
    void Should_ThrowIllegalArgumentException_When_SerializerIsNonCBORGenerator() {
        // Arrange: Create a regular JSON ObjectMapper
        ObjectMapper jsonObjectMapper = new ObjectMapper();
        SimpleModule module = new SimpleModule();
        module.addSerializer(MobileSecurityObject.class, serializer);
        jsonObjectMapper.registerModule(module);

        MobileSecurityObject testObject = mock(MobileSecurityObject.class);

        // Act & Assert: Expect JsonMappingException which wraps the IllegalArgumentException
        JsonMappingException exception =
                assertThrows(
                        JsonMappingException.class,
                        () -> jsonObjectMapper.writeValueAsBytes(testObject));

        // Verify the root cause is IllegalArgumentException with the expected message
        assertEquals(IllegalArgumentException.class, exception.getCause().getClass());
        assertEquals("Requires CBORGenerator", exception.getCause().getMessage());
    }
}
