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

    public void revokeCredential(RevokeRequestBody revokeRequestBody) throws DataStoreException{
        if (revokeRequestBody == null) {
            throw new DataStoreException("Request body is null");
        }

        String documentPrimaryIdentifierValue = revokeRequestBody.getDrivingLicenceNumber();

        dataStore.getCredentialsByDocumentPrimaryIdentifier(documentPrimaryIdentifierValue);

    }
}
