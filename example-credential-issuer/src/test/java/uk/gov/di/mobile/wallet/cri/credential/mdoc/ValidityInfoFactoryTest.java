package uk.gov.di.mobile.wallet.cri.credential.mdoc;

import org.junit.jupiter.api.Test;

import java.time.Clock;
import java.time.Duration;
import java.time.Instant;
import java.time.ZoneOffset;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;

class ValidityInfoFactoryTest {
    private static final Instant FIXED_INSTANT = Instant.parse("2024-01-15T10:30:00Z");
    private static final long CREDENTIAL_TTL_SECONDS = 2592000L;
    private static final Instant EXPECTED_VALID_UNTIL = Instant.parse("2024-02-14T10:30:00Z");

    @Test
    void Should_CreateValidityInfoWithProvidedClock() {
        Clock fixedClock = Clock.fixed(FIXED_INSTANT, ZoneOffset.UTC);

        ValidityInfoFactory factory = new ValidityInfoFactory(fixedClock);
        ValidityInfo validityInfo = factory.build(CREDENTIAL_TTL_SECONDS, Optional.empty());

        assertEquals(FIXED_INSTANT, validityInfo.signed(), "signed should be current time");
        assertEquals(FIXED_INSTANT, validityInfo.validFrom(), "validFrom should be current time");
        assertEquals(
                EXPECTED_VALID_UNTIL,
                validityInfo.validUntil(),
                "validUntil should be 30 days later");
        assertEquals(Optional.empty(), validityInfo.expectedUpdate());
    }

    @Test
    void Should_IncludeExpectedUpdate_When_ExpectedUpdateSecondsProvided() {
        Clock fixedClock = Clock.fixed(FIXED_INSTANT, ZoneOffset.UTC);
        long expectedUpdateSeconds = 86400L;

        ValidityInfoFactory factory = new ValidityInfoFactory(fixedClock);
        ValidityInfo validityInfo =
                factory.build(CREDENTIAL_TTL_SECONDS, Optional.of(expectedUpdateSeconds));

        assertEquals(
                Optional.of(FIXED_INSTANT.plus(Duration.ofSeconds(expectedUpdateSeconds))),
                validityInfo.expectedUpdate());
    }

    @Test
    void Should_UseSystemDefaultZoneClock_When_NoClockPassedToConstructor() {
        ValidityInfoFactory factory = new ValidityInfoFactory();
        ValidityInfo validityInfo = factory.build(CREDENTIAL_TTL_SECONDS, Optional.empty());

        assertNotNull(validityInfo);
        Duration actualDuration =
                Duration.between(validityInfo.validFrom(), validityInfo.validUntil());
        assertEquals(Duration.ofSeconds(CREDENTIAL_TTL_SECONDS), actualDuration);
    }
}
