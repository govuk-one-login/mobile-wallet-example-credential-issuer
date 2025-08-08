package uk.gov.di.mobile.wallet.cri.util;

import org.junit.jupiter.api.Test;

import java.time.Instant;
import java.time.LocalDate;
import java.time.ZoneId;
import java.time.temporal.ChronoUnit;

import static org.junit.jupiter.api.Assertions.*;

public class ExpiryUtilTest {

    @Test
    void calculateExpiryTimeFromTtl_positiveTtl() {
        long ttlMinutes = 100;
        long expectedExpiryTime =
                Instant.now().plus(ttlMinutes, ChronoUnit.MINUTES).getEpochSecond();
        long actualExpiryTime = ExpiryUtil.calculateExpiryTimeFromTtl(ttlMinutes);
        // Allow for a small margin of error due to the time it takes to run the test
        assertTrue(Math.abs(expectedExpiryTime - actualExpiryTime) <= 1);
    }

    @Test
    void calculateExpiryTimeFromTtl_zeroTtl() {
        long ttlMinutes = 0;
        long expectedExpiryTime = Instant.now().getEpochSecond();
        long actualExpiryTime = ExpiryUtil.calculateExpiryTimeFromTtl(ttlMinutes);
        // Allow for a small margin of error due to the time it takes to run the test
        assertTrue(Math.abs(expectedExpiryTime - actualExpiryTime) <= 1);
    }

    @Test
    void calculateExpiryTimeFromTtl_negativeTtl() {
        long ttlMinutes = -10;
        long expectedExpiryTime =
                Instant.now().plus(ttlMinutes, ChronoUnit.MINUTES).getEpochSecond();
        long actualExpiryTime = ExpiryUtil.calculateExpiryTimeFromTtl(ttlMinutes);
        // Allow for a small margin of error due to the time it takes to run the test
        assertTrue(Math.abs(expectedExpiryTime - actualExpiryTime) <= 1);
    }

    @Test
    void calculateExpiryTimeFromDate_futureDate() {
        LocalDate futureDate = LocalDate.now().plusDays(10);
        long expectedExpiryTime =
                futureDate.atStartOfDay(ZoneId.systemDefault()).toInstant().getEpochSecond();
        long actualExpiryTime = ExpiryUtil.calculateExpiryTimeFromDate(futureDate);
        assertEquals(expectedExpiryTime, actualExpiryTime);
    }

    @Test
    void calculateExpiryTimeFromDate_pastDate() {
        LocalDate pastDate = LocalDate.now().minusDays(10);
        long expectedExpiryTime =
                pastDate.atStartOfDay(ZoneId.systemDefault()).toInstant().getEpochSecond();
        long actualExpiryTime = ExpiryUtil.calculateExpiryTimeFromDate(pastDate);
        assertEquals(expectedExpiryTime, actualExpiryTime);
    }

    @Test
    void calculateExpiryTimeFromDate_today() {
        LocalDate today = LocalDate.now();
        long expectedExpiryTime =
                today.atStartOfDay(ZoneId.systemDefault()).toInstant().getEpochSecond();
        long actualExpiryTime = ExpiryUtil.calculateExpiryTimeFromDate(today);
        assertEquals(expectedExpiryTime, actualExpiryTime);
    }

    @Test
    void calculateExpiryTimeFromDate_nullDate() {
        assertThrows(
                NullPointerException.class, () -> ExpiryUtil.calculateExpiryTimeFromDate(null));
    }
}
