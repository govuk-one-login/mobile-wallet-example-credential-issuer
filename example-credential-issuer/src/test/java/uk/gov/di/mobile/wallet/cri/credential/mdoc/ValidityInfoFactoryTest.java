package uk.gov.di.mobile.wallet.cri.credential.mdoc;

import org.junit.jupiter.api.Test;

import java.time.Clock;
import java.time.Duration;
import java.time.Instant;
import java.time.ZoneOffset;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

class ValidityInfoFactoryTest {
    private static final Instant FIXED_INSTANT = Instant.parse("2024-01-15T10:30:00Z");
    private static final long CREDENTIAL_TTL_MINUTES = 43200L;

    @Test
    void Should_CreateValidityInfoWithProvidedClock() {
        Clock fixedClock = Clock.fixed(FIXED_INSTANT, ZoneOffset.UTC);

        ValidityInfoFactory factory = new ValidityInfoFactory(fixedClock);
        ValidityInfo validityInfo = factory.build(CREDENTIAL_TTL_MINUTES);

        assertEquals(FIXED_INSTANT, validityInfo.signed(), "signed should be current time");
        assertEquals(FIXED_INSTANT, validityInfo.validFrom(), "validFrom should be current time");
        assertEquals(
                FIXED_INSTANT.plus(Duration.ofMinutes(CREDENTIAL_TTL_MINUTES)),
                validityInfo.validUntil(),
                "validUntil should be 30 days later");
    }

    @Test
    void Should_UseSystemDefaultZoneClock_When_NoClockPassedToConstructor() {
        Instant beforeCreation = Instant.now();

        ValidityInfoFactory factory = new ValidityInfoFactory(Clock.systemDefaultZone());
        ValidityInfo validityInfo = factory.build(CREDENTIAL_TTL_MINUTES);

        assertNotNull(validityInfo);
        Instant afterCreation = Instant.now();
        // Verify timestamps are within reasonable bounds
        assertTrue(
                validityInfo.signed().isAfter(beforeCreation)
                        || validityInfo.signed().equals(beforeCreation));
        assertTrue(
                validityInfo.validFrom().isBefore(afterCreation)
                        || validityInfo.validFrom().equals(afterCreation));
        // Verify the duration is exactly 30 days
        Duration actualDuration =
                Duration.between(validityInfo.validFrom(), validityInfo.validUntil());

        assertEquals(Duration.ofMinutes(CREDENTIAL_TTL_MINUTES), actualDuration);
    }
}
