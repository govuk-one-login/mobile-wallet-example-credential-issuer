package uk.gov.di.mobile.wallet.cri.credential.mobile_driving_licence.cose;

public class COSEProtectedHeaderBuilder {
    int alg;

    public COSEProtectedHeaderBuilder alg(int alg) {
        this.alg = alg;
        return this;
    }

    public COSEProtectedHeader build() {
        return new COSEProtectedHeader(this);
    }
}
