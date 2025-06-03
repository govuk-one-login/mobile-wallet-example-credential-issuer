package uk.gov.di.mobile.wallet.cri.credential.mobile_driving_licence.mdoc;

import java.util.Arrays;

public record IssuerAuth(byte[] mobileSecurityObjectBytes) {
    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof IssuerAuth other)) return false;
        return Arrays.equals(mobileSecurityObjectBytes, other.mobileSecurityObjectBytes);
    }

    @Override
    public int hashCode() {
        return Arrays.hashCode(mobileSecurityObjectBytes);
    }

    @Override
    public String toString() {
        return "IssuerAuth["
                + "mobileSecurityObjectBytes="
                + Arrays.toString(mobileSecurityObjectBytes)
                + ']';
    }
}
