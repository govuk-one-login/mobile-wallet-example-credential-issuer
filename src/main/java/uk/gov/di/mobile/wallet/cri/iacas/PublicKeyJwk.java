package uk.gov.di.mobile.wallet.cri.iacas;

/**
 * Represents the IACA's Elliptic Curve Public Key in JWK (JSON Web Key) format.
 *
 * @param kty The key type (e.g., "EC" for Elliptic Curve).
 * @param crv The curve used for the elliptic curve key (e.g., "P-256").
 * @param x The x-coordinate for the EC public key.
 * @param y The y-coordinate for the EC public key.
 * @param alg The signing algorithm intended for use with the key,
 */
public record PublicKeyJwk(String kty, String crv, String x, String y, String alg) {}
