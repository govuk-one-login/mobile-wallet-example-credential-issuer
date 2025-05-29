package uk.gov.di.mobile.wallet.cri.credential.mobile_driving_licence.mdoc;

import com.fasterxml.jackson.dataformat.cbor.databind.CBORMapper;
import lombok.SneakyThrows;
import uk.gov.di.mobile.wallet.cri.credential.mobile_driving_licence.cbor.MDLException;

import java.io.IOException;
import java.security.MessageDigest;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

/**
 * Factory class responsible for generating message digests for {@link IssuerSignedItem} instances.
 *
 * <p>Uses a provided {@link CBORMapper} to serialize items and a {@link MessageDigest} to compute
 * digests.
 */
public class ValueDigestsFactory {
    private final CBORMapper cborMapper;
    private final MessageDigest messageDigest;

    /**
     * Constructs a new instance using the given {@link CBORMapper} for serialization and {@link
     * MessageDigest} for digest calculation.
     *
     * @param cborMapper Used to serialize {@link IssuerSignedItem} instances.
     * @param messageDigest Used to calculate the digest.
     */
    public ValueDigestsFactory(CBORMapper cborMapper, MessageDigest messageDigest) {
        this.cborMapper = cborMapper;
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
    public ValueDigests createFromNameSpaces(Map<String, List<IssuerSignedItem>> namespaces) {
        final Map<String, Map<Integer, byte[]>> namespaceToValueDigests = new HashMap<>();

        for (var entry : namespaces.entrySet()) {
            Map<Integer, byte[]> digestIdToDigest =
                    entry.getValue().stream()
                            .map(this::buildDigestIdToDigest)
                            .collect(Collectors.toMap(Map.Entry::getKey, Map.Entry::getValue));

            namespaceToValueDigests.put(entry.getKey(), digestIdToDigest);
        }

        return new ValueDigests(namespaceToValueDigests);
    }

    /**
     * Serializes the given {@link IssuerSignedItem} and computes its digest.
     *
     * @param issuerSignedItem The item to digest.
     * @return A map entry where the key is the digest ID and the value is the digest byte array.
     * @throws MDLException If serialization or digest calculation fails.
     */
    private Map.Entry<Integer, byte[]> buildDigestIdToDigest(IssuerSignedItem issuerSignedItem)
            throws MDLException {
        try {
            byte[] serializedIssuerSignedItem = cborMapper.writeValueAsBytes(issuerSignedItem);
            byte[] digest = messageDigest.digest(serializedIssuerSignedItem);

            return Map.entry(issuerSignedItem.digestId(), digest);
        } catch (IOException e) {
            throw new MDLException("Error when calculating digest over IssuerSignedItem", e);
        }
    }
}
