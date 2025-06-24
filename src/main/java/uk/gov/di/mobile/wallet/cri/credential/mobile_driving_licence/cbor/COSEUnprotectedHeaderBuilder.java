package uk.gov.di.mobile.wallet.cri.credential.mobile_driving_licence.cbor;

import java.security.cert.X509Certificate;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

/**
 * Builder for COSE Unprotected Header, as specified in RFC 8152.
 * The unprotected header is a CBOR map.
 */
public class COSEUnprotectedHeaderBuilder {
    private List<X509Certificate> x5chain;
    private final CBOREncoder cborEncoder;

    /**
     * Constructs a builder for COSE unprotected headers.
     * @param cborEncoder Your CBOR encoder instance.
     */
    public COSEUnprotectedHeaderBuilder(CBOREncoder cborEncoder) {
        this.cborEncoder = cborEncoder;
    }

    /**
     * Sets the 'x5chain' parameter for the unprotected header.
     * @param x5chain The certificate chain (usually a list of X509Certificate).
     * @return This builder instance.
     */
    public COSEUnprotectedHeaderBuilder x5chain(List<X509Certificate> x5chain) {
        this.x5chain = x5chain;
        return this;
    }

    /**
     * Builds the COSEUnprotectedHeader according to RFC 8152:
     * 1. Encodes the header map as CBOR.
     * @return COSEUnprotectedHeader containing the CBOR map bytes.
     */
    public COSEUnprotectedHeader build() {
        // Step 1: Build the header map (e.g., {33: x5chain})
        Map<Integer, Object> headerMap = new LinkedHashMap<>();
        headerMap.put(33, x5chain);


        // Step 2: Encode the map to CBOR bytes
        byte[] headerMapCbor = cborEncoder.encode(headerMap);

        // Step 3: Construct the COSEUnprotectedHeader with the final bytes
        return new COSEUnprotectedHeader(headerMapCbor);
    }
}