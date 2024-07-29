package uk.gov.di.mobile.wallet.cri.services.data_storage;

import uk.gov.di.mobile.wallet.cri.models.CredentialOfferCacheItem;

public interface DataStore {

    void saveCredentialOffer(CredentialOfferCacheItem credentialOfferCacheItem)
            throws DataStoreException;

    CredentialOfferCacheItem getCredentialOffer(String partitionValue) throws DataStoreException;

    void deleteCredentialOffer(String partitionValue) throws DataStoreException;
}
