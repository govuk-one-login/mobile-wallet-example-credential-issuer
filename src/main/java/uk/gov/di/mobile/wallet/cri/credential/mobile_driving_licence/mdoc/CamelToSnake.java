package uk.gov.di.mobile.wallet.cri.credential.mobile_driving_licence.mdoc;

import com.google.common.base.CaseFormat;

/** Utility class to convert a camelCase string to snake_case. */
public final class CamelToSnake {

    private CamelToSnake() {
        // Can't be instantiated
    }

    /**
     * Converts a camelCase string to snake_case.
     *
     * @param camelCase The string in camelCase format
     * @return The string converted to snake_case format
     */
    public static String camelToSnake(String camelCase) {
        if (camelCase == null || camelCase.isEmpty()) {
            return camelCase;
        }
        return CaseFormat.LOWER_CAMEL.to(CaseFormat.LOWER_UNDERSCORE, camelCase);
    }
}
