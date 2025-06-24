package uk.gov.di.mobile.wallet.cri.credential.mobile_driving_licence.cbor;

import java.security.cert.X509Certificate;
import java.util.List;

public class COSEUnprotectedHeaderBuilder {
    private List<X509Certificate> x5chain;

    public COSEUnprotectedHeaderBuilder x5chain(List<X509Certificate> x5chain) {
        this.x5chain = x5chain;
        return this;
    }

    public COSEUnprotectedHeader build() {
        return new COSEUnprotectedHeader(x5chain);
    }
}
