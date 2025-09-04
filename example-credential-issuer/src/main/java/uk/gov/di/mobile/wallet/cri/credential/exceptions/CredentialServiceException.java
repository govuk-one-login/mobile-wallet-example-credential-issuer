package uk.gov.di.mobile.wallet.cri.credential.exceptions;

public class CredentialServiceException extends Exception {

    public CredentialServiceException(String message) {
        super(message);
    }

    public CredentialServiceException(String message, Exception exception) {
        super(message, exception);
    }
}
