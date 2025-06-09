package uk.gov.di.mobile.wallet.cri.iacas;

import lombok.Getter;
import lombok.Setter;

/** Represents the IACA's public key in JSON Web Key (JWK) format. */
@Getter
@Setter
public class PublicKeyJwk {
    /** The key type (e.g., "EC" for Elliptic Curve). */
    private String kty;

    /** The curve used for the elliptic curve key (e.g., "P-256"). */
    private String crv;

    /** The x-coordinate for the EC public key. */
    private String x;

    /** The y-coordinate for the EC public key. */
    private String y;

    /** The algorithm used, typically "ES256" for ECDSA with P-256. */
    private String alg;
}
