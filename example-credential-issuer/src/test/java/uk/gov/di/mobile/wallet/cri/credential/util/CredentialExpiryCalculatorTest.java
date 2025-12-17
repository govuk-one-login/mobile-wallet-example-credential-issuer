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
    void Should_CalculateCredentialExpiry() {
        HashMap<String, Object> data = new HashMap<>();
        long credentialTtlMinutes = 43200; // 30 days
        DocumentStoreRecord document =
                new DocumentStoreRecord(
                        ITEM_ID, DOCUMENT_ID, data, "org.iso.18013.5.1.mDL", credentialTtlMinutes);
        // Expected: 2024-01-15T10:30:00Z + 43200 minutes = 2024-02-14T10:30:00Z
        long expectedEpochSecond =
                ZonedDateTime.of(2024, 2, 14, 10, 30, 0, 0, UTC_ZONE).toInstant().getEpochSecond();

        long result = calculator.calculateExpiry(document);

        assertEquals(expectedEpochSecond, result);
    }
}
