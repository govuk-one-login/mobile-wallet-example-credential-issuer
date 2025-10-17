package uk.gov.di.mobile.wallet.cri.credential;

public class StatusListClientException extends Exception {

    public StatusListClientException(String message) {
        super(message);
    }

    public StatusListClientException(String message, Exception exception) {
        super(message, exception);
    }
}
