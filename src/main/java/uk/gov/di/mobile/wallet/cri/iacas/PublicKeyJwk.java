package uk.gov.di.mobile.wallet.cri.iacas;

import com.nimbusds.jose.Algorithm;
import com.nimbusds.jose.jwk.Curve;
import com.nimbusds.jose.jwk.KeyType;
import com.nimbusds.jose.util.Base64URL;
import lombok.Getter;
import lombok.Setter;

/** Represents the IACA's Elliptic Curve Public Key in JWK (JSON Web Key) format. */
@Getter
@Setter
public class PublicKeyJwk {
    /** The key type (e.g., "EC" for Elliptic Curve). */
    private KeyType kty;

    /** The curve used for the elliptic curve key (e.g., "P-256"). */
    private Curve crv;

    /** The x-coordinate for the EC public key. */
    private Base64URL x;

    /** The y-coordinate for the EC public key. */
    private Base64URL y;

    /** The algorithm used, typically "ES256" for ECDSA with P-256. */
    private Algorithm alg;

    public PublicKeyJwk(KeyType kty, Curve crv, Base64URL x, Base64URL y, Algorithm alg) {
        this.kty = kty;
        this.crv = crv;
        this.x = x;
        this.y = y;
        this.alg = alg;
    }
}
