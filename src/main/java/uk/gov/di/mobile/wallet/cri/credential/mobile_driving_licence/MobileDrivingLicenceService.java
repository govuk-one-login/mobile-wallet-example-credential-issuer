package uk.gov.di.mobile.wallet.cri.credential.mobile_driving_licence;

import uk.gov.di.mobile.wallet.cri.credential.mobile_driving_licence.cbor.CBOREncoder;
import uk.gov.di.mobile.wallet.cri.credential.mobile_driving_licence.cbor.JacksonCBOREncoderProvider;
import uk.gov.di.mobile.wallet.cri.credential.mobile_driving_licence.cbor.MDLException;
import uk.gov.di.mobile.wallet.cri.credential.mobile_driving_licence.mdoc.DigestIDGenerator;
import uk.gov.di.mobile.wallet.cri.credential.mobile_driving_licence.mdoc.DocumentFactory;
import uk.gov.di.mobile.wallet.cri.credential.mobile_driving_licence.mdoc.IssuerSignedItemFactory;

import java.util.HexFormat;

public class MobileDrivingLicenceService {

    private final CBOREncoder cborEncoder;
    private final DocumentFactory documentFactory;

    public MobileDrivingLicenceService() {
        this.cborEncoder = new CBOREncoder(JacksonCBOREncoderProvider.configuredCBORMapper());
        IssuerSignedItemFactory issuerSignedItemFactory =
                new IssuerSignedItemFactory(new DigestIDGenerator());
        this.documentFactory = new DocumentFactory(issuerSignedItemFactory, cborEncoder);
    }

    // Additional constructor required for unit testing purposes
    public MobileDrivingLicenceService(CBOREncoder cborEncoder, DocumentFactory documentFactory) {
        this.cborEncoder = cborEncoder;
        this.documentFactory = documentFactory;
    }

    public String createMobileDrivingLicence(DrivingLicenceDocument drivingLicenceDocument)
            throws MDLException {
        byte[] cborEncodedData = cborEncoder.encode(documentFactory.build(drivingLicenceDocument));
        return HexFormat.of().formatHex(cborEncodedData);
    }
}
