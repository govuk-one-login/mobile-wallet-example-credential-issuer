package uk.gov.di.mobile.wallet.cri.services.data_storage;

import uk.gov.di.mobile.wallet.cri.models.CachedCredentialOffer;
import uk.gov.di.mobile.wallet.cri.models.StoredCredential;

public interface DataStore {

    void saveCredentialOffer(CachedCredentialOffer cachedCredentialOffer) throws DataStoreException;

    CachedCredentialOffer getCredentialOffer(String credentialOfferId) throws DataStoreException;

    void updateCredentialOffer(CachedCredentialOffer cachedCredentialOffer)
            throws DataStoreException;

    void saveCredential(StoredCredential storedCredential) throws DataStoreException;

    StoredCredential getCredential(String credentialId) throws DataStoreException;

    void deleteCredential(String credentialId) throws DataStoreException;
}
