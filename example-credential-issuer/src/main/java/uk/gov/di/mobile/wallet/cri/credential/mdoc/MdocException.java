package uk.gov.di.mobile.wallet.cri.credential.mdoc;

public class MdocException extends RuntimeException {

    public MdocException(String message, Exception exception) {
        super(message, exception);
    }
}
