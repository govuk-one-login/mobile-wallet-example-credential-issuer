package uk.gov.di.mobile.wallet.cri.credential.mobile_driving_licence.cose;

import lombok.Getter;

@Getter
public class COSEUnprotectedHeader {
    private byte[] x5chain;

    COSEUnprotectedHeader(COSEUnprotectedHeaderBuilder builder) {
        this.x5chain = builder.x5chain;
    }
}
