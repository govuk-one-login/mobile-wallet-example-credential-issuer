package uk.gov.di.mobile.wallet.cri.credential.mobile_driving_licence.mdoc;

import com.fasterxml.jackson.annotation.JsonProperty;
import org.jetbrains.annotations.NotNull;

import java.util.Arrays;
import java.util.Objects;

public record IssuerSignedItem(
        @JsonProperty("digestID") Integer digestId,
        byte[] random,
        String elementIdentifier,
        Object elementValue) {

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof IssuerSignedItem that)) return false;
        return Objects.equals(digestId, that.digestId)
                && Arrays.equals(random, that.random)
                && Objects.equals(elementIdentifier, that.elementIdentifier)
                && Objects.equals(elementValue, that.elementValue);
    }

    @Override
    public int hashCode() {
        int result = Objects.hash(digestId, elementIdentifier, elementValue);
        result = 31 * result + Arrays.hashCode(random);
        return result;
    }

    @Override
    public @NotNull String toString() {
        return "IssuerSignedItem{"
                + "digestId="
                + digestId
                + ", random="
                + Arrays.toString(random)
                + ", elementIdentifier='"
                + elementIdentifier
                + '\''
                + ", elementValue="
                + elementValue
                + '}';
    }
}
