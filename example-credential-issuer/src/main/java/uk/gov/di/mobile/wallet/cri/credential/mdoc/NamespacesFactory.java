package uk.gov.di.mobile.wallet.cri.credential.mdoc;

import uk.gov.di.mobile.wallet.cri.annotations.Namespace;

import java.lang.reflect.Field;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.stream.Collectors;

import static uk.gov.di.mobile.wallet.cri.credential.mdoc.CamelToSnake.camelToSnake;

/**
 * Creates {@link Namespaces} from a document class by reflecting over its fields annotated with
 * {@link Namespace}.
 *
 * <ul>
 *   <li>Fields are grouped by the annotation value (the namespace name).
 *   <li>Field names are converted from camelCase to snake_case for use as data element names.
 *   <li>{@link Optional} fields are skipped when empty; otherwise their contained value is used.
 *   <li>Iteration order is preserved (via {@link LinkedHashMap}) so namespaces appear in a stable
 *       order.
 * </ul>
 */
public class NamespacesFactory<T> {
    private final IssuerSignedItemFactory issuerSignedItemFactory;

    public NamespacesFactory(IssuerSignedItemFactory issuerSignedItemFactory) {
        this.issuerSignedItemFactory = issuerSignedItemFactory;
    }

    /**
     * Builds the {@link Namespaces} for a given document.
     *
     * <p>Reflects over fields annotated with {@link Namespace}, converts each field name to
     * snake_case, and creates an {@link IssuerSignedItem} for its value. Empty {@link Optional}
     * fields are ignored.
     *
     * @param document Annotated document instance
     * @return Namespaces mapped to their {@link IssuerSignedItem} structures
     * @throws MdocException When a field cannot be accessed or when encoding fails
     */
    @SuppressWarnings("java:S3011") // Suppressing "Accessibility bypass" warning
    public Namespaces build(T document) throws MdocException {
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
                    if (fieldValue instanceof Optional<?> optional) {
                        if (optional.isEmpty()) {
                            continue;
                        }
                        fieldValue = optional.get();
                    }
                } catch (IllegalAccessException exception) {
                    throw new MdocException(
                            String.format(
                                    "Failed to access property %s to build IssuerSignedItem",
                                    fieldName),
                            exception);
                }
                IssuerSignedItem issuerSignedItem =
                        issuerSignedItemFactory.build(fieldNameAsSnakeCase, fieldValue);
                issuerSignedItems.add(issuerSignedItem);
            }
            namespaces.put(entry.getKey(), issuerSignedItems);
        }
        return new Namespaces(namespaces);
    }

    /**
     * Groups declared fields by the value of their {@link Namespace} annotation.
     *
     * @param clazz Class to inspect.
     * @return Map from namespace value to list of fields.
     */
    private static Map<String, List<Field>> getFieldsByNamespace(Class<?> clazz) {
        return Arrays.stream(clazz.getDeclaredFields())
                .filter(field -> field.getAnnotation(Namespace.class) != null)
                .collect(
                        Collectors.groupingBy(
                                field -> field.getAnnotation(Namespace.class).value()));
    }
}
