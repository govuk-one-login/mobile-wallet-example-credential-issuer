package uk.gov.di.mobile.wallet.cri.services.object_storage;

public interface ObjectStore {

    byte[] getObject(String bucketName, String key) throws ObjectStoreException;
}
