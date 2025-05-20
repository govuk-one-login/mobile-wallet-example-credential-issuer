package uk.gov.di.mobile.wallet.cri.credential.mobile_driving_licence.mdoc;

import com.google.common.base.CaseFormat;
import uk.gov.di.mobile.wallet.cri.annotations.Namespace;
import uk.gov.di.mobile.wallet.cri.credential.mobile_driving_licence.DrivingLicenceDocument;
import uk.gov.di.mobile.wallet.cri.credential.mobile_driving_licence.cbor.CBOREncoder;
import uk.gov.di.mobile.wallet.cri.credential.mobile_driving_licence.cbor.MDLException;

import java.lang.reflect.Field;
import java.util.*;

/**
 * Factory for constructing CBOR-encoded issuer-signed items grouped by namespace from a
 * DrivingLicenceDocument.
 *
 * <p>This class uses reflection to extract annotated fields from the document, builds
 * IssuerSignedItem objects, encodes them to CBOR, and organizes them by their namespace.
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

    /**
     * Builds all namespaces for a given {@link DrivingLicenceDocument}.
     *
     * <p>For each field in the document annotated with {@link Namespace}, this method:
     *
     * <ul>
     *   <li>Groups fields by namespace value.
     *   <li>Converts field names to snake_case.
     *   <li>Builds an {@link IssuerSignedItem} for each field and encodes it to CBOR.
     *   <li>Returns a map where each key is a namespace and each value is a list of CBOR-encoded
     *       field items belonging to that namespace.
     * </ul>
     *
     * @param document The driving licence document to process/extract fields from.
     * @return Map from namespace names to lists of CBOR-encoded issuer-signed items.
     * @throws MDLException If reflection fails or encoding fails.
     */
    @SuppressWarnings("java:S3011") // Suppressing "Accessibility bypass" warning
    public Map<String, List<byte[]>> buildAllNamespaces(DrivingLicenceDocument document)
            throws MDLException {
        Map<String, List<byte[]>> namespaces = new LinkedHashMap<>();
        Map<String, List<Field>> fieldsByNamespace = getFieldsByNamespace(document.getClass());
        for (Map.Entry<String, List<Field>> entry : fieldsByNamespace.entrySet()) {
            List<byte[]> issuerSignedItems = new ArrayList<>();
            for (Field field : entry.getValue()) {
                String fieldName = field.getName();
                // The elementIdentifier within an IssuerSignedItem must be in snake case String
                String asSnakeCase = getAsSnakeCase(fieldName);
                field.setAccessible(true);
                Object fieldValue;
                try {
                    fieldValue = field.get(document);
                } catch (IllegalAccessException exception) {
                    throw new MDLException(
                            "Failed to access Driving Licence properties to build IssuerSignedItem",
                            exception);
                }
                IssuerSignedItem issuerSignedItem =
                        issuerSignedItemFactory.build(asSnakeCase, fieldValue);
                byte[] issuerSignedItemBytes = cborEncoder.encode(issuerSignedItem);
                issuerSignedItems.add(issuerSignedItemBytes);
            }
            namespaces.put(entry.getKey(), issuerSignedItems);
        }
        return namespaces;
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

    /**
     * Groups the declared fields of a class by their {@link Namespace} annotation value.
     *
     * @param clazz The class to inspect.
     * @return Map from namespace value to list of fields.
     */
    public static Map<String, List<Field>> getFieldsByNamespace(Class<?> clazz) {
        Map<String, List<Field>> namespaceFields = new HashMap<>();
        for (Field field : clazz.getDeclaredFields()) {
            Namespace namespace = field.getAnnotation(Namespace.class);
            if (namespace != null) {
                String namespaceValue = namespace.value();
                List<Field> fields = namespaceFields.get(namespaceValue);
                if (fields == null) {
                    fields = new ArrayList<>();
                    namespaceFields.put(namespaceValue, fields);
                }
                fields.add(field);
            }
        }
        return namespaceFields;
    }
}
