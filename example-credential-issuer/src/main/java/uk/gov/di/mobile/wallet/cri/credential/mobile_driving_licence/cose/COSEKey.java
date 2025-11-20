package uk.gov.di.mobile.wallet.cri.credential.mobile_driving_licence.cose;

import java.util.Arrays;
import java.util.Objects;

/**
 * Represents a COSE_Key structure as defined in RFC 8152, used for encoding public keys in Mobile
 * Driving License (mDL).
 *
 * <p>See: <a href="https://www.rfc-editor.org/rfc/rfc8152.html#section-7">RFC 8152, Section 7</a>
 */
public record COSEKey(int keyType, int curve, byte[] x, byte[] y) {
    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof COSEKey that)) return false;
        return keyType == that.keyType
                && curve == that.curve
                && Arrays.equals(x, that.x)
                && Arrays.equals(y, that.y);
    }

    @Override
    public int hashCode() {
        int result = Objects.hash(keyType, curve);
        result = 31 * result + Arrays.hashCode(x);
        result = 31 * result + Arrays.hashCode(y);
        return result;
    }

    @Override
    public String toString() {
        return "COSEKey{"
                + "keyType="
                + keyType
                + ", curve="
                + curve
                + ", x="
                + Arrays.toString(x)
                + ", y="
                + Arrays.toString(y)
                + '}';
    }
}
