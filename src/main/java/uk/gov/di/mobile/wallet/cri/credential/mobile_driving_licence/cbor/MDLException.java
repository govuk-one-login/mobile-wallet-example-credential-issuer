package uk.gov.di.mobile.wallet.cri.credential.mobile_driving_licence.cbor;

public class MDLException extends Exception {

    public MDLException(String message, Exception exception) {
        super(message, exception);
    }
}
