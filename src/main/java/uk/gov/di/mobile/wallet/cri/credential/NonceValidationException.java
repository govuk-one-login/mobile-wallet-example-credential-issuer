package uk.gov.di.mobile.wallet.cri.credential;

public class NonceValidationException extends Exception {

    public NonceValidationException(String message) {
        super(message);
    }

    public NonceValidationException(String message, Exception exception) {
        super(message,  exception);
    }

}
