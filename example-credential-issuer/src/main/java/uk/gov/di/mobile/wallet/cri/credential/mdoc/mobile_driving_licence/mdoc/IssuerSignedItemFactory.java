package uk.gov.di.mobile.wallet.cri.credential.mdoc.mobile_driving_licence.mdoc;

import java.security.SecureRandom;

/**
 * Factory class responsible for creating {@link IssuerSignedItem} instances.
 *
 * <p>This factory generates IssuerSignedItems with a digest ID and random bytes. It uses a {@link
 * DigestIDGenerator} to produce the digest IDs.
 */
public class IssuerSignedItemFactory {
    private static final int RANDOM_BYTES_LENGTH = 16;
    private final DigestIDGenerator digestIDGenerator;
    private static final SecureRandom secureRandom = new SecureRandom();

    /**
     * Constructs the factory with the given DigestIDGenerator.
     *
     * @param digestIDGenerator The generator used to produce digest IDs.
     */
    public IssuerSignedItemFactory(DigestIDGenerator digestIDGenerator) {
        this.digestIDGenerator = digestIDGenerator;
    }

    /**
     * Builds an {@link IssuerSignedItem} with the provided element identifier and value.
     *
     * @param elementIdentifier The identifier or name of the data element.
     * @param elementValue The value of the data element.
     * @return A new {@link IssuerSignedItem} instance.
     */
    public IssuerSignedItem build(final String elementIdentifier, final Object elementValue) {
        byte[] randomBytes = generateRandomBytes();
        int digestID = digestIDGenerator.next();

        return new IssuerSignedItem(digestID, randomBytes, elementIdentifier, elementValue);
    }

    /**
     * Generates an array of random bytes.
     *
     * @return A byte array filled with cryptographically secure random bytes.
     */
    private static byte[] generateRandomBytes() {
        byte[] bytes = new byte[IssuerSignedItemFactory.RANDOM_BYTES_LENGTH];
        secureRandom.nextBytes(bytes);
        return bytes;
    }
}
