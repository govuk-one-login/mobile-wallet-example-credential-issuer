package uk.gov.di.mobile.wallet.cri.credential.mobile_driving_licence.cbor;

public class COSEProtectedHeader {
    private COSEAlgorithms alg;

    public COSEProtectedHeader(COSEAlgorithms alg) {
        this.alg = alg;
    }
}
