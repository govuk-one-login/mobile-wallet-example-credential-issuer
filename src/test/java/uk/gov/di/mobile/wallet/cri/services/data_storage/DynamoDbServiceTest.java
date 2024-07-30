package uk.gov.di.mobile.wallet.cri.services.data_storage;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.ArgumentMatchers;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import software.amazon.awssdk.enhanced.dynamodb.DynamoDbEnhancedClient;
import software.amazon.awssdk.enhanced.dynamodb.DynamoDbTable;
import software.amazon.awssdk.enhanced.dynamodb.Key;
import software.amazon.awssdk.enhanced.dynamodb.TableSchema;
import uk.gov.di.mobile.wallet.cri.models.CredentialOfferCacheItem;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class DynamoDbServiceTest {

    private static final String TEST_TABLE_NAME = "test-cache-cri-table";
    private static final String TEST_PARTITION_KEY = "test-credential_id";
    @Mock private DynamoDbEnhancedClient mockDynamoDbEnhancedClient;
    @Mock private DynamoDbTable<CredentialOfferCacheItem> mockDynamoDbTable;
    private CredentialOfferCacheItem credentialOfferCacheItem;
    private DynamoDbService dynamoDbService;

    @BeforeEach
    void setUp() {
        when(mockDynamoDbEnhancedClient.table(
                        anyString(), ArgumentMatchers.<TableSchema<CredentialOfferCacheItem>>any()))
                .thenReturn(mockDynamoDbTable);
        credentialOfferCacheItem =
                new CredentialOfferCacheItem(
                        TEST_PARTITION_KEY, "test-document-id", "test-wallet-subject-id", 100L);
        dynamoDbService = new DynamoDbService(mockDynamoDbEnhancedClient, TEST_TABLE_NAME);
    }

    @Test
    void shouldSaveCredentialOfferToCache() throws DataStoreException {
        dynamoDbService.saveCredentialOffer(credentialOfferCacheItem);
        ArgumentCaptor<CredentialOfferCacheItem> credentialOfferCacheItemArgumentCaptor =
                ArgumentCaptor.forClass(CredentialOfferCacheItem.class);

        verify(mockDynamoDbTable).putItem(credentialOfferCacheItemArgumentCaptor.capture());

        assertEquals(
                credentialOfferCacheItem.getCredentialIdentifier(),
                credentialOfferCacheItemArgumentCaptor.getValue().getCredentialIdentifier());
        assertEquals(
                credentialOfferCacheItem.getDocumentId(),
                credentialOfferCacheItemArgumentCaptor.getValue().getDocumentId());
        assertEquals(
                credentialOfferCacheItem.getWalletSubjectId(),
                credentialOfferCacheItemArgumentCaptor.getValue().getWalletSubjectId());
    }

    @Test
    void shouldThrowDataStoreExceptionWhenErrorHappensWhileTryingToSaveItem()
            throws DataStoreException {
        dynamoDbService.saveCredentialOffer(credentialOfferCacheItem);
        ArgumentCaptor<CredentialOfferCacheItem> credentialOfferCacheItemArgumentCaptor =
                ArgumentCaptor.forClass(CredentialOfferCacheItem.class);
        doThrow(new UnsupportedOperationException())
                .when(mockDynamoDbTable)
                .putItem(credentialOfferCacheItemArgumentCaptor.capture());

        DataStoreException exception =
                assertThrows(
                        DataStoreException.class,
                        () -> dynamoDbService.saveCredentialOffer(credentialOfferCacheItem));
        assertEquals("Error saving credential offer", exception.getMessage());
    }

    @Test
    void shouldGetCredentialOfferFromCacheThroughPartitionKey() throws DataStoreException {
        dynamoDbService.getCredentialOffer(TEST_PARTITION_KEY);
        ArgumentCaptor<Key> keyCaptor = ArgumentCaptor.forClass(Key.class);
        verify(mockDynamoDbTable).getItem(keyCaptor.capture());

        assertEquals(TEST_PARTITION_KEY, keyCaptor.getValue().partitionKeyValue().s());
    }

    @Test
    void shouldThrowDataStoreExceptionWhenErrorHappensWhileTryingToGetItem()
            throws DataStoreException {
        dynamoDbService.getCredentialOffer(TEST_PARTITION_KEY);
        ArgumentCaptor<Key> keyCaptor = ArgumentCaptor.forClass(Key.class);
        doThrow(new UnsupportedOperationException())
                .when(mockDynamoDbTable)
                .getItem(keyCaptor.capture());

        DataStoreException exception =
                assertThrows(
                        DataStoreException.class,
                        () -> dynamoDbService.getCredentialOffer(TEST_PARTITION_KEY));
        assertEquals("Error fetching credential offer", exception.getMessage());
    }

    @Test
    void shouldDeleteCredentialOfferFromCacheThroughPartitionKey() throws DataStoreException {
        dynamoDbService.deleteCredentialOffer(TEST_PARTITION_KEY);
        ArgumentCaptor<Key> keyCaptor = ArgumentCaptor.forClass(Key.class);
        verify(mockDynamoDbTable).deleteItem(keyCaptor.capture());

        assertEquals(TEST_PARTITION_KEY, keyCaptor.getValue().partitionKeyValue().s());
    }

    @Test
    void shouldThrowDataStoreExceptionWhenErrorHappensWhileTryingToDeleteItem()
            throws DataStoreException {
        dynamoDbService.deleteCredentialOffer(TEST_PARTITION_KEY);
        ArgumentCaptor<Key> keyCaptor = ArgumentCaptor.forClass(Key.class);
        doThrow(new UnsupportedOperationException())
                .when(mockDynamoDbTable)
                .deleteItem(keyCaptor.capture());

        DataStoreException exception =
                assertThrows(
                        DataStoreException.class,
                        () -> dynamoDbService.deleteCredentialOffer(TEST_PARTITION_KEY));
        assertEquals("Error deleting credential offer", exception.getMessage());
    }
}
