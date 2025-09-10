package uk.gov.di.mobile.wallet.cri.credential;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.time.Clock;
import java.time.Instant;
import java.time.ZoneId;
import java.time.ZonedDateTime;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.assertEquals;

class CredentialExpiryCalculatorTest {
    private static final String DOCUMENT_ID = "550e8400-e29b-41d4-a716-446655440000";
    private static final Instant FIXED_INSTANT = Instant.parse("2024-01-15T10:30:00Z");
    private static final ZoneId UTC_ZONE = ZoneId.of("UTC");
    private static final Clock FIXED_CLOCK = Clock.fixed(FIXED_INSTANT, UTC_ZONE);

    private static CredentialExpiryCalculator calculator;

    @BeforeEach
    void setUp() {
        calculator = new CredentialExpiryCalculator(FIXED_CLOCK);
    }

    @Test
    void Should_CalculateExpiry_For_SocialSecurityCredential() {
        HashMap<String, Object> data = new HashMap<>();
        data.put("familyName", "Edwards Green");
        data.put("givenName", "Sarah Elizabeth");
        data.put("nino", "QQ123456C");
        data.put("title", "Miss");
        data.put("credentialTtlMinutes", "1440"); // 24 hours
        Document document = new Document(DOCUMENT_ID, data, "SocialSecurityCredential");

        // Expected: 2024-01-15T10:30:00Z + 1440 minutes = 2024-01-16T10:30:00Z
        long expectedEpochSecond = Instant.parse("2024-01-16T10:30:00Z").getEpochSecond();

        long result = calculator.calculateExpiry(document);

        assertEquals(expectedEpochSecond, result);
    }

    @Test
    void Should_CalculateExpiry_For_BasicDisclosureCredential() {
        HashMap<String, Object> data = new HashMap<>();
        data.put("issuance-day", "11");
        data.put("issuance-month", "07");
        data.put("issuance-year", "2024");
        data.put("expiration-day", "11");
        data.put("expiration-month", "07");
        data.put("expiration-year", "2025");
        data.put("birth-day", "05");
        data.put("birth-month", "12");
        data.put("birth-year", "1970");
        data.put("firstName", "Bonnie");
        data.put("lastName", "Blue");
        data.put("subBuildingName", "Flat 11");
        data.put("buildingName", "Blashford");
        data.put("streetName", "Adelaide Road");
        data.put("addressLocality", "London");
        data.put("addressCountry", "GB");
        data.put("postalCode", "NW3 3RX");
        data.put("certificateNumber", "009878863");
        data.put("applicationNumber", "E0023455534");
        data.put("certificateType", "basic");
        data.put("outcome", "Result clear");
        data.put("policeRecordsCheck", "Clear");
        data.put("credentialTtlMinutes", "720"); // 12 hours
        Document document = new Document(DOCUMENT_ID, data, "BasicDisclosureCredential");

        // Expected: 2024-01-15T10:30:00Z + 720 minutes = 2024-01-15T22:30:00Z
        long expectedEpochSecond = Instant.parse("2024-01-15T22:30:00Z").getEpochSecond();

        long result = calculator.calculateExpiry(document);

        assertEquals(expectedEpochSecond, result);
    }

    @Test
    void Should_CalculateExpiry_For_DigitalVeteranCard() {
        HashMap<String, Object> data = new HashMap<>();
        data.put("cardExpiryDate-day", "11");
        data.put("cardExpiryDate-month", "07");
        data.put("cardExpiryDate-year", "2000");
        data.put("dateOfBirth-day", "05");
        data.put("dateOfBirth-month", "12");
        data.put("dateOfBirth-year", "1970");
        data.put("givenName", "Bonnie");
        data.put("familyName", "Blue");
        data.put("serviceNumber", "25057386");
        data.put("serviceBranch", "HM Naval Service");
        data.put("photo", "base64EncodedPhoto");
        data.put("credentialTtlMinutes", "60"); // 1 hour
        Document document = new Document("documentId", data, "DigitalVeteranCard");

        // Expected: 2024-01-15T10:30:00Z + 60 minutes = 2024-01-15T11:30:00Z
        long expectedEpochSecond = Instant.parse("2024-01-15T11:30:00Z").getEpochSecond();

        long result = calculator.calculateExpiry(document);

        assertEquals(expectedEpochSecond, result);
    }

    @Test
    void Should_CalculateExpiry_Fo_MobileDrivingLicence() {
        List<Map<String, String>> drivingPrivileges = new ArrayList<>();
        Map<String, String> drivingPrivilege = new HashMap<>();
        drivingPrivilege.put("vehicle_category_code", "A");
        drivingPrivileges.add(drivingPrivilege);
        HashMap<String, Object> data = new HashMap<>();
        data.put("family_name", "Edwards");
        data.put("given_name", "Sarah Ann");
        data.put("title", "Miss");
        data.put("welsh_licence", false);
        data.put("portrait", "VGhpcyBpcyBhIHRlc3Qgc3RyaW5nLg==");
        data.put("birth_date", "01-02-2000");
        data.put("birth_place", "London");
        data.put("issue_date", "08-04-2020");
        data.put("expiry_date", "15-06-2025");
        data.put("issuing_authority", "TEST");
        data.put("issuing_country", "GB");
        data.put("document_number", "123456789");
        data.put("resident_address", new String[] {"Flat 2a", "64 Berry Street"});
        data.put("resident_postal_code", "N1 7FN");
        data.put("resident_city", "London");
        data.put("driving_privileges", drivingPrivileges);
        data.put("un_distinguishing_sign", "UK");
        Document document = new Document("documentId", data, "org.iso.18013.5.1.mDL");

        // Expected: 2025-06-15T00:00:00Z (start of day in UTC)
        long expectedEpochSecond =
                ZonedDateTime.of(2025, 6, 15, 0, 0, 0, 0, UTC_ZONE).toInstant().getEpochSecond();

        long result = calculator.calculateExpiry(document);

        assertEquals(expectedEpochSecond, result);
    }
}
