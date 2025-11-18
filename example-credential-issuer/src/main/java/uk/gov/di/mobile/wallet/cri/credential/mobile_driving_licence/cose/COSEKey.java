package uk.gov.di.mobile.wallet.cri.credential.mobile_driving_licence.cose;

/**
 * Represents a COSE_Key structure as defined in RFC 8152, used for encoding public keys in Mobile
 * Driving License (mDL).
 *
 * <p>The {@code @JsonValue} annotation is used here to ensure that when this record is serialized,
 * the map contents are emitted at the top level, rather than being wrapped inside a "parameters"
 * property.
 *
 * <p>See: <a href="https://www.rfc-editor.org/rfc/rfc8152.html#section-7">RFC 8152, Section 7</a>
 */
public record COSEKey(int keyType, int curve, byte[] x, byte[] y) {
    /** Compact constructor: defensively copy mutable array components to preserve immutability. */
    public COSEKey {
        x = x == null ? null : x.clone();
        y = y == null ? null : y.clone();
    }

    /** Return a defensive copy of X coordinate bytes. */
    @Override
    public byte[] x() {
        return x == null ? null : x.clone();
    }

    /** Return a defensive copy of Y coordinate bytes. */
    @Override
    public byte[] y() {
        return y == null ? null : y.clone();
    }
}
