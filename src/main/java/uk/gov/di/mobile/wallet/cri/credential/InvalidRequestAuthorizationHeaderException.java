package uk.gov.di.mobile.wallet.cri.credential;

public class InvalidRequestAuthorizationHeaderException extends Exception {
    public InvalidRequestAuthorizationHeaderException() {
        super("Invalid access token");
    }

    public InvalidRequestAuthorizationHeaderException(Exception exception) {
        super("Invalid access token: " + exception);
    }
}
