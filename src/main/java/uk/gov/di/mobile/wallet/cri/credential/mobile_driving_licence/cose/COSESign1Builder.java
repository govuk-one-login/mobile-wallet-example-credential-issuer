package uk.gov.di.mobile.wallet.cri.credential.mobile_driving_licence.cose;

import java.util.Map;

public class COSESign1Builder {
    private byte[] protectedHeader;
    private Map<Integer, Object> unprotectedHeader;
    private byte[] payload;
    private byte[] signature;

    public COSESign1Builder protectedHeader(byte[] header) {
        this.protectedHeader = header;
        return this;
    }

    public COSESign1Builder unprotectedHeader(Map<Integer, Object> header) {
        this.unprotectedHeader = header;
        return this;
    }

    public COSESign1Builder payload(byte[] payload) {
        this.payload = payload;
        return this;
    }

    public COSESign1Builder signature(byte[] signature) {
        this.signature = signature;
        return this;
    }

    public COSESign1 build() {
        return new COSESign1(protectedHeader, unprotectedHeader, payload, signature);
    }
}
