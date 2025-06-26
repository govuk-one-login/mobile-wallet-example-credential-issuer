package uk.gov.di.mobile.wallet.cri.credential.mobile_driving_licence.cose;

import java.util.LinkedHashMap;
import java.util.Map;

public class COSEUnprotectedHeaderBuilder {
    private final Map<Integer, Object> unprotectedHeader = new LinkedHashMap<>();

    public COSEUnprotectedHeaderBuilder x5chain(Object x5chain) {
        unprotectedHeader.put(33, x5chain);
        return this;
    }

    public COSEUnprotectedHeader build() {
        return new COSEUnprotectedHeader(new LinkedHashMap<>(unprotectedHeader));
    }
}
