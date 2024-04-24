package uk.gov.di.mobile.wallet.cri.did_document;

public class DidDocumentException extends RuntimeException {
    public DidDocumentException(String message) {
        this(message, null);
    }

    public DidDocumentException(String message, Exception exception) {
        super(message, exception);
    }
}
