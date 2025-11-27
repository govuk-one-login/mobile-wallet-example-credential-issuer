package uk.gov.di.mobile.wallet.cri.credential.mobile_driving_licence.cose;

import java.util.Arrays;
import java.util.Objects;

public record COSESign1(
        byte[] protectedHeader,
        COSEUnprotectedHeader unprotectedHeader,
        byte[] payload,
        byte[] signature) {

    @Override
    public boolean equals(Object obj) {
        if (this == obj) return true;
        if (!(obj instanceof COSESign1 other)) return false;

        return Arrays.equals(protectedHeader, other.protectedHeader)
                && Objects.equals(unprotectedHeader, other.unprotectedHeader)
                && Arrays.equals(payload, other.payload)
                && Arrays.equals(signature, other.signature);
    }

    @Override
    public int hashCode() {
        return Objects.hash(
                Arrays.hashCode(protectedHeader),
                unprotectedHeader,
                Arrays.hashCode(payload),
                Arrays.hashCode(signature));
    }

    @Override
    public String toString() {
        return "COSESign1["
                + "protectedHeader="
                + Arrays.toString(protectedHeader)
                + ", unprotectedHeader="
                + unprotectedHeader
                + ", payload="
                + Arrays.toString(payload)
                + ", signature="
                + Arrays.toString(signature)
                + ']';
    }
}
