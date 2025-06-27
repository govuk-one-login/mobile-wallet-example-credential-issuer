package uk.gov.di.mobile.wallet.cri.credential.mobile_driving_licence.cbor;

import com.fasterxml.jackson.core.JsonGenerator;
import com.fasterxml.jackson.databind.SerializerProvider;
import com.fasterxml.jackson.dataformat.cbor.CBORGenerator;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InOrder;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import uk.gov.di.mobile.wallet.cri.credential.mobile_driving_licence.mdoc.MobileSecurityObject;
import uk.gov.di.mobile.wallet.cri.credential.mobile_driving_licence.mdoc.ValidityInfo;
import uk.gov.di.mobile.wallet.cri.credential.mobile_driving_licence.mdoc.ValueDigests;

import java.io.IOException;
import java.time.Instant;
import java.util.HashMap;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.inOrder;
import static org.mockito.Mockito.mock;

@ExtendWith(MockitoExtension.class)
class MobileSecurityObjectSerializerTest {

    private MobileSecurityObjectSerializer serializer;

    @Mock private CBORGenerator cborGenerator;
    @Mock private JsonGenerator regularGenerator;
    @Mock private SerializerProvider serializerProvider;

    @BeforeEach
    void setUp() {
        serializer = new MobileSecurityObjectSerializer();
    }

    @Test
    void Should_SerializeMobileSecurityObjectWithCBORGenerator() throws IOException {
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

        // Arrange: Create the test object
        MobileSecurityObject testObject =
                new MobileSecurityObject(
                        "1.0", "SHA-256", valueDigests, "org.iso.18013.5.1.mDL", validityInfo);

        // Act: Serialize the object
        serializer.serialize(testObject, cborGenerator, serializerProvider);

        // Assert: Verify the correct sequence of CBOR generator calls
        InOrder inOrder = inOrder(cborGenerator);
        inOrder.verify(cborGenerator).writeTag(24);
        inOrder.verify(cborGenerator).writeBinary(any(byte[].class));
    }

    @Test
    void Should_ThrowIllegalArgumentException_When_SerializerIsNonCBORGenerator() {
        MobileSecurityObject testObject = mock(MobileSecurityObject.class);

        IllegalArgumentException exception =
                assertThrows(
                        IllegalArgumentException.class,
                        () ->
                                serializer.serialize(
                                        testObject, regularGenerator, serializerProvider));
        assertEquals("This serializer only supports CBORGenerator", exception.getMessage());
    }
}
