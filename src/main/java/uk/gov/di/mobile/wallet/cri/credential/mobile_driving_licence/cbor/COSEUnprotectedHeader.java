package uk.gov.di.mobile.wallet.cri.credential.mobile_driving_licence.cbor;

import lombok.Getter;

@Getter
public class COSEUnprotectedHeader {
    private final byte[] cborBytes;

    public COSEUnprotectedHeader(byte[] cborBytes) {
        this.cborBytes = cborBytes;
    }
}