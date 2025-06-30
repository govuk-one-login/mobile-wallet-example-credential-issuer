package uk.gov.di.mobile.wallet.cri.services.object_storage;

public interface ObjectStore {

    String getObject(String bucketName, String key) throws ObjectStoreException;
}
