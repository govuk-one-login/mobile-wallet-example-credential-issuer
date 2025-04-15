package uk.gov.di.mobile.wallet.cri.credential.mobile_driving_licence.cbor;

public class CBOREncodingException extends Exception {

    public CBOREncodingException(String message, Exception exception) {
        super(message, exception);
    }
}
