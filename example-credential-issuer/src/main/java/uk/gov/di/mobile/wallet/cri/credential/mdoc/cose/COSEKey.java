package uk.gov.di.mobile.wallet.cri.credential.mdoc.cose;

import java.util.Arrays;
import java.util.Objects;

/**
 * Encodes a public key in the COSE_Key format, as per <a
 * href="https://www.rfc-editor.org/rfc/rfc8152.html#section-7">RFC 8152.
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
