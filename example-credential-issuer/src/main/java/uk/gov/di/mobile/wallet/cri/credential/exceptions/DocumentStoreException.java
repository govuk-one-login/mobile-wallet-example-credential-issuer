package uk.gov.di.mobile.wallet.cri.credential.exceptions;

public class DocumentStoreException extends Exception {

    public DocumentStoreException(String message) {
        super(message);
    }

    public DocumentStoreException(String message, Exception exception) {
        super(message, exception);
    }
}
