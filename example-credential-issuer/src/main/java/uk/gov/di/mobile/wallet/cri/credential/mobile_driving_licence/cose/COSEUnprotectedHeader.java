package uk.gov.di.mobile.wallet.cri.credential.mobile_driving_licence.cose;

import org.jetbrains.annotations.NotNull;

import java.util.Arrays;

public record COSEUnprotectedHeader(byte[] x5chain) {
    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof COSEUnprotectedHeader that)) return false;
        return Arrays.equals(x5chain, that.x5chain);
    }

    @Override
    public int hashCode() {
        return 31 + Arrays.hashCode(x5chain);
    }

    @Override
    public @NotNull String toString() {
        return "COSEUnprotectedHeader{" + "x5chain=" + Arrays.toString(x5chain) + '}';
    }
}
