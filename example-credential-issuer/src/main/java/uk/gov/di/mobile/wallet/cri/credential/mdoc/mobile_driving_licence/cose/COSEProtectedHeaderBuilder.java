package uk.gov.di.mobile.wallet.cri.credential.mdoc.mobile_driving_licence.cose;

import java.util.LinkedHashMap;
import java.util.Map;

public class COSEProtectedHeaderBuilder {
    private final Map<Integer, Object> protectedHeader = new LinkedHashMap<>();

    public COSEProtectedHeaderBuilder alg(int alg) {
        protectedHeader.put(1, alg);
        return this;
    }

    public COSEProtectedHeader build() {
        if (!protectedHeader.containsKey(1)) {
            throw new IllegalStateException("alg must be set");
        }
        return new COSEProtectedHeader(new LinkedHashMap<>(protectedHeader));
    }
}
