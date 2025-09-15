package uk.gov.di.mobile.wallet.cri.util;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.Clock;
import java.time.Instant;
import java.time.LocalDate;
import java.time.ZoneId;
import java.time.ZonedDateTime;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class ExpiryUtilTest {

    @Mock private Clock mockClock;

    private ExpiryUtil expiryUtil;

    private static final Instant FIXED_INSTANT = Instant.parse("2024-01-15T10:30:00Z");
    private static final ZoneId UTC_ZONE = ZoneId.of("UTC");

    @BeforeEach
    void setUp() {
        expiryUtil = new ExpiryUtil(mockClock);
    }

    @Test
    void Should_CalculateExpiryTimeFromTtl() {
        when(mockClock.instant()).thenReturn(FIXED_INSTANT);
        long ttlMinutes = 60;
        long expectedEpochSecond = Instant.parse("2024-01-15T11:30:00Z").getEpochSecond();

        long result = expiryUtil.calculateExpiryTimeFromTtl(ttlMinutes);

        assertEquals(expectedEpochSecond, result);
    }

    @Test
    void Should_CalculateExpiryTimeFromDate() {
        when(mockClock.getZone()).thenReturn(UTC_ZONE);
        LocalDate expiryDate = LocalDate.of(2024, 6, 15);
        long expectedEpochSecond =
                ZonedDateTime.of(2024, 6, 15, 0, 0, 0, 0, UTC_ZONE).toInstant().getEpochSecond();

        long result = expiryUtil.calculateExpiryTimeFromDate(expiryDate);

        assertEquals(expectedEpochSecond, result);
    }
}
