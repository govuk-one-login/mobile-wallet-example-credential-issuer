package uk.gov.di.mobile.wallet.cri.services.object_storage;

public class ObjectStoreException extends Exception {

    public ObjectStoreException(String message, Exception exception) {
        super(message, exception);
    }
}
