package uk.gov.di.mobile.wallet.cri.credential.mdoc;

import uk.gov.di.mobile.wallet.cri.credential.StatusListClient;
import uk.gov.di.mobile.wallet.cri.credential.mdoc.constants.DocumentTypes;
import uk.gov.di.mobile.wallet.cri.credential.mdoc.cose.COSEKey;
import uk.gov.di.mobile.wallet.cri.credential.mdoc.cose.COSEKeyFactory;

import java.security.interfaces.ECPublicKey;
import java.util.Set;

/** Creates {@link MobileSecurityObject} instances for mobile driver's licenses. */
public class MobileSecurityObjectFactory {

    /** Version for the {@link MobileSecurityObject}. */
    private static final String MSO_VERSION = "1.0";

    /**
     * Document type identifier for a mobile driver's license (mDL), as specified by ISO 18013-5.
     */
    private static final String DOC_TYPE = DocumentTypes.MDL;

    /** Factory for creating {@link ValueDigests} instances. */
    private final ValueDigestsFactory valueDigestsFactory;

    /** Factory for creating {@link ValidityInfo} instances. */
    private final ValidityInfoFactory validityInfoFactory;

    /** Factory for creating {@link COSEKey} instances. */
    private final COSEKeyFactory coseKeyFactory;

    /**
     * Constructs a new {@link MobileSecurityObjectFactory}.
     *
     * @param valueDigestsFactory Factory for creating value digests
     * @param validityInfoFactory Factory for creating validity information
     * @param coseKeyFactory Factory for creating COSE keys
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
     * Builds a new {@link MobileSecurityObject} instance using the provided parameters.
     *
     * <p>
     *
     * @param nameSpaces The namespaces from which to generate value digests.
     * @param publicKey The device's elliptic curve public key that will be encoded into the COSE
     *     key.
     * @param statusListInformation Status list data containing index and URI information for
     *     revocation or status checking.
     * @param credentialTtlMinutes The credential time-to-live, in minutes, used to determine its
     *     validity period.
     * @return {@link MobileSecurityObject}
     * @throws MdocException If an error occurs when building the {@link ValueDigests}
     */
    public MobileSecurityObject build(
            Namespaces nameSpaces,
            ECPublicKey publicKey,
            StatusListClient.StatusListInformation statusListInformation,
            long credentialTtlMinutes)
            throws MdocException {
        COSEKey coseKey = coseKeyFactory.fromECPublicKey(publicKey);
        Set<String> authorizedNameSpaces = nameSpaces.namespaces().keySet();
        KeyAuthorizations keyAuthorizations = new KeyAuthorizations(authorizedNameSpaces);
        DeviceKeyInfo deviceKeyInfo = new DeviceKeyInfo(coseKey, keyAuthorizations);
        ValueDigests valueDigests = valueDigestsFactory.createFromNamespaces(nameSpaces);
        ValidityInfo validityInfo = validityInfoFactory.build(credentialTtlMinutes);
        StatusList statusList =
                new StatusList(statusListInformation.idx(), statusListInformation.uri());
        Status status = new Status(statusList);

        return new MobileSecurityObject(
                MSO_VERSION,
                valueDigestsFactory.getDigestAlgorithm(),
                deviceKeyInfo,
                valueDigests,
                DOC_TYPE,
                validityInfo,
                status);
    }
}
