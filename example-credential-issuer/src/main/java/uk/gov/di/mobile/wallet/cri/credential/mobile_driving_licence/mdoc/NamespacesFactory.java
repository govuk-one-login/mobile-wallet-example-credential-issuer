package uk.gov.di.mobile.wallet.cri.credential.mobile_driving_licence.mdoc;

import uk.gov.di.mobile.wallet.cri.annotations.Namespace;
import uk.gov.di.mobile.wallet.cri.credential.mobile_driving_licence.DrivingLicenceDocument;
import uk.gov.di.mobile.wallet.cri.credential.mobile_driving_licence.DrivingPrivilege;
import uk.gov.di.mobile.wallet.cri.credential.mobile_driving_licence.MDLException;

import java.lang.reflect.Field;
import java.lang.reflect.Modifier;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.stream.Collectors;

import static uk.gov.di.mobile.wallet.cri.credential.mobile_driving_licence.mdoc.CamelToSnake.camelToSnake;

public class NamespacesFactory {
    private final IssuerSignedItemFactory issuerSignedItemFactory;

    public NamespacesFactory(IssuerSignedItemFactory issuerSignedItemFactory) {
        this.issuerSignedItemFactory = issuerSignedItemFactory;
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
    public Namespaces build(DrivingLicenceDocument document) throws MDLException {
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

                    if ("driving_privileges".equals(fieldNameAsSnakeCase)) {
                        fieldValue =
                                convertDrivingPrivilegesToSnakeCase(
                                        (List<DrivingPrivilege>) fieldValue);
                    }

                    if ("provisional_driving_privileges".equals(fieldNameAsSnakeCase)) {
                        Optional<List<DrivingPrivilege>> optionalPrivileges =
                                (Optional<List<DrivingPrivilege>>) fieldValue;
                        if (optionalPrivileges.isPresent()) {
                            fieldValue = convertDrivingPrivilegesToSnakeCase(optionalPrivileges);
                        }
                    }

                } catch (IllegalAccessException exception) {
                    throw new MDLException(
                            String.format(
                                    "Failed to access driving licence property %s to build IssuerSignedItem",
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

    /**
     * Converts a List or Optional List of DrivingPrivilege objects to a List of Maps with
     * snake_case keys.
     *
     * @param optionalDrivingPrivileges Optional wrapping List of DrivingPrivilege objects
     * @return List of Maps with snake_case keys representing each DrivingPrivilege
     * @throws IllegalAccessException if reflective access fails
     */
    public List<Map<String, Object>> convertDrivingPrivilegesToSnakeCase(
            Optional<List<DrivingPrivilege>> optionalDrivingPrivileges)
            throws IllegalAccessException {
        return convertDrivingPrivilegesToSnakeCase(optionalDrivingPrivileges.get());
    }

    /**
     * Converts a List of DrivingPrivilege objects to a List of Maps with snake_case keys.
     *
     * @param drivingPrivileges List of DrivingPrivilege objects
     * @return List of Maps with snake_case keys representing each DrivingPrivilege
     * @throws IllegalAccessException if reflective access fails
     */
    public List<Map<String, Object>> convertDrivingPrivilegesToSnakeCase(
            List<DrivingPrivilege> drivingPrivileges) throws IllegalAccessException {
        List<Map<String, Object>> result = new ArrayList<>();
        for (DrivingPrivilege privilege : drivingPrivileges) {
            Map<String, Object> map = new LinkedHashMap<>();
            for (Field field : privilege.getClass().getDeclaredFields()) {
                if (shouldSkipField(field)) {
                    continue;
                }
                field.setAccessible(true);
                String snakeKey = camelToSnake(field.getName());
                Object fieldValue = field.get(privilege);
                if (fieldValue instanceof Optional<?> optionalValue) {
                    if (optionalValue.isEmpty()) {
                        continue;
                    }
                    fieldValue = optionalValue.get();
                }
                map.put(snakeKey, fieldValue);
            }
            result.add(map);
        }
        return result;
    }

    private boolean shouldSkipField(Field field) {
        return Modifier.isStatic(field.getModifiers()) || field.isSynthetic();
    }
}
