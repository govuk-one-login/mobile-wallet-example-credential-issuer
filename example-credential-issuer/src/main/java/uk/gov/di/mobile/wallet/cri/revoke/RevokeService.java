package uk.gov.di.mobile.wallet.cri.revoke;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import uk.gov.di.mobile.wallet.cri.notification.NotificationService;
import uk.gov.di.mobile.wallet.cri.services.data_storage.DataStore;
import uk.gov.di.mobile.wallet.cri.services.data_storage.DataStoreException;

public class RevokeService {

    private final DataStore dataStore;

    private static final Logger LOGGER = LoggerFactory.getLogger(NotificationService.class);

    public RevokeService(DataStore dataStore) {
        this.dataStore = dataStore;
    }

    public void revokeCredential(String documentPrimaryIdentifier) throws DataStoreException {
        if (documentPrimaryIdentifier == null) {
            throw new DataStoreException("Document primary identifier is null");
        }

        dataStore.getCredentialsByDocumentPrimaryIdentifier(documentPrimaryIdentifier);
    }
}
