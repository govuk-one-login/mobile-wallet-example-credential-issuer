package uk.gov.di.mobile.wallet.cri.credential.mobile_driving_licence.mdoc;

import com.google.common.base.CaseFormat;
import uk.gov.di.mobile.wallet.cri.annotations.Namespace;
import uk.gov.di.mobile.wallet.cri.credential.mobile_driving_licence.cbor.CBOREncoder;
import uk.gov.di.mobile.wallet.cri.credential.mobile_driving_licence.cbor.MDLException;

import java.lang.reflect.Field;
import java.util.ArrayList;
import java.util.List;

/**
 * Factory class for creating a list of serialized IssuerSignedItems from an input object. This
 * class uses reflection to iterate through the fields of an input object, converts each field's
 * name to snake_case, and creates an IssuerSignedItem for each field.
 */
public class NamespaceFactory {
    /** Factory for creating IssuerSignedItem objects from field names and values */
    private final IssuerSignedItemFactory issuerSignedItemFactory;

    /** Encoder for converting IssuerSignedItem objects to CBOR byte arrays */
    private final CBOREncoder cborEncoder;

    /**
     * Constructs a NamespaceFactory with the necessary dependencies.
     *
     * @param issuerSignedItemFactory Factory to create IssuerSignedItem objects
     * @param cborEncoder Encoder to serialize objects into CBOR format
     */
    public NamespaceFactory(
            IssuerSignedItemFactory issuerSignedItemFactory, CBOREncoder cborEncoder) {
        this.issuerSignedItemFactory = issuerSignedItemFactory;
        this.cborEncoder = cborEncoder;
    }

    public static List<Field> getFieldsByNamespace(Class<?> clazz, String targetNamespace) {
        List<Field> result = new ArrayList<>();
        for (Field field : clazz.getDeclaredFields()) {
            Namespace ns = field.getAnnotation(Namespace.class);
            if (ns != null && ns.value().equals(targetNamespace)) {
                result.add(field);
            }
        }
        return result;
    }

    /**
     * Builds a list of serialized IssuerSignedItems from the input object's fields. Uses reflection
     * to access each field, converts field names to snake_case, then creates and serializes an
     * IssuerSignedItem for each field.
     *
     * @param input The object whose fields will be processed
     * @return A list of byte arrays, each representing a serialized IssuerSignedItem
     * @throws MDLException If there's an error accessing fields or serializing items
     */
    @SuppressWarnings("java:S3011") // Suppressing "Accessibility bypass" warning
    public List<byte[]> build(Object input, String targetNamespace) throws MDLException {
        List<byte[]> issuerSignedItems = new ArrayList<>();
        for (Field field : input.getClass().getDeclaredFields()) {
            Namespace namespace = field.getAnnotation(Namespace.class);
            if (namespace != null && !namespace.value().equals(targetNamespace)) {
                continue;
            }

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

    /**
     * Converts a camelCase string to snake_case.
     *
     * @param fieldName The field name in camelCase format
     * @return The field name converted to snake_case format
     */
    private static String getAsSnakeCase(String fieldName) {
        return CaseFormat.LOWER_CAMEL.to(CaseFormat.LOWER_UNDERSCORE, fieldName);
    }
}
