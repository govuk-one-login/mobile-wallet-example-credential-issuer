package uk.gov.di.mobile.wallet.cri.credential.mobile_driving_licence.mdoc;

import uk.gov.di.mobile.wallet.cri.credential.mobile_driving_licence.DrivingLicenceDocument;

import java.time.Clock;
import java.time.Duration;
import java.time.Instant;

/**
 * Factory for creating {@link ValidityInfo} objects.
 *
 * <p>This factory encapsulates the logic for creating validity information used in mobile security
 * objects.
 */
public class ValidityInfoFactory {

    /** The source of current time for validity information. */
    private final Clock clock;

    private DrivingLicenceDocument document;

    /** Constructs a new {@link ValidityInfoFactory}. */
    public ValidityInfoFactory() {

        this.clock = Clock.systemDefaultZone();
    }

    /**
     * Constructs a new {@link ValidityInfoFactory} with the specified clock.
     *
     * @param clock The source of current time for validity information.
     */
    public ValidityInfoFactory(Clock clock) {

        this.clock = clock;
    }

    /**
     * Creates a {@link ValidityInfo} object with a one-year validity period.
     *
     * <p>The validity period starts from the current time and extends for 365 days. Both the signed
     * and valid from timestamps are set to the current time.
     *
     * @return A {@link ValidityInfo} object with current time as signed/valid from and current time
     *     plus 365 days as valid until.
     */
    public ValidityInfo build(long credentialTtlMinutes) {
        Instant currentTimestamp = clock.instant();
        Instant validUntil = currentTimestamp.plus(Duration.ofMinutes(credentialTtlMinutes));
        return new ValidityInfo(currentTimestamp, currentTimestamp, validUntil);
    }
}
