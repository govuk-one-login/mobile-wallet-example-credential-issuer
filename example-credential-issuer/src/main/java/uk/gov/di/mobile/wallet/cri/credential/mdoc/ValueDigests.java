package uk.gov.di.mobile.wallet.cri.credential.mdoc;

import com.fasterxml.jackson.annotation.JsonValue;

import java.util.HashMap;
import java.util.Map;

/**
 * Record representing a mapping from namespaces to their digest ID and digest value mappings.
 *
 * <p>Each namespace (a String key) maps to another map where the keys are digest IDs (Integer), and
 * the values are the corresponding digest calculations as serialized byte arrays.
 *
 * <p>For example, the namespace {@code "org.iso.18013.5.1"} could map to a child map like: {@code 0
 * -> byte[], 5 -> byte[]}, representing two digest IDs and their serialized digest values.
 */
public record ValueDigests(Map<String, Map<Integer, byte[]>> valueDigests) {

    /**
     * Returns a shallow copy of the mapping from namespaces to their digest ID and digest value
     * maps.
     *
     * <p>The {@link JsonValue} annotation ensures that when this object is serialized with Jackson,
     * the returned map is used directly, omitting the record's field name from the output.
     *
     * @return A shallow copy of the namespace to digest mappings.
     */
    @JsonValue
    public Map<String, Map<Integer, byte[]>> valueDigests() {
        return new HashMap<>(valueDigests);
    }
}
