package uk.gov.di.mobile.wallet.cri.credential.mobile_driving_licence;

import uk.gov.di.mobile.wallet.cri.credential.mobile_driving_licence.cbor.CBOREncoder;
import uk.gov.di.mobile.wallet.cri.credential.mobile_driving_licence.cbor.JacksonCBOREncoderProvider;
import uk.gov.di.mobile.wallet.cri.credential.mobile_driving_licence.cbor.MDLException;
import uk.gov.di.mobile.wallet.cri.credential.mobile_driving_licence.mdoc.*;

import java.util.HexFormat;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

public class MobileDrivingLicenceService {

    private static final String MOBILE_DRIVING_LICENCE_NAMESPACE = "org.iso.18013.5.1";

    private final CBOREncoder cborEncoder;
    private final DocumentFactory documentFactory;
    private NamespaceBuilder namespaceBuilder;

    public MobileDrivingLicenceService() {
        this.cborEncoder = new CBOREncoder(JacksonCBOREncoderProvider.configuredCBORMapper());
        IssuerSignedItemFactory issuerSignedItemFactory =
                new IssuerSignedItemFactory(new DigestIDGenerator());
        this.documentFactory = new DocumentFactory();
        this.namespaceBuilder = new NamespaceBuilder(issuerSignedItemFactory, cborEncoder);
        ;
    }

    // Additional constructor required for unit testing purposes
    public MobileDrivingLicenceService(CBOREncoder cborEncoder, DocumentFactory documentFactory) {
        this.cborEncoder = cborEncoder;
        this.documentFactory = documentFactory;
    }

    public String createMobileDrivingLicence(DrivingLicenceDocument drivingLicenceDocument)
            throws MDLException {
        Map<String, List<byte[]>> nameSpaces = new LinkedHashMap<>();
        List<byte[]> namespace = namespaceBuilder.buildNamespace(drivingLicenceDocument);
        nameSpaces.put(MOBILE_DRIVING_LICENCE_NAMESPACE, namespace);

        Document mdoc = documentFactory.build(nameSpaces);
        byte[] cborEncodedMobileDrivingLicence = cborEncoder.encode(mdoc);
        return HexFormat.of().formatHex(cborEncodedMobileDrivingLicence);
    }
}
