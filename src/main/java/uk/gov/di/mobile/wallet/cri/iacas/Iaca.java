package uk.gov.di.mobile.wallet.cri.iacas;

import lombok.Getter;
import lombok.Setter;

/**
 * Represents an IACA (Issuing Authority Certificate Authority).
 *
 * <p>The IACA is an X.509 certificate that serves as the trust anchor (root or top-level CA) for a
 * specific mDoc issuer, and is used to sign Document Signer Certificates, which in turn sign Mobile
 * Security Objects (MSOs).
 */
@Getter
@Setter
public class Iaca {
    /** Unique identifier for the IACA. */
    private String id;

    /** Indicates whether this IACA is currently active and valid for use. */
    private boolean active;

    /** PEM-encoded X.509 certificate. */
    private String certificatePem;

    /** Metadata about the IACA. */
    private CertificateData certificateData;

    /** The fingerprint (cryptographic hash) of the IACA. */
    private String certificateFingerprint;

    /** The public key of the IACA in JWK (JSON Web Key) format. */
    private PublicKeyJwk publicKeyJwk;
}
