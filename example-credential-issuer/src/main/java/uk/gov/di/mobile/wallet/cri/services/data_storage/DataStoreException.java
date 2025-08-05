package uk.gov.di.mobile.wallet.cri.services.data_storage;

public class DataStoreException extends Exception {

    public DataStoreException(String message, Exception exception) {
        super(message, exception);
    }

    public DataStoreException(String message) {
        super(message);
    }
}
