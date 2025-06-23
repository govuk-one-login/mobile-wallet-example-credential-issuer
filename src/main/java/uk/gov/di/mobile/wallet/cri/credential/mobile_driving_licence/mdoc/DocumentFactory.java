package uk.gov.di.mobile.wallet.cri.credential.mobile_driving_licence.mdoc;

import org.jetbrains.annotations.NotNull;
import uk.gov.di.mobile.wallet.cri.annotations.Namespace;
import uk.gov.di.mobile.wallet.cri.credential.mobile_driving_licence.DocType;
import uk.gov.di.mobile.wallet.cri.credential.mobile_driving_licence.DrivingLicenceDocument;
import uk.gov.di.mobile.wallet.cri.credential.mobile_driving_licence.cbor.CBOREncoder;
import uk.gov.di.mobile.wallet.cri.credential.mobile_driving_licence.cbor.MDLException;

import java.lang.reflect.Field;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.stream.Collectors;

import static uk.gov.di.mobile.wallet.cri.credential.mobile_driving_licence.utils.CamelToSnake.camelToSnake;

/**
 * Factory for constructing CBOR-encoded issuer-signed items grouped by their respective namespace.
 *
 * <p>This class uses reflection to extract annotated fields from a document, builds
 * IssuerSignedItem objects, encodes them to CBOR, and organizes them by their namespace.
 */
public class DocumentFactory {

    private static final String DOC_TYPE = DocType.MDL.getValue();

    private final IssuerSignedItemFactory issuerSignedItemFactory;
    private final MobileSecurityObjectFactory mobileSecurityObjectFactory;
    private final CBOREncoder cborEncoder;

    /**
     * Constructs a DocumentFactory with the necessary dependencies.
     *
     * @param issuerSignedItemFactory Factory to create IssuerSignedItem objects
     * @param mobileSecurityObjectFactory Factory to create MobileSecurityObject
     * @param cborEncoder Encoder to serialize objects into CBOR format
     */
    public DocumentFactory(
            IssuerSignedItemFactory issuerSignedItemFactory,
            MobileSecurityObjectFactory mobileSecurityObjectFactory,
            CBOREncoder cborEncoder) {
        this.issuerSignedItemFactory = issuerSignedItemFactory;
        this.mobileSecurityObjectFactory = mobileSecurityObjectFactory;
        this.cborEncoder = cborEncoder;
    }

    public Document build(final DrivingLicenceDocument drivingLicence) throws MDLException {
        Map<String, List<IssuerSignedItem>> namespaces = buildNamespaces(drivingLicence);
        IssuerSigned issuerSigned = buildIssuerSigned(namespaces);
        return new Document(DOC_TYPE, issuerSigned);
    }

    /**
     * Builds all namespaces for a given {@link DrivingLicenceDocument}.
     *
     * <p>For each field in the document annotated with {@link Namespace}, this method:
     *
     * <ul>
     *   <li>Groups fields by namespace value.
     *   <li>Converts field names to snake_case.
     *   <li>Builds an {@link IssuerSignedItem} for each field.
     *   <li>Returns a map where each key is a namespace and each value is a list of {@link
     *       IssuerSignedItem} objects belonging to that namespace.
     * </ul>
     *
     * @param document The driving licence document to process/extract fields from.
     * @return Map from namespace names to lists of issuer-signed items.
     * @throws MDLException If reflection fails or encoding fails.
     */
    @SuppressWarnings("java:S3011") // Suppressing "Accessibility bypass" warning
    private Map<String, List<IssuerSignedItem>> buildNamespaces(DrivingLicenceDocument document)
            throws MDLException {
        Map<String, List<IssuerSignedItem>> namespaces = new LinkedHashMap<>();
        Map<String, List<Field>> fieldsByNamespace = getFieldsByNamespace(document.getClass());

        for (Map.Entry<String, List<Field>> entry : fieldsByNamespace.entrySet()) {
            List<IssuerSignedItem> issuerSignedItems = new ArrayList<>();
            for (Field field : entry.getValue()) {
                String fieldName = field.getName();
                String fieldNameAsSnakeCase = camelToSnake(fieldName);
                field.setAccessible(true);
                Object fieldValue;
                try {
                    fieldValue = field.get(document);
                    if (fieldValue == Optional.empty()) {
                        continue;
                    }
                } catch (IllegalAccessException exception) {
                    throw new MDLException(
                            String.format(
                                    "Failed to access Driving Licence property %s to build IssuerSignedItem",
                                    fieldName),
                            exception);
                }
                IssuerSignedItem issuerSignedItem =
                        issuerSignedItemFactory.build(fieldNameAsSnakeCase, fieldValue);
                issuerSignedItems.add(issuerSignedItem);
            }
            namespaces.put(entry.getKey(), issuerSignedItems);
        }
        return namespaces;
    }

    /**
     * Groups the declared fields of a class by their {@link Namespace} annotation value.
     *
     * @param clazz The class to inspect.
     * @return Map from namespace value to list of fields.
     */
    private static Map<String, List<Field>> getFieldsByNamespace(Class<?> clazz) {
        return Arrays.stream(clazz.getDeclaredFields())
                .filter(field -> field.getAnnotation(Namespace.class) != null)
                .collect(
                        Collectors.groupingBy(
                                field -> field.getAnnotation(Namespace.class).value()));
    }

    private IssuerSigned buildIssuerSigned(final Map<String, List<IssuerSignedItem>> namespaces)
            throws MDLException {
        MobileSecurityObject mobileSecurityObject = mobileSecurityObjectFactory.build(namespaces);
        byte[] mobileSecurityObjectBytes = cborEncoder.encode(mobileSecurityObject);

        // sign mobileSecurityObjectBytes

        IssuerAuth issuerAuth = new IssuerAuth(mobileSecurityObjectBytes);
        Map<String, List<byte[]>> encodedNamespaces = getEncodedNamespaces(namespaces);

        return new IssuerSigned(encodedNamespaces, issuerAuth);
    }

    private @NotNull Map<String, List<byte[]>> getEncodedNamespaces(
            Map<String, List<IssuerSignedItem>> nameSpaces) throws MDLException {
        Map<String, List<byte[]>> encodedNamespaces = new LinkedHashMap<>();

        for (Map.Entry<String, List<IssuerSignedItem>> entry : nameSpaces.entrySet()) {
            List<byte[]> encodedItems = new ArrayList<>();
            for (IssuerSignedItem item : entry.getValue()) {
                byte[] issuerSignedItemBytes = cborEncoder.encode(item);
                encodedItems.add(issuerSignedItemBytes);
            }
            encodedNamespaces.put(entry.getKey(), encodedItems);
        }
        return encodedNamespaces;
    }
}
