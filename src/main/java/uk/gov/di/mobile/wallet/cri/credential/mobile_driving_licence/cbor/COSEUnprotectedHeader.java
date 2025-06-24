package uk.gov.di.mobile.wallet.cri.credential.mobile_driving_licence.cbor;

import java.security.cert.X509Certificate;
import java.util.List;

public class COSEUnprotectedHeader {
    private List<X509Certificate> x5chain;

    public COSEUnprotectedHeader(List<X509Certificate> x5chain) {
        this.x5chain = x5chain;
    }
}
