package uk.gov.di.mobile.wallet.cri.notification;

public class InvalidNotificationRequestException extends Exception {

    public InvalidNotificationRequestException(String message, Exception exception) {
        super(message, exception);
    }

    public InvalidNotificationRequestException(String message) {
        super(message);
    }
}
