package uk.gov.di.mobile.wallet.cri.credential.mobile_driving_licence.mdoc;

import uk.gov.di.mobile.wallet.cri.credential.mobile_driving_licence.DrivingLicenceDocument;

import java.lang.reflect.Field;
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
            final DrivingLicenceDocument drivingLicence) throws IllegalAccessException {
        List<IssuerSignedItem> issuerSignedItems = new ArrayList<>();
        for (Field field : drivingLicence.getClass().getDeclaredFields()) {
            String fieldName = field.getName();

            String asSnakeCase = CamelToSnakeCaseConvertor.convert(fieldName);
            field.setAccessible(true);

            Object fieldValue = field.get(drivingLicence);

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
