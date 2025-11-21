package uk.gov.di.mobile.wallet.cri.credential.mobile_driving_licence.cose;

public class COSEUnprotectedHeaderBuilder {
    byte[] x5chain;

    public COSEUnprotectedHeaderBuilder x5chain(byte[] x5chain) {
        if (x5chain == null) {
            throw new IllegalArgumentException("x5chain cannot be null");
        }
        this.x5chain = x5chain;
        return this;
    }

    public COSEUnprotectedHeader build() {
        if (this.x5chain == null) {
            throw new IllegalStateException("x5chain must be set");
        }
        return new COSEUnprotectedHeader(this);
    }
}
