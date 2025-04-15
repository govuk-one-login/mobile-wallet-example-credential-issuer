package uk.gov.di.mobile.wallet.cri.credential.mobile_driving_licence.mdoc;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;
import com.google.common.base.CaseFormat;
import org.jetbrains.annotations.NotNull;
import uk.gov.di.mobile.wallet.cri.credential.mobile_driving_licence.DrivingLicenceDocument;
import uk.gov.di.mobile.wallet.cri.credential.mobile_driving_licence.cbor.CBOREncoder;
import uk.gov.di.mobile.wallet.cri.credential.mobile_driving_licence.cbor.CBOREncodingException;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

public class DocumentFactory {
    private static final String MOBILE_DRIVING_LICENCE_NAMESPACE = "org.iso.18013.5.1";
    private static final String MOBILE_DRIVING_LICENCE_DOCUMENT_TYPE =
            MOBILE_DRIVING_LICENCE_NAMESPACE + ".mDL";

    private final IssuerSignedItemFactory issuerSignedItemFactory;
    private final CBOREncoder cborEncoder;

    public DocumentFactory(
            IssuerSignedItemFactory issuerSignedItemFactory, CBOREncoder cborEncoder) {
        this.issuerSignedItemFactory = issuerSignedItemFactory;
        this.cborEncoder = cborEncoder;
    }

    public Document build(final DrivingLicenceDocument drivingLicence)
            throws CBOREncodingException {
        Map<String, List<byte[]>> nameSpaces = buildNameSpaces(drivingLicence);
        IssuerSigned issuerSigned = buildIssuerSigned(nameSpaces);
        return new Document(MOBILE_DRIVING_LICENCE_DOCUMENT_TYPE, issuerSigned);
    }

    private Map<String, List<byte[]>> buildNameSpaces(final DrivingLicenceDocument drivingLicence)
            throws CBOREncodingException {
        ObjectMapper objectMapper = new ObjectMapper();
        objectMapper.registerModule(new JavaTimeModule());
        Map<String, Object> drivingLicenceMap =
                objectMapper.convertValue(drivingLicence, Map.class);

        List<byte[]> issuerSignedItems = new ArrayList<>();
        for (Map.Entry<String, Object> entry : drivingLicenceMap.entrySet()) {
            String fieldName = entry.getKey();
            Object fieldValue = entry.getValue();
            String asSnakeCase = getAsSnakeCase(fieldName);
            IssuerSignedItem issuerSignedItem =
                    issuerSignedItemFactory.build(asSnakeCase, fieldValue);
            byte[] issuerSignedItemBytes = cborEncoder.encode(issuerSignedItem);
            issuerSignedItems.add(issuerSignedItemBytes);
        }
        return Map.of(MOBILE_DRIVING_LICENCE_NAMESPACE, issuerSignedItems);
    }

    private static @NotNull String getAsSnakeCase(String fieldName) {
        return CaseFormat.LOWER_CAMEL.to(CaseFormat.LOWER_UNDERSCORE, fieldName);
    }

    private IssuerSigned buildIssuerSigned(final Map<String, List<byte[]>> nameSpaces) {
        return new IssuerSigned(nameSpaces, new IssuerAuth());
    }
}
