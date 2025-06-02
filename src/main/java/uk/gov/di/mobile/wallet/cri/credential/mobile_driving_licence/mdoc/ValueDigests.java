package uk.gov.di.mobile.wallet.cri.credential.mobile_driving_licence.mdoc;

import com.fasterxml.jackson.annotation.JsonValue;

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
public record ValueDigests(Map<String, Map<Integer, byte[]>> namespaceToValueDigests) {

    @JsonValue
    public Map<String, Map<Integer, byte[]>> value() {
      return namespaceToValueDigests;
    }
}
