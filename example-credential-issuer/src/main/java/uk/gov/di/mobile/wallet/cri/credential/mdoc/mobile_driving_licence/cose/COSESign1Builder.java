package uk.gov.di.mobile.wallet.cri.credential.mdoc.mobile_driving_licence.cose;

import java.util.Map;

public class COSESign1Builder {
    private byte[] protectedHeader;
    private Map<Integer, Object> unprotectedHeader;
    private byte[] payload;
    private byte[] signature;

    public COSESign1Builder protectedHeader(byte[] header) {
        if (header == null) {
            throw new IllegalArgumentException("protectedHeader cannot be null");
        }
        this.protectedHeader = header;
        return this;
    }

    public COSESign1Builder unprotectedHeader(Map<Integer, Object> header) {
        if (header == null) {
            throw new IllegalArgumentException("unprotectedHeader cannot be null");
        }
        this.unprotectedHeader = header;
        return this;
    }

    public COSESign1Builder payload(byte[] payload) {
        if (payload == null) {
            throw new IllegalArgumentException("payload cannot be null");
        }
        this.payload = payload;
        return this;
    }

    public COSESign1Builder signature(byte[] signature) {
        if (signature == null) {
            throw new IllegalArgumentException("signature cannot be null");
        }
        this.signature = signature;
        return this;
    }

    public COSESign1 build() {
        if (protectedHeader == null
                || unprotectedHeader == null
                || payload == null
                || signature == null) {
            throw new IllegalStateException("All fields must be set and non-null");
        }
        return new COSESign1(protectedHeader, unprotectedHeader, payload, signature);
    }
}
