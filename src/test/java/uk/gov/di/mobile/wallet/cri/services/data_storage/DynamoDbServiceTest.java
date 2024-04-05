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
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class DynamoDbServiceTest {

    private static final String TEST_TABLE_NAME = "test-cache-cri-table";

    @Mock private DynamoDbEnhancedClient mockDynamoDbEnhancedClient;
    @Mock private DynamoDbTable<CredentialOfferCacheItem> mockDynamoDbTable;

    private CredentialOfferCacheItem credentialOfferCacheItem;
    private DynamoDbService dynamoDbService;

    @BeforeEach
    void setUp() {
        when(mockDynamoDbEnhancedClient.table(
                        anyString(), ArgumentMatchers.<TableSchema<CredentialOfferCacheItem>>any()))
                .thenReturn(mockDynamoDbTable);

        credentialOfferCacheItem = new CredentialOfferCacheItem();

        dynamoDbService = new DynamoDbService(mockDynamoDbEnhancedClient, TEST_TABLE_NAME);
    }

    @Test
    void shouldSaveCredentialOfferToCache() throws DataStoreException {
        dynamoDbService.saveCredentialOffer(credentialOfferCacheItem);

        ArgumentCaptor<CredentialOfferCacheItem> credentialOfferCacheItemArgumentCaptor =
                ArgumentCaptor.forClass(CredentialOfferCacheItem.class);

        verify(mockDynamoDbEnhancedClient)
                .table(
                        eq(TEST_TABLE_NAME),
                        ArgumentMatchers.<TableSchema<CredentialOfferCacheItem>>any());
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
    void shouldGetCredentialOfferFromCacheThroughPartitionKey() throws DataStoreException {
        dynamoDbService.getCredentialOffer("test-partition-key-123");

        ArgumentCaptor<Key> keyCaptor = ArgumentCaptor.forClass(Key.class);

        verify(mockDynamoDbEnhancedClient)
                .table(
                        eq(TEST_TABLE_NAME),
                        ArgumentMatchers.<TableSchema<CredentialOfferCacheItem>>any());
        verify(mockDynamoDbTable).getItem(keyCaptor.capture());
        assertEquals("test-partition-key-123", keyCaptor.getValue().partitionKeyValue().s());
    }
}
