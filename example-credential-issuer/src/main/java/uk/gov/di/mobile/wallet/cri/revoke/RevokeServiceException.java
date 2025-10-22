package uk.gov.di.mobile.wallet.cri.revoke;

public class RevokeServiceException extends Exception {

    public RevokeServiceException(String message) {
        super(message);
    }

    public RevokeServiceException(String message, Exception exception) {
        super(message, exception);
    }
}
