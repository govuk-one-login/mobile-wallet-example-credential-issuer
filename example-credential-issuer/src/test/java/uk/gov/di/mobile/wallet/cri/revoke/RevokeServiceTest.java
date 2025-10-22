package uk.gov.di.mobile.wallet.cri.revoke;

import io.dropwizard.testing.junit5.DropwizardExtensionsSupport;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.slf4j.Logger;
import uk.gov.di.mobile.wallet.cri.credential.StatusListClient;
import uk.gov.di.mobile.wallet.cri.credential.StatusListClientException;
import uk.gov.di.mobile.wallet.cri.models.StoredCredential;
import uk.gov.di.mobile.wallet.cri.services.data_storage.DataStore;
import uk.gov.di.mobile.wallet.cri.services.data_storage.DataStoreException;

import java.util.List;

import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(DropwizardExtensionsSupport.class)
@ExtendWith(MockitoExtension.class)
class RevokeServiceTest {

    private static final String DOCUMENT_ID = "EDWAR515163SE5RO";
    private static final String CREDENTIAL_ID = "bf125350-a084-47a4-8cd0-c35f3fea8e87";
    private static final int STATUS_LIST_INDEX = 5;
    private static final String STATUS_LIST_URI = "https://status-list.test.com/t/12345";

    @Mock private DataStore mockDataStore;
    @Mock private StatusListClient mockStatusListClient;
    @Mock private Logger mockLogger;

    private RevokeService revokeService;

    @BeforeEach
    void setUp() {
        revokeService =
                new RevokeService(mockDataStore, mockStatusListClient) {
                    @Override
                    protected Logger getLogger() {
                        return mockLogger;
                    }
                };
    }

    @Test
    void shouldThrowRevokeServiceExceptionWhenDataStoreFailsToGetCredentials()
            throws DataStoreException {
        when(mockDataStore.getCredentialsByDocumentId(DOCUMENT_ID))
                .thenThrow(new DataStoreException("Database error"));

        RevokeServiceException exception =
                assertThrows(
                        RevokeServiceException.class,
                        () -> revokeService.revokeCredentials(DOCUMENT_ID));

        assertEquals("Failed to retrieve credentials for revocation", exception.getMessage());
        assertEquals("Database error", exception.getCause().getMessage());
        verify(mockDataStore, times(1)).getCredentialsByDocumentId(DOCUMENT_ID);
    }

    @Test
    void shouldThrowCredentialNotFoundExceptionWhenNoCredentialFound() throws DataStoreException {
        when(mockDataStore.getCredentialsByDocumentId(DOCUMENT_ID)).thenReturn(List.of());

        CredentialNotFoundException exception =
                assertThrows(
                        CredentialNotFoundException.class,
                        () -> revokeService.revokeCredentials(DOCUMENT_ID));

        assertEquals(
                "No credential found for document with ID EDWAR515163SE5RO",
                exception.getMessage());
        verify(mockDataStore, times(1)).getCredentialsByDocumentId(DOCUMENT_ID);
    }

    @Test
    void shouldContinueRevokingCredentialsWhenOneFailsToRevoke()
            throws DataStoreException, StatusListClientException {
        StoredCredential credential1 = createStoredCredential("credentialId1", 1, "uri1");
        StoredCredential credential2 = createStoredCredential("credentialId2", 2, "uri2");
        when(mockDataStore.getCredentialsByDocumentId(DOCUMENT_ID))
                .thenReturn(List.of(credential1, credential2));
        StatusListClientException statusListClientException =
                new StatusListClientException("Failed");
        doThrow(statusListClientException).when(mockStatusListClient).revokeCredential(1, "uri1");

        assertThrows(
                RevokeServiceException.class, () -> revokeService.revokeCredentials(DOCUMENT_ID));

        verify(mockStatusListClient, times(1)).revokeCredential(1, "uri1");
        verify(mockLogger)
                .error(
                        "Failed to revoke credential with ID {} and document ID {}",
                        "credentialId1",
                        "EDWAR515163SE5RO",
                        statusListClientException);
        verify(mockDataStore, never()).deleteCredential("credentialId1");
        verify(mockStatusListClient, times(1)).revokeCredential(2, "uri2");
        verify(mockDataStore, times(1)).deleteCredential("credentialId2");
    }

    @Test
    void shouldThrowRevokeServiceExceptionWhenAnyCredentialFailsToRevoke()
            throws DataStoreException, StatusListClientException {
        StoredCredential credential1 = createStoredCredential("credentialId1", 1, "uri1");
        when(mockDataStore.getCredentialsByDocumentId(DOCUMENT_ID))
                .thenReturn(List.of(credential1));
        doThrow(new StatusListClientException("Failed"))
                .when(mockStatusListClient)
                .revokeCredential(1, "uri1");

        RevokeServiceException exception =
                assertThrows(
                        RevokeServiceException.class,
                        () -> revokeService.revokeCredentials(DOCUMENT_ID));

        assertEquals("One or more credentials could not be revoked", exception.getMessage());
        verify(mockLogger)
                .info(
                        "Revocation complete for document {}: {} succeeded, {} failed out of {} total",
                        "EDWAR515163SE5RO",
                        0,
                        1,
                        1);
    }

