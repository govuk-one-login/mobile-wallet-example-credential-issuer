package uk.gov.di.mobile.wallet.cri.services.data_storage;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.ArgumentMatchers;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import software.amazon.awssdk.enhanced.dynamodb.DynamoDbEnhancedClient;
import software.amazon.awssdk.enhanced.dynamodb.DynamoDbIndex;
import software.amazon.awssdk.enhanced.dynamodb.DynamoDbTable;
import software.amazon.awssdk.enhanced.dynamodb.Key;
import software.amazon.awssdk.enhanced.dynamodb.TableSchema;
import software.amazon.awssdk.enhanced.dynamodb.model.Page;
import software.amazon.awssdk.enhanced.dynamodb.model.PageIterable;
import software.amazon.awssdk.enhanced.dynamodb.model.QueryEnhancedRequest;
import uk.gov.di.mobile.wallet.cri.models.CachedCredentialOffer;
import uk.gov.di.mobile.wallet.cri.models.StoredCredential;

import java.time.Instant;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.stream.Stream;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class DynamoDbServiceTest {

    private static final String PARTITION_KEY = "4a1b1b18-b495-45ac-b0ce-73848bd32b70";
    private static final Long TTL = Instant.now().plusSeconds(300).getEpochSecond();
    private static final String DOCUMENT_PRIMARY_IDENTIFIER =
            "cb2e831f-b2d9-4c7a-b42e-be5370ea4c77";

    @Mock private DynamoDbEnhancedClient mockDynamoDbEnhancedClient;
    @Mock private DynamoDbTable<CachedCredentialOffer> mockCachedCredentialOfferTable;
    @Mock private DynamoDbTable<StoredCredential> mockStoredCredentialTable;
    @Mock private PageIterable<StoredCredential> mockPageIterable;
    @Mock private Page<StoredCredential> mockPage;
    @Mock private DynamoDbIndex<StoredCredential> mockIndex;

    private CachedCredentialOffer cachedCredentialOffer;
    private DynamoDbService dynamoDbService;
    private StoredCredential storedCredential;

    @BeforeEach
    void setUp() {
        when(mockDynamoDbEnhancedClient.table(
                        eq("test-cache-cri-table"),
                        ArgumentMatchers.<TableSchema<CachedCredentialOffer>>any()))
                .thenReturn(mockCachedCredentialOfferTable);
        when(mockDynamoDbEnhancedClient.table(
                        eq("test-credential-store-table"),
                        ArgumentMatchers.<TableSchema<StoredCredential>>any()))
                .thenReturn(mockStoredCredentialTable);

        cachedCredentialOffer =
                new CachedCredentialOffer(
                        PARTITION_KEY,
                        "cb2e831f-b2d9-4c7a-b42e-be5370ea4c77",
                        "urn:fdc:wallet.account.gov.uk:2024:DtPT8x-dp_73tnlY3KNTiCitziN9GEherD16bqxNt9i",
                        TTL);

        storedCredential =
                new StoredCredential(
                        "4a1b1b18-b495-45ac-b0ce-73848bd32b70",
                        "267b1335-fc0e-41cf-a2b1-16134bf62dc4",
                        "urn:fdc:wallet.account.gov.uk:2024:DtPT8x-dp_73tnlY3KNTiCitziN9GEherD16bqxNt9i",
                        43200L,
                        DOCUMENT_PRIMARY_IDENTIFIER);
        dynamoDbService =
                new DynamoDbService(
                        mockDynamoDbEnhancedClient,
                        "test-cache-cri-table",
                        "test-credential-store-table");
    }

    @Test
    void Should_SaveCredentialOfferToCache() throws DataStoreException {
        dynamoDbService.saveCredentialOffer(cachedCredentialOffer);

        ArgumentCaptor<CachedCredentialOffer> credentialOfferCacheItemArgumentCaptor =
                ArgumentCaptor.forClass(CachedCredentialOffer.class);
        verify(mockCachedCredentialOfferTable)
                .putItem(credentialOfferCacheItemArgumentCaptor.capture());
        assertEquals(
                PARTITION_KEY,
                credentialOfferCacheItemArgumentCaptor.getValue().getCredentialIdentifier());
        assertEquals(
                "cb2e831f-b2d9-4c7a-b42e-be5370ea4c77",
                credentialOfferCacheItemArgumentCaptor.getValue().getDocumentId());
        assertEquals(
                "urn:fdc:wallet.account.gov.uk:2024:DtPT8x-dp_73tnlY3KNTiCitziN9GEherD16bqxNt9i",
                credentialOfferCacheItemArgumentCaptor.getValue().getWalletSubjectId());
        assertEquals(TTL, credentialOfferCacheItemArgumentCaptor.getValue().getTimeToLive());
    }

    @Test
    void Should_ThrowDataStoreException_When_ErrorHappensSavingCredentialOffer()
            throws DataStoreException {
        dynamoDbService.saveCredentialOffer(cachedCredentialOffer);

        ArgumentCaptor<CachedCredentialOffer> argumentCaptor =
                ArgumentCaptor.forClass(CachedCredentialOffer.class);
        doThrow(new UnsupportedOperationException())
                .when(mockCachedCredentialOfferTable)
                .putItem(argumentCaptor.capture());
        DataStoreException exception =
                assertThrows(
                        DataStoreException.class,
                        () -> dynamoDbService.saveCredentialOffer(cachedCredentialOffer));
        assertEquals("Error saving credential offer", exception.getMessage());
    }

    @Test
    void Should_ReturnCachedCredentialOffer() throws DataStoreException {
        when(mockCachedCredentialOfferTable.getItem(any(Key.class)))
                .thenReturn(cachedCredentialOffer);

        CachedCredentialOffer response = dynamoDbService.getCredentialOffer(PARTITION_KEY);

        ArgumentCaptor<Key> argumentCaptor = ArgumentCaptor.forClass(Key.class);
        verify(mockCachedCredentialOfferTable).getItem(argumentCaptor.capture());
        assertEquals(PARTITION_KEY, argumentCaptor.getValue().partitionKeyValue().s());
        assertEquals(cachedCredentialOffer, response);
    }

    @Test
    void Should_ThrowDataStoreException_When_ErrorHappensGettingCredentialOffer()
            throws DataStoreException {
        dynamoDbService.getCredentialOffer(PARTITION_KEY);

        ArgumentCaptor<Key> argumentCaptor = ArgumentCaptor.forClass(Key.class);
        doThrow(new UnsupportedOperationException())
                .when(mockCachedCredentialOfferTable)
                .getItem(argumentCaptor.capture());
        DataStoreException exception =
                assertThrows(
                        DataStoreException.class,
                        () -> dynamoDbService.getCredentialOffer(PARTITION_KEY));
        assertEquals("Error fetching credential offer", exception.getMessage());
    }

    @Test
    void Should_ThrowDataStoreException_When_ErrorHappensDeletingCredentialOffer()
            throws DataStoreException {
        dynamoDbService.deleteCredentialOffer(PARTITION_KEY);

        ArgumentCaptor<Key> argumentCaptor = ArgumentCaptor.forClass(Key.class);
        doThrow(new UnsupportedOperationException())
                .when(mockCachedCredentialOfferTable)
                .deleteItem(argumentCaptor.capture());
        DataStoreException exception =
                assertThrows(
                        DataStoreException.class,
                        () -> dynamoDbService.deleteCredentialOffer(PARTITION_KEY));
        assertEquals("Error deleting credential offer", exception.getMessage());
    }

    @Test
    void Should_SaveStoredCredential() throws DataStoreException {
        dynamoDbService.saveStoredCredential(storedCredential);

        ArgumentCaptor<StoredCredential> storedCredentialArgumentCaptor =
                ArgumentCaptor.forClass(StoredCredential.class);
        verify(mockStoredCredentialTable).putItem(storedCredentialArgumentCaptor.capture());
        assertEquals(
                PARTITION_KEY, storedCredentialArgumentCaptor.getValue().getCredentialIdentifier());
        assertEquals(
                DOCUMENT_PRIMARY_IDENTIFIER,
                storedCredentialArgumentCaptor.getValue().getDocumentPrimaryIdentifier());
        assertEquals(
                "267b1335-fc0e-41cf-a2b1-16134bf62dc4",
                storedCredentialArgumentCaptor.getValue().getNotificationId());
        assertEquals(43200L, storedCredentialArgumentCaptor.getValue().getTimeToLive());
    }

    @Test
    void Should_ThrowDataStoreException_When_ErrorHappensSavingStoredCredential()
            throws DataStoreException {
        dynamoDbService.saveStoredCredential(storedCredential);

        ArgumentCaptor<StoredCredential> argumentCaptor =
                ArgumentCaptor.forClass(StoredCredential.class);
        doThrow(new UnsupportedOperationException())
                .when(mockStoredCredentialTable)
                .putItem(argumentCaptor.capture());
        DataStoreException exception =
                assertThrows(
                        DataStoreException.class,
                        () -> dynamoDbService.saveStoredCredential(storedCredential));
        assertEquals("Failed to store credential in DynamoDB", exception.getMessage());
    }

    @Test
    void Should_ReturnStoredCredential() throws DataStoreException {
        when(mockStoredCredentialTable.getItem(any(Key.class))).thenReturn(storedCredential);

        StoredCredential response = dynamoDbService.getStoredCredential(PARTITION_KEY);

        ArgumentCaptor<Key> argumentCaptor = ArgumentCaptor.forClass(Key.class);
        verify(mockStoredCredentialTable).getItem(argumentCaptor.capture());
        assertEquals(PARTITION_KEY, argumentCaptor.getValue().partitionKeyValue().s());
        assertEquals(storedCredential, response);
    }

    @Test
    void Should_ThrowDataStoreException_When_ErrorHappensGettingStoredCredential()
            throws DataStoreException {
        dynamoDbService.getStoredCredential(PARTITION_KEY);

        ArgumentCaptor<Key> argumentCaptor = ArgumentCaptor.forClass(Key.class);
        doThrow(new UnsupportedOperationException())
                .when(mockStoredCredentialTable)
                .getItem(argumentCaptor.capture());
        DataStoreException exception =
                assertThrows(
                        DataStoreException.class,
                        () -> dynamoDbService.getStoredCredential(PARTITION_KEY));
        assertEquals("Error fetching credential", exception.getMessage());
    }

    @Test
    void Should_ReturnCredentialsFetchedByDocumentPrimaryIdentifier() throws DataStoreException {
        StoredCredential credential1 =
                new StoredCredential(
                        "credentialIdentifier1",
                        "notificationId1",
                        "walletSubjectId1",
                        43200L,
                        DOCUMENT_PRIMARY_IDENTIFIER);
        StoredCredential credential2 =
                new StoredCredential(
                        "credentialIdentifier2",
                        "notificationId2",
                        "walletSubjectId2",
                        43200L,
                        DOCUMENT_PRIMARY_IDENTIFIER);

        when(mockPage.items()).thenReturn(Arrays.asList(credential1, credential2));
        when(mockPageIterable.stream()).thenReturn(Stream.of(mockPage));
        when(mockStoredCredentialTable.index("documentPrimaryIdentifierIndex"))
                .thenReturn(mockIndex);
        when(mockIndex.query(any(QueryEnhancedRequest.class))).thenReturn(mockPageIterable);

        List<StoredCredential> result =
                dynamoDbService.getCredentialsByDocumentPrimaryIdentifier(
                        DOCUMENT_PRIMARY_IDENTIFIER);

        assertEquals(2, result.size());
        assertEquals("credentialIdentifier1", result.get(0).getCredentialIdentifier());
        assertEquals("credentialIdentifier2", result.get(1).getCredentialIdentifier());
        verify(mockStoredCredentialTable).index("documentPrimaryIdentifierIndex");
        verify(mockIndex).query(any(QueryEnhancedRequest.class));
    }

    @Test
    void Should_ReturnEmptyList_When_NoCredentialsFound() throws DataStoreException {
        when(mockPage.items()).thenReturn(Collections.emptyList());
        when(mockPageIterable.stream()).thenReturn(Stream.of(mockPage));
        when(mockStoredCredentialTable.index("documentPrimaryIdentifierIndex"))
                .thenReturn(mockIndex);
        when(mockIndex.query(any(QueryEnhancedRequest.class))).thenReturn(mockPageIterable);

        List<StoredCredential> result =
                dynamoDbService.getCredentialsByDocumentPrimaryIdentifier(
                        DOCUMENT_PRIMARY_IDENTIFIER);

        assertNotNull(result);
        assertTrue(result.isEmpty());
    }

    @Test
    void Should_ThrowDataStoreException_When_QueryFails() {
        when(mockStoredCredentialTable.index("documentPrimaryIdentifierIndex"))
                .thenReturn(mockIndex);
        when(mockIndex.query(any(QueryEnhancedRequest.class)))
                .thenThrow(new RuntimeException("Some DynamoDB error"));

        DataStoreException exception =
                assertThrows(
                        DataStoreException.class,
                        () ->
                                dynamoDbService.getCredentialsByDocumentPrimaryIdentifier(
                                        DOCUMENT_PRIMARY_IDENTIFIER));

        assertEquals(
                "Error fetching credentials by documentPrimaryIdentifier", exception.getMessage());
        assertEquals(exception.getCause().getMessage(), "Some DynamoDB error");
    }
}
