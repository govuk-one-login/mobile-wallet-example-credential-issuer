package uk.gov.di.mobile.wallet.cri.credential.mobile_driving_licence.cbor;

import java.nio.charset.StandardCharsets;

public class COSESign1Builder {
    private COSEProtectedHeader protectedHeader;
    private COSEUnprotectedHeader unprotectedHeader;
    private byte[] payload;
    private byte[] signature;

    public COSESign1Builder protectedHeader(COSEProtectedHeader header) {
        this.protectedHeader = header;
        return this;
    }

    public COSESign1Builder unprotectedHeader(COSEUnprotectedHeader header) {
        this.unprotectedHeader = header;
        return this;
    }

    public COSESign1Builder payload(String payload) {
        this.payload = payload.getBytes(StandardCharsets.UTF_8);
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
