package uk.gov.di.mobile.wallet.cri.credential.util;

import uk.gov.di.mobile.wallet.cri.credential.DocumentStoreRecord;

import java.time.Clock;
import java.time.Instant;
import java.time.temporal.ChronoUnit;

public class CredentialExpiryCalculator {

    private final Clock clock;

    public CredentialExpiryCalculator() {
        this(Clock.systemDefaultZone());
    }

    public CredentialExpiryCalculator(Clock clock) {
        this.clock = clock;
    }

    public long calculateExpiry(DocumentStoreRecord record) {
        return Instant.now(clock)
                .plus(record.getCredentialTtlMinutes(), ChronoUnit.MINUTES)
                .getEpochSecond();
    }
}
