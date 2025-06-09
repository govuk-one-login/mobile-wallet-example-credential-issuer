package uk.gov.di.mobile.wallet.cri.iacas;

import lombok.Getter;
import lombok.Setter;

/** Contains metadata about the IACA. */
@Getter
@Setter
public class CertificateData {
    /** The date and time (ISO 8601 format) when the certificate expires. */
    private String notAfter;

    /** The date and time (ISO 8601 format) when the certificate becomes valid. */
    private String notBefore;

    /**
     * The country code (a valid Alpha 2 country code as per ISO 3166-1) of the IACA’s issuer
     * country.
     */
    private String country;

    /** The IACA's name. */
    private String commonName;
}
