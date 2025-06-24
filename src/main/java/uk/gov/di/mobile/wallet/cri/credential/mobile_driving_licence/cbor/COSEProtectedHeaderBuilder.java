package uk.gov.di.mobile.wallet.cri.credential.mobile_driving_licence.cbor;

public class COSEProtectedHeaderBuilder {
    private COSEAlgorithms alg;

    public COSEProtectedHeaderBuilder alg(COSEAlgorithms alg) {
        this.alg = alg;
        return this;
    }

    public COSEProtectedHeader build() {
        return new COSEProtectedHeader(alg);
    }
}
