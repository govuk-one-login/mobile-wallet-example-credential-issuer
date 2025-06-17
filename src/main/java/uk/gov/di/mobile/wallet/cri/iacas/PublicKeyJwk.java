package uk.gov.di.mobile.wallet.cri.iacas;

import lombok.Getter;
import lombok.Setter;

/** Represents the IACA's Elliptic Curve Public Key in JWK (JSON Web Key) format. */
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

    public PublicKeyJwk(String kty, String crv, String x, String y) {
        this.kty = kty;
        this.crv = crv;
        this.x = x;
        this.y = y;
    }
}
