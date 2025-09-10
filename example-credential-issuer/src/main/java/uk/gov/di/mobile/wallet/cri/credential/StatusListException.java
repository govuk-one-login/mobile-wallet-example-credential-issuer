package uk.gov.di.mobile.wallet.cri.credential;

public class StatusListException extends Exception {

    public StatusListException(String message) {
        super(message);
    }

    public StatusListException(String message, Exception exception) {
        super(message, exception);
    }
}
