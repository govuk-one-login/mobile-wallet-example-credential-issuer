package uk.gov.di.mobile.wallet.cri.services.signing;

public class SigningException extends Exception {

    public SigningException(Exception exception) {
        super(exception);
    }
}
