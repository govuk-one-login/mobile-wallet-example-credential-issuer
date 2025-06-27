package uk.gov.di.mobile.wallet.cri.credential.mobile_driving_licence.cose;

import java.util.LinkedHashMap;
import java.util.Map;

public class COSEUnprotectedHeaderBuilder {
    private final Map<Integer, Object> unprotectedHeader = new LinkedHashMap<>();

    public COSEUnprotectedHeaderBuilder x5chain(byte[] x5chain) {
        if (x5chain == null) {
            throw new IllegalArgumentException("x5chain cannot be null");
        }
        unprotectedHeader.put(33, x5chain);
        return this;
    }

    public COSEUnprotectedHeader build() {
        if (!unprotectedHeader.containsKey(33)) {
            throw new IllegalStateException("x5chain must be set");
        }
        return new COSEUnprotectedHeader(new LinkedHashMap<>(unprotectedHeader));
    }
}
