package uk.gov.di.mobile.wallet.cri.credential.mobile_driving_licence.mdoc;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;
import uk.gov.di.mobile.wallet.cri.credential.mobile_driving_licence.DrivingLicenceDocument;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

public class DocumentFactory {
    private static final String MOBILE_DRIVING_LICENCE_NAMESPACE = "org.iso.18013.5.1";
    private static final String MOBILE_DRIVING_LICENCE_DOCUMENT_TYPE =
            MOBILE_DRIVING_LICENCE_NAMESPACE + "mDL";

    private final IssuerSignedItemFactory issuerSignedItemFactory;

    public DocumentFactory(IssuerSignedItemFactory issuerSignedItemFactory) {
        this.issuerSignedItemFactory = issuerSignedItemFactory;
    }

    public Document build(final DrivingLicenceDocument drivingLicence)
            throws IllegalAccessException {
        Map<String, List<IssuerSignedItem>> nameSpaces = buildNameSpaces(drivingLicence);
        IssuerSigned issuerSigned = buildIssuerSigned(nameSpaces);
        return new Document(MOBILE_DRIVING_LICENCE_DOCUMENT_TYPE, issuerSigned);
    }

    private Map<String, List<IssuerSignedItem>> buildNameSpaces(
            final DrivingLicenceDocument drivingLicence) {
        ObjectMapper objectMapper = new ObjectMapper();
        objectMapper.registerModule(new JavaTimeModule());
        Map<String, Object> drivingLicenceMap =
                objectMapper.convertValue(drivingLicence, Map.class);

        List<IssuerSignedItem> issuerSignedItems = new ArrayList<>();
        for (Map.Entry<String, Object> entry : drivingLicenceMap.entrySet()) {
            String fieldName = entry.getKey();
            Object fieldValue = entry.getValue();

            String asSnakeCase = CamelToSnakeCaseConvertor.convert(fieldName);

            IssuerSignedItem issuerSignedItem =
                    issuerSignedItemFactory.build(asSnakeCase, fieldValue);
            issuerSignedItems.add(issuerSignedItem);
        }
        return Map.of(MOBILE_DRIVING_LICENCE_NAMESPACE, issuerSignedItems);
    }

    private IssuerSigned buildIssuerSigned(final Map<String, List<IssuerSignedItem>> nameSpaces) {
        return new IssuerSigned(nameSpaces, new IssuerAuth());
    }
}
