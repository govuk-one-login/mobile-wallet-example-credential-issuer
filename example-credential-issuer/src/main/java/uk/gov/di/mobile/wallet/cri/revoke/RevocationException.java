package uk.gov.di.mobile.wallet.cri.revoke;

public class RevocationException extends Exception {

    public RevocationException(String message) {
        super(message);
    }

    public RevocationException(String message, Exception exception) {
        super(message, exception);
    }
}
