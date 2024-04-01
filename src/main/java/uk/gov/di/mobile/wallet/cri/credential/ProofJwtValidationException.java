package uk.gov.di.mobile.wallet.cri.credential;

public class ProofJwtValidationException extends Exception {
    public ProofJwtValidationException(String message, Exception exception) {
        super(message, exception);
    }

    public ProofJwtValidationException(String message) {
        super(message);
    }
}
