package uk.gov.di.mobile.wallet.cri.credential.mobile_driving_licence.cose;

import lombok.Getter;

@Getter
public class COSEProtectedHeader {
    private final int alg;

    COSEProtectedHeader(COSEProtectedHeaderBuilder builder) {
        this.alg = builder.alg;
    }
}
