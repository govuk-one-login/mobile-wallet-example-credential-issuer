package uk.gov.di.mobile.wallet.cri.util;

import java.time.Clock;
import java.time.Instant;
import java.time.LocalDate;
import java.time.temporal.ChronoUnit;

public final class ExpiryUtil {
    private final Clock clock;

    public ExpiryUtil(Clock clock) {
        this.clock = clock;
    }

    public long calculateExpiryTimeFromTtl(long ttlMinutes) {
        return Instant.now(clock).plus(ttlMinutes, ChronoUnit.MINUTES).getEpochSecond();
    }

    public long calculateExpiryTimeFromDate(LocalDate expiryDate) {
        return expiryDate.atStartOfDay(clock.getZone()).toInstant().getEpochSecond();
    }
}
