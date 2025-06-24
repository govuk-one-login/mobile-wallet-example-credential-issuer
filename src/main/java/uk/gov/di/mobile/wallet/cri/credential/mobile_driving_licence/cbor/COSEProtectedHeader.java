package uk.gov.di.mobile.wallet.cri.credential.mobile_driving_licence.cbor;

import lombok.Getter;

@Getter
public class COSEProtectedHeader {
    private final byte[] cborBytes;

    public COSEProtectedHeader(byte[] cborBytes) {
        this.cborBytes = cborBytes;
    }

}