package uk.gov.di.mobile.wallet.cri.util;

import java.time.Instant;
import java.time.LocalDate;
import java.time.ZoneId;
import java.time.temporal.ChronoUnit;

public final class ExpiryUtil {
    private ExpiryUtil() {}

    /** Returns the epoch-second at now + ttlMinutes */
    public static long calculateExpiryTimeFromTtl(long ttlMinutes) {
        return Instant.now().plus(ttlMinutes, ChronoUnit.MINUTES).getEpochSecond();
    }

    /** Returns the epoch-second at the start of the given date in the system default time zone */
    public static long calculateExpiryTimeFromDate(LocalDate expiryDate) {
        return expiryDate.atStartOfDay(ZoneId.systemDefault()).toInstant().getEpochSecond();
    }
}
