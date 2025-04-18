package uk.gov.di.mobile.wallet.cri.credential.mobile_driving_licence.mdoc;

import com.google.common.base.CaseFormat;
import uk.gov.di.mobile.wallet.cri.credential.mobile_driving_licence.cbor.CBOREncoder;
import uk.gov.di.mobile.wallet.cri.credential.mobile_driving_licence.cbor.MDLException;

import java.lang.reflect.Field;
import java.util.ArrayList;
import java.util.List;

public class NamespaceBuilder {
    private final IssuerSignedItemFactory issuerSignedItemFactory;
    private final CBOREncoder cborEncoder;

    public NamespaceBuilder(
            IssuerSignedItemFactory issuerSignedItemFactory, CBOREncoder cborEncoder) {
        this.issuerSignedItemFactory = issuerSignedItemFactory;
        this.cborEncoder = cborEncoder;
    }

    @SuppressWarnings("java:S3011") // Suppressing "Accessibility bypass" warning
    public List<byte[]> buildNamespace(Object input) throws MDLException {
        List<byte[]> issuerSignedItems = new ArrayList<>();
        for (Field field : input.getClass().getDeclaredFields()) {
            String fieldName = field.getName();
            // The elementIdentifier within an IssuerSignedItem must be in snake case String
            String asSnakeCase = getAsSnakeCase(fieldName);
            field.setAccessible(true);
            Object fieldValue;
            try {
                fieldValue = field.get(input);
            } catch (IllegalAccessException exception) {
                throw new MDLException(
                        "Filed to access Driving Licence properties to build IssuerSignedItem",
                        exception);
            }
            IssuerSignedItem issuerSignedItem =
                    issuerSignedItemFactory.build(asSnakeCase, fieldValue);
            byte[] issuerSignedItemBytes = cborEncoder.encode(issuerSignedItem);
            issuerSignedItems.add(issuerSignedItemBytes);
        }
        return issuerSignedItems;
    }

    private static String getAsSnakeCase(String fieldName) {
        return CaseFormat.LOWER_CAMEL.to(CaseFormat.LOWER_UNDERSCORE, fieldName);
    }
}
