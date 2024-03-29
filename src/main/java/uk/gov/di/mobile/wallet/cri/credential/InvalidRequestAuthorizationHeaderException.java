package uk.gov.di.mobile.wallet.cri.credential;

public class InvalidRequestException extends Exception {
    public InvalidRequestException() {
        super("Invalid access token");
    }

    public InvalidRequestException(Exception exception) {
        super("Invalid access token: " + exception);
    }
}
