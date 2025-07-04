package uk.gov.di.mobile.wallet.cri.credential.mobile_driving_licence.cbor;

import com.fasterxml.jackson.dataformat.cbor.databind.CBORMapper;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import uk.gov.di.mobile.wallet.cri.credential.mobile_driving_licence.cose.COSESign1;
import uk.gov.di.mobile.wallet.cri.credential.mobile_driving_licence.mdoc.IssuerSigned;

import java.time.LocalDate;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.*;

class JacksonCBOREncoderProviderTest {

    private CBORMapper mapper;

    @BeforeEach
    void setUp() {
        mapper = JacksonCBOREncoderProvider.configuredCBORMapper();
    }

    @Test
    void Should_ReturnNonNullMapper_When_CBORMapperIsConfigured() {
        assertNotNull(mapper, "Configured CBOR mapper should not be null");
    }

    @Test
    void Should_SerializeLocalDate() throws Exception {
        // Create a LocalDate object for testing
        Map<String, Object> testObj = new HashMap<>();
        LocalDate testDate = LocalDate.of(2025, 4, 4);
        testObj.put("date", testDate);

        // Serialize to bytes
        byte[] serialized = mapper.writeValueAsBytes(testObj);
        assertNotNull(serialized, "Serialization of LocalDate should work");
        assertTrue(serialized.length > 0, "Serialized data should not be empty");

        // Deserialize back to ensure correct serialization
        Map<?, ?> deserialized = mapper.readValue(serialized, Map.class);
        assertNotNull(
                deserialized.get("date"), "Date field should be present after deserialization");
    }

    @Test
    void Should_SerializeIssuerSigned() throws Exception {
        // Create an IssuerSigned object for testing
        IssuerSigned testIssuerSigned = createTestIssuerSigned();
        Map<String, Object> testObj = new HashMap<>();
        testObj.put("issuerSigned", testIssuerSigned);

        // Serialize to bytes
        byte[] serialized = mapper.writeValueAsBytes(testObj);
        assertNotNull(serialized, "Serialization of IssuerSigned should work");
        assertTrue(serialized.length > 0, "Serialized data should not be empty");

        // Deserialize back to ensure correct serialization
        Map<?, ?> deserialized = mapper.readValue(serialized, Map.class);
        assertNotNull(
                deserialized.get("issuerSigned"),
                "Date field should be present after deserialization");
    }

    private IssuerSigned createTestIssuerSigned() {
        byte[] issuerSignedItemBytes = {1, 2, 3, 4};

        byte[] protectedHeaderBytes = {1, 2, 3, 4};
        Map<Integer, Object> unprotectedHeader = new HashMap<>();
        byte[] payloadBytes = {1, 2, 3, 4};
        byte[] signatureBytes = {1, 2, 3, 4};

        Map<String, List<byte[]>> testNameSpaces =
                Map.of("namespace", List.of(issuerSignedItemBytes));
        return new IssuerSigned(
                testNameSpaces,
                new COSESign1(
                        protectedHeaderBytes, unprotectedHeader, payloadBytes, signatureBytes));
    }
}
