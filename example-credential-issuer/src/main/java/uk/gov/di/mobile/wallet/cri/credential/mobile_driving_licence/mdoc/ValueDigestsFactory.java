package uk.gov.di.mobile.wallet.cri.credential.mobile_driving_licence.mdoc;

import com.fasterxml.jackson.dataformat.cbor.databind.CBORMapper;
import lombok.SneakyThrows;
import uk.gov.di.mobile.wallet.cri.credential.mobile_driving_licence.MDLException;
import uk.gov.di.mobile.wallet.cri.credential.mobile_driving_licence.cbor.CBOREncoder;

import java.security.MessageDigest;
import java.util.HashMap;
import java.util.Map;
import java.util.stream.Collectors;

/**
 * Factory class responsible for generating message digests for {@link IssuerSignedItem} instances.
 *
 * <p>Uses a provided {@link CBORMapper} to serialize items and a {@link MessageDigest} to compute
 * digests.
 */
public class ValueDigestsFactory {
    private final CBOREncoder cborEncoder;
    private final MessageDigest messageDigest;

    /**
     * Constructs a new instance using the given {@link CBORMapper} for serialization and {@link
     * MessageDigest} for digest calculation.
     *
     * @param cborEncoder Used to serialize {@link IssuerSignedItem} instances.
     * @param messageDigest Used to calculate the digest.
     */
    public ValueDigestsFactory(CBOREncoder cborEncoder, MessageDigest messageDigest) {
        this.cborEncoder = cborEncoder;
        this.messageDigest = messageDigest;
    }

    /**
     * Returns the digest algorithm used for this instance's generation of message digests.
     *
     * @return The digest algorithm used for this instance's generation of message digests.
     */
    public String getDigestAlgorithm() {
        return messageDigest.getAlgorithm();
    }

    /**
     * Creates a {@link ValueDigests} instance by calculating digests for each {@link
     * IssuerSignedItem} in the provided namespaces.
     *
     * <p>For each namespace key, serializes each {@link IssuerSignedItem} and computes its digest,
     * building a map of digest IDs to digest byte arrays.
     *
     * @param namespaces A map from namespace strings to lists of {@link IssuerSignedItem}s.
     * @return A new {@link ValueDigests} instance containing the calculated digests.
     */
    @SneakyThrows
    public ValueDigests createFromNamespaces(Namespaces namespaces) throws MDLException {
        // Map to hold the final result: namespace -> (digestId -> digest bytes)
        final Map<String, Map<Integer, byte[]>> namespaceToValueDigests = new HashMap<>();

        for (var entry : namespaces.namespaces().entrySet()) {
            // For each namespace, process its list of IssuerSignedItems:
            // 1. Serialize and digest each item
            // 2. Collect results into a map from digestId to digest bytes
            Map<Integer, byte[]> digestIdToDigest =
                    entry.getValue().stream()
                            .map(this::serializeAndComputeDigest)
                            .collect(Collectors.toMap(Map.Entry::getKey, Map.Entry::getValue));

            namespaceToValueDigests.put(entry.getKey(), digestIdToDigest);
        }

        // Return a new ValueDigests record containing all computed digests
        return new ValueDigests(namespaceToValueDigests);
    }

    /**
     * Serializes the given {@link IssuerSignedItem} and computes its digest.
     *
     * @param issuerSignedItem The item to digest.
     * @return A map entry where the key is the digest ID and the value is the digest byte array.
     * @throws MDLException If serialization or digest calculation fails.
     */
    private Map.Entry<Integer, byte[]> serializeAndComputeDigest(
            final IssuerSignedItem issuerSignedItem) throws MDLException {
        // Serialize the IssuerSignedItem to a CBOR byte array
        byte[] serializedIssuerSignedItem = cborEncoder.encode(issuerSignedItem);

        // Compute the digest over the serialized bytes
        byte[] digest = messageDigest.digest(serializedIssuerSignedItem);

        // Return a map entry pairing the digest ID with the computed digest bytes
        return Map.entry(issuerSignedItem.digestId(), digest);
    }
}