    @Test
    void shouldContinueRevokingCredentialsWhenOneFailsToDelete()
            throws DataStoreException, StatusListClientException {
        StoredCredential credential1 = createStoredCredential("credentialId1", 1, "uri1");
        StoredCredential credential2 = createStoredCredential("credentialId2", 2, "uri2");
        when(mockDataStore.getCredentialsByDocumentId(DOCUMENT_ID))
                .thenReturn(List.of(credential1, credential2));
        DataStoreException dataStoreException = new DataStoreException("Delete failed");
        doThrow(dataStoreException).when(mockDataStore).deleteCredential("credentialId1");

        assertDoesNotThrow(() -> revokeService.revokeCredentials(DOCUMENT_ID));

        verify(mockStatusListClient, times(1)).revokeCredential(1, "uri1");
        verify(mockDataStore, times(1)).deleteCredential("credentialId1");
        verify(mockLogger)
                .error(
                        "Failed to delete revoked credential with ID {} and document ID {}",
                        "credentialId1",
                        "EDWAR515163SE5RO",
                        dataStoreException);
        verify(mockStatusListClient, times(1)).revokeCredential(2, "uri2");
        verify(mockDataStore, times(1)).deleteCredential("credentialId2");
    }

    @Test
    void shouldNotThrowExceptionWhenDataStoreFailsToDeleteRevokedCredential()
            throws DataStoreException {
        StoredCredential credential = createStoredCredential();
        when(mockDataStore.getCredentialsByDocumentId(DOCUMENT_ID)).thenReturn(List.of(credential));
        DataStoreException dataStoreException = new DataStoreException("Delete failed");
        doThrow(dataStoreException).when(mockDataStore).deleteCredential(CREDENTIAL_ID);

        assertDoesNotThrow(() -> revokeService.revokeCredentials(DOCUMENT_ID));

        verify(mockLogger)
                .info(
                        "Revocation complete for document {}: {} succeeded, {} failed out of {} total",
                        "EDWAR515163SE5RO",
                        1,
                        0,
                        1);
    }

    @Test
    void shouldRevokeCredentialSuccessfully_OneCredentialFound()
            throws DataStoreException, StatusListClientException {
        StoredCredential credential = createStoredCredential();
        when(mockDataStore.getCredentialsByDocumentId(DOCUMENT_ID)).thenReturn(List.of(credential));

        assertDoesNotThrow(() -> revokeService.revokeCredentials(DOCUMENT_ID));

        verify(mockDataStore, times(1)).getCredentialsByDocumentId(DOCUMENT_ID);
        verify(mockStatusListClient, times(1)).revokeCredential(STATUS_LIST_INDEX, STATUS_LIST_URI);
        verify(mockDataStore, times(1)).deleteCredential(CREDENTIAL_ID);
        verify(mockLogger)
                .info(
                        "Revocation complete for document {}: {} succeeded, {} failed out of {} total",
                        "EDWAR515163SE5RO",
                        1,
                        0,
                        1);
    }

    @Test
    void shouldRevokeMultipleCredentialsSuccessfully_MultipleCredentialFound()
            throws DataStoreException, StatusListClientException {
        StoredCredential credential1 = createStoredCredential("credentialId1", 1, "uri1");
        StoredCredential credential2 = createStoredCredential("credentialId2", 2, "uri2");
        when(mockDataStore.getCredentialsByDocumentId(DOCUMENT_ID))
                .thenReturn(List.of(credential1, credential2));

        assertDoesNotThrow(() -> revokeService.revokeCredentials(DOCUMENT_ID));

        verify(mockDataStore, times(1)).getCredentialsByDocumentId(DOCUMENT_ID);
        verify(mockStatusListClient, times(1)).revokeCredential(1, "uri1");
        verify(mockStatusListClient, times(1)).revokeCredential(2, "uri2");
        verify(mockDataStore, times(1)).deleteCredential("credentialId1");
        verify(mockDataStore, times(1)).deleteCredential("credentialId2");
        verify(mockLogger)
                .info(
                        "Revocation complete for document {}: {} succeeded, {} failed out of {} total",
                        "EDWAR515163SE5RO",
                        2,
                        0,
                        2);
    }

    private StoredCredential createStoredCredential() {
        return createStoredCredential(CREDENTIAL_ID, STATUS_LIST_INDEX, STATUS_LIST_URI);
    }

    private StoredCredential createStoredCredential(String credentialId, int index, String uri) {
        StoredCredential credential = new StoredCredential();
        credential.setCredentialIdentifier(credentialId);
        credential.setDocumentId(DOCUMENT_ID);
        credential.setStatusListUri(uri);
        credential.setStatusListIndex(index);
        return credential;
    }
}
