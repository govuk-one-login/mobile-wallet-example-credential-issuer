package uk.gov.di.mobile.wallet.cri.credential.mobile_driving_licence;

import uk.gov.di.mobile.wallet.cri.credential.mobile_driving_licence.cbor.CBOREncoder;
import uk.gov.di.mobile.wallet.cri.credential.mobile_driving_licence.mdoc.Document;
import uk.gov.di.mobile.wallet.cri.credential.mobile_driving_licence.mdoc.DocumentFactory;

import java.util.HexFormat;

/**
 * Service responsible for creating Mobile Driving Licence (mDL) documents following the ISO 18013-5
 * standard.
 *
 * <p>This service encodes driving licence information into CBOR format and organizes it into the
 * proper namespace structure required by the standard.
 */
public class MobileDrivingLicenceService {
    /** Encoder used to convert document objects into CBOR byte representation. */
    private final CBOREncoder cborEncoder;

    private final DocumentFactory documentFactory;

    /**
     * Constructs a new MobileDrivingLicenceService with all required dependencies.
     *
     * @param cborEncoder The CBOR encoder to use for data serialization
     * @param documentFactory The document factory to use for document creation array of
     *     IssuerSignedItem objects
     */
    public MobileDrivingLicenceService(CBOREncoder cborEncoder, DocumentFactory documentFactory) {
        this.cborEncoder = cborEncoder;
        this.documentFactory = documentFactory;
    }

    /**
     * Creates a Mobile Driving Licence document in CBOR format from the provided driving licence
     * document and returns it as a hexadecimal string.
     *
     * @param drivingLicenceDocument The driving licence document containing user information
     * @return A hexadecimal string representation of the CBOR-encoded mobile driving licence
     * @throws DrivingPrivilege.MDLException If an error occurs during document creation or encoding
     */
    public String createMobileDrivingLicence(DrivingLicenceDocument drivingLicenceDocument)
            throws Exception {
        Document mdoc = documentFactory.build(drivingLicenceDocument);
        byte[] cborEncodedMobileDrivingLicence = cborEncoder.encode(mdoc);
        return HexFormat.of().formatHex(cborEncodedMobileDrivingLicence);
    }
}
