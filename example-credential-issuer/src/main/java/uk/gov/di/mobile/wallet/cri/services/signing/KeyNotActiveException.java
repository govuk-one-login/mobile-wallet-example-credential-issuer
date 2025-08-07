package uk.gov.di.mobile.wallet.cri.services.signing;

public class KeyNotActiveException extends Exception {
    public KeyNotActiveException(String message) {
        super(message);
    }
}
