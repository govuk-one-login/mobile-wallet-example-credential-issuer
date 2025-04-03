package uk.gov.di.mobile.wallet.cri.credential;

public class CredentialServiceException extends Exception {

    public CredentialServiceException(String message) {
        super(message);
    }

    public CredentialServiceException(String message, Exception cause) {
        super(message, cause);
    }
}
