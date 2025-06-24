package uk.gov.di.mobile.wallet.cri.credential.mobile_driving_licence.cbor;

import java.util.LinkedHashMap;
import java.util.Map;

public class COSEProtectedHeaderBuilder {
    private Integer alg;
    private final CBOREncoder cborEncoder;


    public COSEProtectedHeaderBuilder(CBOREncoder cborEncoder) {
        this.cborEncoder = cborEncoder;
    }

    /**
     * Sets the 'alg' (algorithm) parameter for the protected header.
     * @param alg The COSE algorithm enum value.
     * @return This builder instance.
     */
    public COSEProtectedHeaderBuilder alg(COSEAlgorithms alg) {
        this.alg = alg.getId();
        return this;
    }

    /**
     * Builds the COSEProtectedHeader according to RFC 8152:
     * 1. Encodes the header map as CBOR.
     * 2. Wraps the result in a CBOR byte string.
     * @return COSEProtectedHeader containing the CBOR byte string.
     */
    public COSEProtectedHeader build() {
        // Step 1: Build the header map (e.g., {1: -7})
        Map<Integer, Integer> headerMap = new LinkedHashMap<>();
        headerMap.put(1, alg);

        // Step 2: Encode the map to CBOR bytes
        byte[] headerMapCbor = cborEncoder.encode(headerMap);

        // Step 3: Wrap the CBOR map bytes in a CBOR byte string, as required by COSE
        byte[] protectedHeaderCbor = cborEncoder.encode(headerMapCbor);

        // Step 4: Construct the COSEProtectedHeader with the final bytes
        return new COSEProtectedHeader(protectedHeaderCbor);
    }
}