package uk.gov.di.mobile.wallet.cri.revoke;

import uk.gov.di.mobile.wallet.cri.models.StoredCredential;
import uk.gov.di.mobile.wallet.cri.services.data_storage.DataStore;
import uk.gov.di.mobile.wallet.cri.services.data_storage.DataStoreException;

import java.util.List;

public class RevokeService {

    private final DataStore dataStore;

    public RevokeService(DataStore dataStore) {
        this.dataStore = dataStore;
    }

    public void revokeCredential(String documentPrimaryIdentifier)
            throws DataStoreException, CredentialNotFoundException {
        List<StoredCredential> credentials =
                dataStore.getCredentialsByDocumentPrimaryIdentifier(documentPrimaryIdentifier);

        if (credentials.isEmpty()) {
            throw new CredentialNotFoundException(
                    "No credential found for document number " + documentPrimaryIdentifier);
        }

        // Revoke credentials found

    }
}
