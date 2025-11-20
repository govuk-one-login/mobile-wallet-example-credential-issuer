package uk.gov.di.mobile.wallet.cri.credential.mobile_driving_licence.cbor;

import com.fasterxml.jackson.databind.JsonMappingException;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.module.SimpleModule;
import com.fasterxml.jackson.dataformat.cbor.CBORFactory;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;
import uk.gov.di.mobile.wallet.cri.credential.mobile_driving_licence.Code;
import uk.gov.di.mobile.wallet.cri.credential.mobile_driving_licence.DrivingPrivilege;

import java.io.IOException;
import java.util.List;
import java.util.stream.Stream;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

class DrivingPrivilegeSerializerTest {

    private ObjectMapper cborObjectMapper;

    @BeforeEach
    void setUp() {
        CBORFactory cborFactory = new CBORFactory();
        cborObjectMapper = new ObjectMapper(cborFactory);
        SimpleModule module = new SimpleModule();
        module.addSerializer(new DrivingPrivilegeSerializer());
        module.addSerializer(new LocalDateCBORSerializer()); // Required for DrivingPrivilege
        cborObjectMapper.registerModule(module);
    }

    @ParameterizedTest
    @MethodSource("provideDrivingPrivilegeTestCases")
    void shouldSerializeDrivingPrivilege(DrivingPrivilege valueToSerialize, byte expectedFirstByte)
            throws IOException {
        byte[] cborBytes = cborObjectMapper.writeValueAsBytes(valueToSerialize);
        assertEquals(expectedFirstByte, cborBytes[0]);
    }

    private static Stream<Arguments> provideDrivingPrivilegeTestCases() {
        return Stream.of(
                Arguments.of(
                        new DrivingPrivilege("B1", null, null, null), (byte) 0xA1), // 1-entry map
                Arguments.of(
                        new DrivingPrivilege("B1", "01-01-2024", null, null),
                        (byte) 0xA2), // 2-entry map
                Arguments.of(
                        new DrivingPrivilege("B1", "01-01-2024", "31-12-2025", null),
                        (byte) 0xA3), // 3-entry map
                Arguments.of(
                        new DrivingPrivilege(
                                "B1",
                                "01-01-2024",
                                "31-12-2025",
                                List.of(new Code("A"), new Code("B"))),
                        (byte) 0xA4) // 4-entry map
                );
    }

    @Test
    void Should_SerializeDrivingPrivilege_ContentRoundtrip_InSnakeCase() throws IOException {
        DrivingPrivilege valueToSerialize =
                new DrivingPrivilege(
                        "B1", "01-01-2024", "31-12-2025", List.of(new Code("A"), new Code("B")));
        byte[] cborBytes = cborObjectMapper.writeValueAsBytes(valueToSerialize);

        JsonNode parsedResult = cborObjectMapper.readTree(cborBytes);
        assertTrue(parsedResult.isObject());
        assertTrue(
                parsedResult.has("vehicle_category_code"),
                "vehicleCategoryCode key should be serialized to vehicle_category_code");
        assertTrue(
                parsedResult.has("issue_date"), "issueDate key should be serialized to issue_date");
        assertTrue(
                parsedResult.has("expiry_date"),
                "expiryDate key should be serialized to expiry_date");
        assertTrue(parsedResult.has("codes"), "should have codes key");
        assertEquals("B1", parsedResult.get("vehicle_category_code").asText());
        assertEquals("2024-01-01", parsedResult.get("issue_date").asText());
        assertEquals("2025-12-31", parsedResult.get("expiry_date").asText());
        JsonNode codes = parsedResult.get("codes");
        assertTrue(codes.isArray());
        assertEquals(2, codes.size());
        assertEquals("A", codes.get(0).get("code").asText());
        assertEquals("B", codes.get(1).get("code").asText());
    }

    @Test
    void Should_ThrowIllegalArgumentException_When_SerializerIsNonCBORGenerator() {
        // Create a JSON ObjectMapper (not CBOR) so the serializer sees a non-CBOR generator
        ObjectMapper jsonObjectMapper = new ObjectMapper();
        SimpleModule module = new SimpleModule();
        module.addSerializer(new DrivingPrivilegeSerializer());
        jsonObjectMapper.registerModule(module);
        DrivingPrivilege valueToSerialize =
                new DrivingPrivilege("A", "01-01-2020", "02-02-2020", List.of());

        JsonMappingException exception =
                assertThrows(
                        JsonMappingException.class,
                        () -> jsonObjectMapper.writeValueAsBytes(valueToSerialize));
        assertEquals(IllegalArgumentException.class, exception.getCause().getClass());
        assertEquals("Requires CBORGenerator", exception.getCause().getMessage());
    }
}
