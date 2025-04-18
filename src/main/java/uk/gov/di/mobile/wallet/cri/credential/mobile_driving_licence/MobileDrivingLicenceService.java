package uk.gov.di.mobile.wallet.cri.credential.mobile_driving_licence;

import uk.gov.di.mobile.wallet.cri.credential.mobile_driving_licence.cbor.CBOREncoder;
import uk.gov.di.mobile.wallet.cri.credential.mobile_driving_licence.cbor.MDLException;
import uk.gov.di.mobile.wallet.cri.credential.mobile_driving_licence.mdoc.Document;
import uk.gov.di.mobile.wallet.cri.credential.mobile_driving_licence.mdoc.DocumentFactory;
import uk.gov.di.mobile.wallet.cri.credential.mobile_driving_licence.mdoc.NameSpaceFactory;

import java.util.HexFormat;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

/**
 * Service responsible for creating Mobile Driving Licence (mDL) documents following the ISO 18013-5
 * standard.
 *
 * <p>This service encodes driving licence information into CBOR format and organizes it into the
 * proper namespace structure required by the standard.
 */
public class MobileDrivingLicenceService {

    /** The standard namespace for Mobile Driving Licence documents as defined by ISO 18013-5. */
    private static final String MOBILE_DRIVING_LICENCE_NAMESPACE = "org.iso.18013.5.1";

    /** Encoder used to convert document objects into CBOR byte representation. */
    private final CBOREncoder cborEncoder;

    /** Factory for creating document objects that will be encoded. */
    private final DocumentFactory documentFactory;

    /** Factory for creating a namespace from driving licence documents. */
    private final NameSpaceFactory namespaceFactory;

    /**
     * Constructs a new MobileDrivingLicenceService with all required dependencies.
     *
     * @param cborEncoder The CBOR encoder to use for data serialization
     * @param documentFactory The document factory to use for document creation
     * @param namespaceFactory The namespace factory to use for namespace creation - this is an
     *     array of IssuerSignedItem objects
     */
    public MobileDrivingLicenceService(
            CBOREncoder cborEncoder,
            DocumentFactory documentFactory,
            NameSpaceFactory namespaceFactory) {
        this.cborEncoder = cborEncoder;
        this.documentFactory = documentFactory;
        this.namespaceFactory = namespaceFactory;
    }

    /**
     * Creates a Mobile Driving Licence document in CBOR format from the provided driving licence
     * document and returns it as a hexadecimal string.
     *
     * @param drivingLicenceDocument The driving licence document containing user information
     * @return A hexadecimal string representation of the CBOR-encoded mobile driving licence
     * @throws MDLException If an error occurs during document creation or encoding
     */
    public String createMobileDrivingLicence(DrivingLicenceDocument drivingLicenceDocument)
            throws MDLException {
        Map<String, List<byte[]>> nameSpaces = new LinkedHashMap<>();
        List<byte[]> namespace = namespaceFactory.build(drivingLicenceDocument);
        nameSpaces.put(MOBILE_DRIVING_LICENCE_NAMESPACE, namespace);

        Document mdoc = documentFactory.build(nameSpaces);
        byte[] cborEncodedMobileDrivingLicence = cborEncoder.encode(mdoc);
        return HexFormat.of().formatHex(cborEncodedMobileDrivingLicence);
    }
}
