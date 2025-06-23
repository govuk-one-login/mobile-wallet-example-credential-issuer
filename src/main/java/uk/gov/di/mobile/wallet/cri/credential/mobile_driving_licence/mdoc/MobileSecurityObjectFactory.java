package uk.gov.di.mobile.wallet.cri.credential.mobile_driving_licence.mdoc;

import uk.gov.di.mobile.wallet.cri.credential.mobile_driving_licence.DocType;
import uk.gov.di.mobile.wallet.cri.credential.mobile_driving_licence.cbor.MDLException;

import java.util.List;
import java.util.Map;

/** Factory class for creating MobileSecurityObject instances. */
public class MobileSecurityObjectFactory {

    /** The version for the {@link MobileSecurityObject}. */
    private static final String MSO_VERSION = "1.0";

    /**
     * The document type identifier for a mobile driver's license (mDL), as specified by ISO
     * 18013-5.
     */
    private static final String DOC_TYPE = DocType.MDL.getValue();

    /** The factory responsible for creating {@link ValueDigests} instances. */
    private final ValueDigestsFactory valueDigestsFactory;

    /**
     * Constructs a new {@link MobileSecurityObjectFactory} with the provided {@link
     * ValueDigestsFactory}.
     *
     * @param valueDigestsFactory The factory used to create value digests for the {@link
     *     MobileSecurityObject}.
     */
    public MobileSecurityObjectFactory(ValueDigestsFactory valueDigestsFactory) {
        this.valueDigestsFactory = valueDigestsFactory;
    }

    /**
     * Builds a {@link MobileSecurityObject} instance from the provided namespaces.
     *
     * <p>This method generates the value digests for each namespace and constructs the {@link
     * MobileSecurityObject}.
     *
     * @param nameSpaces A map where the key is the namespace string and the value is a list of
     *     {@link IssuerSignedItem} objects belonging to that namespace. This map provides the data
     *     used to generate the value digests for the {@link MobileSecurityObject}.
     * @return The constructed {@link MobileSecurityObject} instance.
     * @throws MDLException If an error occurs during the creation of the {@link ValueDigests}.
     */
    public MobileSecurityObject build(Map<String, List<IssuerSignedItem>> nameSpaces)
            throws MDLException {
        ValueDigests valueDigests = valueDigestsFactory.createFromNamespaces(nameSpaces);
        return new MobileSecurityObject(
                MSO_VERSION, valueDigestsFactory.getDigestAlgorithm(), valueDigests, DOC_TYPE);
    }
}
