package uk.gov.di.mobile.wallet.cri.revoke;

import io.dropwizard.testing.junit5.DropwizardExtensionsSupport;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import uk.gov.di.mobile.wallet.cri.models.StoredCredential;
import uk.gov.di.mobile.wallet.cri.services.data_storage.DataStoreException;
import uk.gov.di.mobile.wallet.cri.services.data_storage.DynamoDbService;

import java.util.List;

import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(DropwizardExtensionsSupport.class)
@ExtendWith(MockitoExtension.class)
class RevokeServiceTest {

    private static final String DRIVING_LICENCE_NUMBER = "EDWAR515163SE5RO";
    @Mock private DynamoDbService mockDynamoDbService;
    private RevokeService revokeService;

    @BeforeEach
    void setUp() {
        revokeService = new RevokeService(mockDynamoDbService) {};
    }

    @Test
    void Should_PropagateDataStoreException() throws DataStoreException {

        when(mockDynamoDbService.getCredentialsByDocumentId(DRIVING_LICENCE_NUMBER))
                .thenThrow(new DataStoreException("Some detabase error"));

        DataStoreException exception =
                assertThrows(
                        DataStoreException.class,
                        () -> revokeService.revokeCredential(DRIVING_LICENCE_NUMBER));
        assertEquals("Some detabase error", exception.getMessage());
        verify(mockDynamoDbService, times(1)).getCredentialsByDocumentId(DRIVING_LICENCE_NUMBER);
    }

    @Test
    void Should_ThrowCredentialNotFoundException_When_NoCredentialFound()
            throws DataStoreException {
        when(mockDynamoDbService.getCredentialsByDocumentId(DRIVING_LICENCE_NUMBER))
                .thenReturn(List.of());

        CredentialNotFoundException exception =
                assertThrows(
                        CredentialNotFoundException.class,
                        () -> revokeService.revokeCredential(DRIVING_LICENCE_NUMBER));
        assertEquals(
                "No credential found for document with ID EDWAR515163SE5RO",
                exception.getMessage());
        verify(mockDynamoDbService, times(1)).getCredentialsByDocumentId(DRIVING_LICENCE_NUMBER);
    }

    @Test
    void Should_NotThrow_When_CredentialsFound() throws DataStoreException {
        when(mockDynamoDbService.getCredentialsByDocumentId(DRIVING_LICENCE_NUMBER))
                .thenReturn(List.of(new StoredCredential()));

        assertDoesNotThrow(() -> revokeService.revokeCredential(DRIVING_LICENCE_NUMBER));
        verify(mockDynamoDbService, times(1)).getCredentialsByDocumentId(DRIVING_LICENCE_NUMBER);
    }
}
