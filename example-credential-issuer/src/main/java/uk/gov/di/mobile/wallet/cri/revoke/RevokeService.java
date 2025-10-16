package uk.gov.di.mobile.wallet.cri.revoke;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import uk.gov.di.mobile.wallet.cri.credential.StatusListClient;
import uk.gov.di.mobile.wallet.cri.credential.StatusListException;
import uk.gov.di.mobile.wallet.cri.models.StoredCredential;
import uk.gov.di.mobile.wallet.cri.services.data_storage.DataStore;
import uk.gov.di.mobile.wallet.cri.services.data_storage.DataStoreException;
import uk.gov.di.mobile.wallet.cri.services.signing.SigningException;

import java.util.List;

public class RevokeService {

    private static final Logger LOGGER = LoggerFactory.getLogger(RevokeService.class);

    private final DataStore dataStore;
    private final StatusListClient statusListClient;

    public RevokeService(DataStore dataStore, StatusListClient statusListClient) {
        this.dataStore = dataStore;
        this.statusListClient = statusListClient;
    }

    public void revokeCredential(String documentId)
            throws DataStoreException,
                    CredentialNotFoundException,
                    RevocationException,
                    SigningException {
        List<StoredCredential> credentials = dataStore.getCredentialsByDocumentId(documentId);

        if (credentials.isEmpty()) {
            throw new CredentialNotFoundException(
                    "No credential found for document with ID " + documentId);
        }

        int failureCount = 0;
        for (StoredCredential credential : credentials) {
            try {
                String uri = credential.getStatusListUri();
                int index = credential.getStatusListIndex();
                statusListClient.revokeCredential(index, uri);
                dataStore.deleteCredential(credential.getCredentialIdentifier());
            } catch (StatusListException exception) {
                failureCount++;
                LOGGER.error(
                        "Failed to revoke credential with ID {} and document ID {}: {}",
                        credential.getCredentialIdentifier(),
                        credential.getDocumentId(),
                        exception.getMessage(),
                        exception);
            } catch (DataStoreException exception) {
                LOGGER.error(
                        "Failed to delete revoked credential with ID {} and document ID {}: {}",
                        credential.getCredentialIdentifier(),
                        credential.getDocumentId(),
                        exception.getMessage(),
                        exception);
            }
        }

        if (failureCount > 0) {
            throw new RevocationException("One or more credentials could not be revoked");
        }
    }
}
