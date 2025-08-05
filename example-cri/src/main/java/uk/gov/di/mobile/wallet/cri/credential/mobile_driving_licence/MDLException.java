package uk.gov.di.mobile.wallet.cri.credential.mobile_driving_licence;

public class MDLException extends RuntimeException {

    public MDLException(String message, Exception exception) {
        super(message, exception);
    }
}
