package uk.gov.di.mobile.wallet.cri.credential.mobile_driving_licence.cose;

public class COSESign1 {
    private final COSEProtectedHeader protectedHeader;
    private final COSEUnprotectedHeader unprotectedHeader;
    private final byte[] payload;
    private final byte[] signature;

    public COSESign1(
            COSEProtectedHeader protectedHeader,
            COSEUnprotectedHeader unprotectedHeader,
            byte[] payload,
            byte[] signature) {
        this.protectedHeader = protectedHeader;
        this.unprotectedHeader = unprotectedHeader;
        this.payload = payload;
        this.signature = signature;
    }

    public COSEProtectedHeader getProtectedHeader() {
        return protectedHeader;
    }

    public COSEUnprotectedHeader getUnprotectedHeader() {
        return unprotectedHeader;
    }

    public byte[] getPayload() {
        return payload;
    }

    public byte[] getSignature() {
        return signature;
    }
}
