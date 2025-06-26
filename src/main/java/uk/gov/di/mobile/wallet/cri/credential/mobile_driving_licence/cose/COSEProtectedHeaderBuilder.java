package uk.gov.di.mobile.wallet.cri.credential.mobile_driving_licence.cose;

import java.util.LinkedHashMap;
import java.util.Map;

public class COSEProtectedHeaderBuilder {
    private final Map<Integer, Object> protectedHeader = new LinkedHashMap<>();

    public COSEProtectedHeaderBuilder() {}

    public COSEProtectedHeaderBuilder alg(int alg) {
        protectedHeader.put(1, alg);
        return this;
    }

    public COSEProtectedHeader build() {
        return new COSEProtectedHeader(protectedHeader);
    }
}
