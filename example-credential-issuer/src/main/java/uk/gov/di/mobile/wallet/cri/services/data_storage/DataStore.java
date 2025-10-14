package uk.gov.di.mobile.wallet.cri.services.data_storage;

import uk.gov.di.mobile.wallet.cri.models.CachedCredentialOffer;
import uk.gov.di.mobile.wallet.cri.models.StoredCredential;

import java.util.List;

public interface DataStore {

    void saveCredentialOffer(CachedCredentialOffer cachedCredentialOffer) throws DataStoreException;

    CachedCredentialOffer getCredentialOffer(String credentialOfferId) throws DataStoreException;

    void deleteCredentialOffer(String credentialOfferId) throws DataStoreException;

    void saveStoredCredential(StoredCredential storedCredential) throws DataStoreException;

    StoredCredential getStoredCredential(String credentialId) throws DataStoreException;

    List<StoredCredential> getCredentialsByDocumentId(String documentId) throws DataStoreException;
}
