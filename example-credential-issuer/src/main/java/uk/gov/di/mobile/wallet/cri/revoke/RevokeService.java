package uk.gov.di.mobile.wallet.cri.revoke;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import uk.gov.di.mobile.wallet.cri.credential.StatusListClient;
import uk.gov.di.mobile.wallet.cri.credential.StatusListClientException;
import uk.gov.di.mobile.wallet.cri.models.StoredCredential;
import uk.gov.di.mobile.wallet.cri.services.data_storage.DataStore;
import uk.gov.di.mobile.wallet.cri.services.data_storage.DataStoreException;

import java.util.List;

public class RevokeService {

    private static final Logger LOGGER = LoggerFactory.getLogger(RevokeService.class);

    private final DataStore dataStore;
    private final StatusListClient statusListClient;

    public RevokeService(DataStore dataStore, StatusListClient statusListClient) {
        this.dataStore = dataStore;
        this.statusListClient = statusListClient;
    }

    public void revokeCredentials(String documentId)
            throws CredentialNotFoundException, RevokeServiceException {
        List<StoredCredential> credentials;
        try {
            credentials = dataStore.getCredentialsByDocumentId(documentId);
            if (credentials.isEmpty()) {
                throw new CredentialNotFoundException(
                        "No credential found for document with ID " + documentId);
            }
        } catch (DataStoreException exception) {
            throw new RevokeServiceException(
                    "Failed to retrieve credentials for revocation", exception);
        }

        int failureCount = 0;
        for (StoredCredential credential : credentials) {
            try {
                String uri = credential.getStatusListUri();
                int index = credential.getStatusListIndex();
                statusListClient.revokeCredential(index, uri);
                dataStore.deleteCredential(credential.getCredentialIdentifier());
            } catch (StatusListClientException exception) {
                failureCount++;
                getLogger()
                        .error(
                                "Failed to revoke credential with ID {} and document ID {}",
                                credential.getCredentialIdentifier(),
                                credential.getDocumentId(),
                                exception);
            } catch (DataStoreException exception) {
                getLogger()
                        .error(
                                "Failed to delete revoked credential with ID {} and document ID {}",
                                credential.getCredentialIdentifier(),
                                credential.getDocumentId(),
                                exception);
            }
        }

        int totalCount = credentials.size();
        int successCount = totalCount - failureCount;
        getLogger()
                .info(
                        "Revocation complete for document {}: {} succeeded, {} failed out of {} total",
                        documentId,
                        successCount,
                        failureCount,
                        totalCount);

        if (failureCount > 0) {
            throw new RevokeServiceException("One or more credentials could not be revoked");
        }
    }

    protected Logger getLogger() {
        return LOGGER;
    }
}
