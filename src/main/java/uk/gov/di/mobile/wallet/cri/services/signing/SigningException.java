package uk.gov.di.mobile.wallet.cri.services.signing;

public class SigningException extends Exception {

    public SigningException(String message, Exception exception) {
        super(message, exception);
    }
}
