package uk.gov.di.mobile.wallet.cri.credential.mobile_driving_licence;

import uk.gov.di.mobile.wallet.cri.credential.mobile_driving_licence.cbor.CBOREncoder;
import uk.gov.di.mobile.wallet.cri.credential.mobile_driving_licence.cbor.JacksonCBOREncoderProvider;
import uk.gov.di.mobile.wallet.cri.credential.mobile_driving_licence.cbor.MDLException;
import uk.gov.di.mobile.wallet.cri.credential.mobile_driving_licence.mdoc.*;

import javax.xml.stream.events.Namespace;
import java.util.HexFormat;

public class MobileDrivingLicenceService {

    private final CBOREncoder cborEncoder;
    private final DocumentFactory documentFactory;
    private NamespaceBuilder namespaceBuilder;

    public MobileDrivingLicenceService() {
        this.cborEncoder = new CBOREncoder(JacksonCBOREncoderProvider.configuredCBORMapper());
        IssuerSignedItemFactory issuerSignedItemFactory =
                new IssuerSignedItemFactory(new DigestIDGenerator());
        this.documentFactory = new DocumentFactory();
        this.namespaceBuilder = new NamespaceBuilder(issuerSignedItemFactory, cborEncoder);;
    }

    // Additional constructor required for unit testing purposes
    public MobileDrivingLicenceService(CBOREncoder cborEncoder, DocumentFactory documentFactory) {
        this.cborEncoder = cborEncoder;
        this.documentFactory = documentFactory;
    }

    public String createMobileDrivingLicence(DrivingLicenceDocument drivingLicenceDocument)
            throws MDLException {
        String namespace = namespaceBuilder.buildNamespace(drivingLicenceDocument);



        Document mdoc = documentFactory.build(drivingLicenceDocument);
        byte[] cborEncodedMobileDrivingLicence = cborEncoder.encode(mdoc);
        return HexFormat.of().formatHex(cborEncodedMobileDrivingLicence);
    }
}
