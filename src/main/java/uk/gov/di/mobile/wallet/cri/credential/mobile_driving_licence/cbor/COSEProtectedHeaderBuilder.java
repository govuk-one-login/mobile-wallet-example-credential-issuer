package uk.gov.di.mobile.wallet.cri.credential.mobile_driving_licence.cbor;

import java.util.LinkedHashMap;
import java.util.Map;

public class COSEProtectedHeaderBuilder {
    private final Map<Integer, Object> headerMap = new LinkedHashMap<>();
    private final CBOREncoder cborEncoder;

    public COSEProtectedHeaderBuilder(CBOREncoder cborEncoder) {
        this.cborEncoder = cborEncoder;
    }

    public COSEProtectedHeaderBuilder alg(COSEAlgorithms alg) {
        headerMap.put(1, alg.getId());
        return this;
    }

    public COSEProtectedHeader build() {
        byte[] protectedHeaderCbor = cborEncoder.encode(headerMap);
        return new COSEProtectedHeader(protectedHeaderCbor);
    }
}