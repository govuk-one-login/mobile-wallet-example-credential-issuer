package uk.gov.di.mobile.wallet.cri.credential.mobile_driving_licence.cbor;

import java.util.Map;

public class COSEUnprotectedHeader {
    private final Map<Integer, Object> headerMap;

    public COSEUnprotectedHeader(Map<Integer, Object> headerMap) {
        this.headerMap = headerMap;
    }

    public Map<Integer, Object> getHeaderMap() {
        return headerMap;
    }
}