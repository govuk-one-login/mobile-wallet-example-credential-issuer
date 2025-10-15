package uk.gov.di.mobile.wallet.cri.revoke;

import uk.gov.di.mobile.wallet.cri.credential.StatusListClient;
import uk.gov.di.mobile.wallet.cri.credential.StatusListException;
import uk.gov.di.mobile.wallet.cri.models.StoredCredential;
import uk.gov.di.mobile.wallet.cri.services.data_storage.DataStore;
import uk.gov.di.mobile.wallet.cri.services.data_storage.DataStoreException;
import uk.gov.di.mobile.wallet.cri.services.signing.SigningException;

import java.util.List;

public class RevokeService {

    private final DataStore dataStore;
    private final StatusListClient statusListClient;

    public RevokeService(DataStore dataStore, StatusListClient statusListClient) {
        this.dataStore = dataStore;
        this.statusListClient = statusListClient;
    }

    public void revokeCredential(String documentId)
            throws DataStoreException,
                    CredentialNotFoundException,
                    StatusListException,
                    SigningException {
        List<StoredCredential> credentials = dataStore.getCredentialsByDocumentId(documentId);

        if (credentials.isEmpty()) {
            throw new CredentialNotFoundException(
                    "No credential found for document with ID " + documentId);
        }

        for (StoredCredential credential : credentials) {
            int index = credential.getStatusListIndex();
            String uri = credential.getStatusListUri();

            statusListClient.revokeCredential(index, uri);
        }
    }
}
