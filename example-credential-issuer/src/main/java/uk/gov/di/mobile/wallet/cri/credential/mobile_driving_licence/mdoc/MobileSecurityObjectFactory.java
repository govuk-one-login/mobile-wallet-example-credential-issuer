package uk.gov.di.mobile.wallet.cri.credential.mobile_driving_licence.mdoc;

import uk.gov.di.mobile.wallet.cri.credential.mobile_driving_licence.MDLException;
import uk.gov.di.mobile.wallet.cri.credential.mobile_driving_licence.cose.COSEKey;
import uk.gov.di.mobile.wallet.cri.credential.mobile_driving_licence.cose.COSEKeyFactory;
import uk.gov.di.mobile.wallet.cri.credential.mobile_driving_licence.mdoc.constants.DocumentTypes;

import java.security.interfaces.ECPublicKey;
import java.util.Set;

/**
 * Factory for creating {@link MobileSecurityObject} instances for mobile driver's licenses.
 *
 * <p>This factory handles the creation of value digests, validity information, and device key
 * information required for the MSO.
 */
public class MobileSecurityObjectFactory {

    /** The version for the {@link MobileSecurityObject}. */
    private static final String MSO_VERSION = "1.0";

    /**
     * The document type identifier for a mobile driver's license (mDL), as specified by ISO
     * 18013-5.
     */
    private static final String DOC_TYPE = DocumentTypes.MDL;

    /** The factory responsible for creating {@link ValueDigests} instances. */
    private final ValueDigestsFactory valueDigestsFactory;

    /** The factory responsible for creating {@link ValidityInfo} instances. */
    private final ValidityInfoFactory validityInfoFactory;

    /** The factory responsible for creating {@link COSEKeyFactory} instances. */
    private final COSEKeyFactory coseKeyFactory;

    /**
     * Constructs a new {@link MobileSecurityObjectFactory} with the provided factories.
     *
     * @param valueDigestsFactory The factory used to create value digests for the {@link
     *     MobileSecurityObject}.
     * @param validityInfoFactory The factory used to create validity information for the {@link
     *     MobileSecurityObject}.
     * @param coseKeyFactory The factory used to create the COSE key for the {@link
     *     MobileSecurityObject}.
     */
    public MobileSecurityObjectFactory(
            ValueDigestsFactory valueDigestsFactory,
            ValidityInfoFactory validityInfoFactory,
            COSEKeyFactory coseKeyFactory) {
        this.valueDigestsFactory = valueDigestsFactory;
        this.validityInfoFactory = validityInfoFactory;
        this.coseKeyFactory = coseKeyFactory;
    }

    /**
     * Builds a {@link MobileSecurityObject} instance from the provided namespaces and public key.
     *
     * <p>This method creates a mobile security object by:
     *
     * <ul>
     *   <li>Generating value digests for the provided namespaces
     *   <li>Creating validity information with a one-year validity period
     *   <li>Converting the EC public key to COSE key format
     *   <li>Setting up key authorizations for all provided namespaces
     * </ul>
     *
     * <p>The validity period is determined by the configured {@link ValidityInfoFactory}. The
     * created MSO will authorize access to all namespaces provided in the input.
     *
     * @param nameSpaces A map where the key is the namespace string and the value is a list of
     *     {@link IssuerSignedItem} objects belonging to that namespace. This map provides the data
     *     used to generate the value digests for the {@link MobileSecurityObject}.
     * @param publicKey The EC public key for device authentication. Must use the P-256 curve.
     * @return The constructed {@link MobileSecurityObject} instance.
     * @throws MDLException If an error occurs during the creation of the {@link ValueDigests}.
     * @throws IllegalArgumentException If the public key does not use the P-256 curve.
     */
    public MobileSecurityObject build(Namespaces nameSpaces, ECPublicKey publicKey)
            throws MDLException {
        ValueDigests valueDigests = valueDigestsFactory.createFromNamespaces(nameSpaces);
        ValidityInfo validityInfo = validityInfoFactory.build();
        COSEKey coseKey = coseKeyFactory.fromECPublicKey(publicKey);

        Set<String> authorizedNameSpaces = nameSpaces.asMap().keySet();
        KeyAuthorizations keyAuthorizations = new KeyAuthorizations(authorizedNameSpaces);

        return new MobileSecurityObject(
                MSO_VERSION,
                valueDigestsFactory.getDigestAlgorithm(),
                new DeviceKeyInfo(coseKey, keyAuthorizations),
                valueDigests,
                DOC_TYPE,
                validityInfo);
    }
}
