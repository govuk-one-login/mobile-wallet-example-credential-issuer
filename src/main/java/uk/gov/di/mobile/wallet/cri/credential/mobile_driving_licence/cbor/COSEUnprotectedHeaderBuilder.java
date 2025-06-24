package uk.gov.di.mobile.wallet.cri.credential.mobile_driving_licence.cbor;

import java.util.LinkedHashMap;
import java.util.Map;

/**
 * Builder for COSE Unprotected Header, as specified in RFC 8152.
 * The unprotected header is a CBOR map.
 */
public class COSEUnprotectedHeaderBuilder {
    private final Map<Integer, Object> headerMap = new LinkedHashMap<>();
    private final CBOREncoder cborEncoder;

    /**
     * Constructs a builder for COSE unprotected headers.
     * @param cborEncoder Your CBOR encoder instance.
     */
    public COSEUnprotectedHeaderBuilder(CBOREncoder cborEncoder) {
        this.cborEncoder = cborEncoder;
    }

    public COSEUnprotectedHeaderBuilder x5chain(Object x5chain) {
        headerMap.put(33, x5chain);
        return this;
    }

    public COSEUnprotectedHeader build() {
        return new COSEUnprotectedHeader(new LinkedHashMap<>(headerMap));
    }
}