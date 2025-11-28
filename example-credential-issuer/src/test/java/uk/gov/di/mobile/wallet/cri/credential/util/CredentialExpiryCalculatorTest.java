package uk.gov.di.mobile.wallet.cri.credential.util;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import uk.gov.di.mobile.wallet.cri.credential.DocumentStoreRecord;

import java.time.Clock;
import java.time.Instant;
import java.time.ZoneId;
import java.time.ZonedDateTime;
import java.util.HashMap;

import static org.junit.jupiter.api.Assertions.assertEquals;

class CredentialExpiryCalculatorTest {
    private static final String ITEM_ID = "550e8400-e29b-41d4-a716-446655440000";
    private static final String DOCUMENT_ID = "ABCdef123";
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
        data.put("credentialTtlMinutes", 1440); // 24 hours
        DocumentStoreRecord document =
                new DocumentStoreRecord(ITEM_ID, DOCUMENT_ID, data, "SocialSecurityCredential");
        // Expected: 2024-01-15T10:30:00Z + 1440 minutes = 2024-01-16T10:30:00Z
        long expectedEpochSecond = Instant.parse("2024-01-16T10:30:00Z").getEpochSecond();

        long result = calculator.calculateExpiry(document);

        assertEquals(expectedEpochSecond, result);
    }

    @Test
    void Should_CalculateExpiry_For_BasicDisclosureCredential() {
        HashMap<String, Object> data = new HashMap<>();
        data.put("firstName", "Bonnie");
        data.put("lastName", "Blue");
        data.put("credentialTtlMinutes", "720"); // 12 hours
        DocumentStoreRecord document =
                new DocumentStoreRecord(ITEM_ID, DOCUMENT_ID, data, "BasicDisclosureCredential");
        // Expected: 2024-01-15T10:30:00Z + 720 minutes = 2024-01-15T22:30:00Z
        long expectedEpochSecond = Instant.parse("2024-01-15T22:30:00Z").getEpochSecond();

        long result = calculator.calculateExpiry(document);

        assertEquals(expectedEpochSecond, result);
    }

    @Test
    void Should_CalculateExpiry_For_DigitalVeteranCard() {
        HashMap<String, Object> data = new HashMap<>();
        data.put("givenName", "Bonnie");
        data.put("familyName", "Blue");
        data.put("credentialTtlMinutes", "60"); // 1 hour
        DocumentStoreRecord document =
                new DocumentStoreRecord(ITEM_ID, DOCUMENT_ID, data, "DigitalVeteranCard");
        // Expected: 2024-01-15T10:30:00Z + 60 minutes = 2024-01-15T11:30:00Z
        long expectedEpochSecond = Instant.parse("2024-01-15T11:30:00Z").getEpochSecond();

        long result = calculator.calculateExpiry(document);

        assertEquals(expectedEpochSecond, result);
    }

    @Test
    void Should_CalculateExpiry_Fo_MobileDrivingLicence() {
        HashMap<String, Object> data = new HashMap<>();
        data.put("family_name", "Edwards");
        data.put("given_name", "Sarah Ann");
        data.put("credentialTtlMinutes", 43200); // 30 days
        DocumentStoreRecord document =
                new DocumentStoreRecord(ITEM_ID, DOCUMENT_ID, data, "org.iso.18013.5.1.mDL");
        // Expected: 2024-01-15T10:30:00Z + 43200 minutes = 2024-02-14T10:30:00Z
        long expectedEpochSecond =
                ZonedDateTime.of(2024, 2, 14, 10, 30, 0, 0, UTC_ZONE).toInstant().getEpochSecond();

        long result = calculator.calculateExpiry(document);

        assertEquals(expectedEpochSecond, result);
    }
}
