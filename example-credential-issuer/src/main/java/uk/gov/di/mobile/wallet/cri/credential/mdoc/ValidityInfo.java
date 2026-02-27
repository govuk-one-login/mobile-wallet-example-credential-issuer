package uk.gov.di.mobile.wallet.cri.credential.mdoc;

import java.time.Instant;

/**
 * Record describing the validity information for a {@link MobileSecurityObject}.
 *
 * <p>This record captures the time window during which a Mobile Security Object (MSO) is considered
 * valid. It includes the time of signing, as well as the start and end of the validity period.
 */
public record ValidityInfo(
        /*
         The instant at which the MobileSecurityObject was signed.
        */
        Instant signed,

        /*
         The instant from which the MobileSecurityObject is considered valid.
         The object should not be used for verification before this time.
        */
        Instant validFrom,

        /*
         The instant until which the MobileSecurityObject is considered valid.
         The object should not be used for verification after this time.
        */
        Instant validUntil) {}
