package uk.gov.di.mobile.wallet.cri.notification;

public class RequestBodyValidationException extends Exception {

    public RequestBodyValidationException(String message, Exception exception) {
        super(message, exception);
    }

    public RequestBodyValidationException(String message) {
        super(message);
    }
}
