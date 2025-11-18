package uk.gov.di.mobile.wallet.cri.credential.mobile_driving_licence.cbor;

import com.fasterxml.jackson.databind.JsonMappingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.module.SimpleModule;
import com.fasterxml.jackson.dataformat.cbor.CBORFactory;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;
import org.jetbrains.annotations.NotNull;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
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
import static org.junit.jupiter.api.Assertions.assertTrue;
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

    @DisplayName(
            "Should serialize MobileSecurityObject with CBOR tag 24)and definite-length byte string")
    @Test
    void shouldSerializeMobileSecurityObject() throws IOException {
        MobileSecurityObject testObject = getTestMobileSecurityObject();

        byte[] result = cborObjectMapper.writeValueAsBytes(testObject);

        // The first two bytes should encode tag 24, indicating that the following data is
        // CBOR-encoded.
        // 0xD8: major type 6 (tag) with additional info 24
        // 0x18: tag number 24
        assertEquals((byte) 0xD8, result[0]);
        assertEquals((byte) 0x18, result[1]);

        // The third byte should signal a definite-length byte string
        // Top 3 bits (0xE0 mask) == 0x40 means major type 2 (byte string)
        // Lower 5 bits (0x1F mask) != 0x1F means definite length (not indefinite)
        byte initialByte = result[2];
        boolean isByteStringMajorType = (initialByte & 0xE0) == 0x40;
        boolean isDefiniteLength = (initialByte & 0x1F) != 0x1F;
        assertTrue(isByteStringMajorType && isDefiniteLength);
    }

    @DisplayName("Should throw if serializer is used with a non-CBOR generator")
    @Test
    void shouldThrowWhenSerializerUsesNonCborGenerator() {
        // Create a JSON ObjectMapper (not CBOR) so the serializer sees a non-CBOR generator
        ObjectMapper jsonObjectMapper = new ObjectMapper();
        SimpleModule module = new SimpleModule();
        module.addSerializer(MobileSecurityObject.class, serializer);
        jsonObjectMapper.registerModule(module);

        MobileSecurityObject testObject = mock(MobileSecurityObject.class);

        JsonMappingException exception =
                assertThrows(
                        JsonMappingException.class,
                        () -> jsonObjectMapper.writeValueAsBytes(testObject));
        assertEquals(IllegalArgumentException.class, exception.getCause().getClass());
        assertEquals("Requires CBORGenerator", exception.getCause().getMessage());
    }

    private static @NotNull MobileSecurityObject getTestMobileSecurityObject() {
        byte[] x = new byte[] {0x01, 0x02, 0x03};
        byte[] y = new byte[] {0x04, 0x05, 0x06};
        COSEKey coseKey = new COSEKey(COSEKeyTypes.EC2, COSEEllipticCurves.P256, x, y);
        KeyAuthorizations keyAuthorizations =
                new KeyAuthorizations(Set.of("testNamespace1", "testNamespace2"));
        DeviceKeyInfo deviceKeyInfo = new DeviceKeyInfo(coseKey, keyAuthorizations);

        Map<Integer, byte[]> digestMap = new HashMap<>();
        digestMap.put(1, new byte[] {0x01, 0x02, 0x03});
        Map<String, Map<Integer, byte[]>> valueDigestsMap = new HashMap<>();
        valueDigestsMap.put("org.iso.18013.5.1", digestMap);
        ValueDigests valueDigests = new ValueDigests(valueDigestsMap);

        ValidityInfo validityInfo =
                new ValidityInfo(
                        Instant.parse("2025-06-27T12:00:00Z"),
                        Instant.parse("2025-06-27T12:00:00Z"),
                        Instant.parse("2026-06-27T12:00:00Z"));

        StatusList statusList = new StatusList(0, "https://test-status-list.gov.uk/t/3B0F3BD087A7");
        Status status = new Status(statusList);

        return new MobileSecurityObject(
                "1.0",
                "SHA-256",
                deviceKeyInfo,
                valueDigests,
                "org.iso.18013.5.1.mDL",
                validityInfo,
                status);
    }
}
