package uk.gov.di.mobile.wallet.cri;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.ArgumentMatchers;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import software.amazon.awssdk.enhanced.dynamodb.DynamoDbEnhancedClient;
import software.amazon.awssdk.enhanced.dynamodb.DynamoDbTable;
import software.amazon.awssdk.enhanced.dynamodb.TableSchema;
import uk.gov.di.mobile.wallet.cri.helpers.CredentialOfferCacheItem;
import uk.gov.di.mobile.wallet.cri.services.DynamoDbService;

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
    private DynamoDbService<CredentialOfferCacheItem> dynamoDbService;

    @BeforeEach
    void setUp() {
        when(mockDynamoDbEnhancedClient.table(
                anyString(), ArgumentMatchers.<TableSchema<CredentialOfferCacheItem>>any()))
                .thenReturn(mockDynamoDbTable);

        credentialOfferCacheItem = new CredentialOfferCacheItem();

        dynamoDbService =
                new DynamoDbService<>(
                        mockDynamoDbEnhancedClient,
                        CredentialOfferCacheItem.class,
                        TEST_TABLE_NAME);
    }

    @Test
    void shouldSaveCredentialOfferItemTooDynamoDbTable() {
        dynamoDbService.putItem(credentialOfferCacheItem);

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
}
