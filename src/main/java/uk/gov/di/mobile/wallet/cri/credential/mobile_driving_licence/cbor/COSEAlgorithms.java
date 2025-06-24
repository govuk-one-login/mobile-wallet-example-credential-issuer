package uk.gov.di.mobile.wallet.cri.credential.mobile_driving_licence.cbor;

import lombok.Getter;

@Getter
public enum COSEAlgorithms {
    ES256(-7); // ECDSA w/ SHA-256

    private final int id;

    COSEAlgorithms(int id) {
        this.id = id;
    }
}
