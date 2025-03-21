package uk.gov.di.mobile.wallet.cri.services.authentication;

public class AccessTokenValidationException extends Exception {

    public AccessTokenValidationException(String message, Exception exception) {
        super(message, exception);
    }

    public AccessTokenValidationException(String message) {
        super(message);
    }
}
