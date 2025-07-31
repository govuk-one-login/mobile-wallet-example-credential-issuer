package uk.gov.di.mobile.wallet.cri.services.data_storage;

import uk.gov.di.mobile.wallet.cri.models.CachedCredentialOffer;

public interface DataStore {

    void saveCredentialOffer(CachedCredentialOffer cachedCredentialOffer) throws DataStoreException;

    CachedCredentialOffer getCredentialOffer(String credentialOfferId) throws DataStoreException;

    void updateCredentialOffer(CachedCredentialOffer cachedCredentialOffer)
            throws DataStoreException;
}
