package uk.gov.di.mobile.wallet.cri.credential.mobile_driving_licence.mdoc;

import java.util.HashMap;
import java.util.Map;

/**
 * Record representing a mapping from namespaces to their digest ID and digest value mappings.
 *
 * <p>Each namespace (a String key) maps to another map where the keys are digest IDs (Integer),
 * and the values are the corresponding digest calculations as serialized byte arrays.</p>
 *
 * <p>For example, the namespace {@code "org.iso.18013.5.1"} could map to a child map like:
 * {@code 0 -> byte[], 5 -> byte[]}, representing two digest IDs and their serialized digest values.</p>
 */
public record ValueDigests(Map<String, Map<Integer, byte[]>> namespaceToValueDigests) {

  /**
   * Returns a shallow copy of the mapping from namespaces to their digest ID and digest value maps.
   *
   * @return A shallow copy of the namespace to digest mappings.
   */
  @Override
  public Map<String, Map<Integer, byte[]>> namespaceToValueDigests() {
    return new HashMap<>(namespaceToValueDigests);
  }
}