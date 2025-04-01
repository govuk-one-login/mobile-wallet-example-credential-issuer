package uk.gov.di.mobile.wallet.cri.credential.mobile_driving_licence.mdoc;

/** Utility class for converting camel case strings to snake case. */
public final class CamelToSnakeCaseConvertor {

    private CamelToSnakeCaseConvertor() {
        // Should never be instantiated
    }

    /**
     * Converts a camel case string to snake case.
     *
     * @param camelCase The camel case string to convert.
     * @return The snake case representation of the input string, or the input string if it is null
     *     or empty.
     */
    public static String convert(String camelCase) {
        if (camelCase == null || camelCase.isEmpty()) {
            return camelCase;
        }
        StringBuilder snakeCase = new StringBuilder();
        for (char character : camelCase.toCharArray()) {
            if (Character.isUpperCase(character)) {
                snakeCase.append("_").append(Character.toLowerCase(character));
            } else {
                snakeCase.append(character);
            }
        }
        return snakeCase.toString();
    }
}
