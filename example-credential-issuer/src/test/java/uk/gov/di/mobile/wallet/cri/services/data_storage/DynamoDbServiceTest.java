package uk.gov.di.mobile.wallet.cri.services.data_storage;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
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
import uk.gov.di.mobile.wallet.cri.credential.StatusListClient;
import uk.gov.di.mobile.wallet.cri.credential.StoredCredential;
import uk.gov.di.mobile.wallet.cri.credential_offer.CachedCredentialOffer;

import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.Optional;
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

    private static final String CREDENTIAL_OFFER_TABLE = "test-cache-cri-table";
    private static final String CREDENTIAL_TABLE = "test-credential-store-table";

    private static final String PARTITION_KEY = "4a1b1b18-b495-45ac-b0ce-73848bd32b70";
    private static final String ITEM_ID = "cb2e831f-b2d9-4c7a-b42e-be5370ea4c77";
    private static final Long TIME_TO_LIVE = 12345L;
    private static final String WALLET_SUBJECT_ID =
            "urn:fdc:wallet.account.gov.uk:2024:DtPT8x-dp_73tnlY3KNTiCitziN9GEherD16bqxNt9i";
    private static final String NOTIFICATION_ID = "267b1335-fc0e-41cf-a2b1-16134bf62dc4";
    private static final String DOCUMENT_ID = "1234ABCD";
    private static final Optional<StatusListClient.StatusListInformation> STATUS_LIST_INFORMATION =
            Optional.of(
                    new StatusListClient.StatusListInformation(
                            0, "https://test-status-list.gov.uk/t/3B0F3BD087A7"));

    @Mock private DynamoDbEnhancedClient mockDynamoDbEnhancedClient;
    @Mock private DynamoDbTable<CachedCredentialOffer> mockCredentialOfferTable;
    @Mock private DynamoDbTable<StoredCredential> mockCredentialTable;

    @Mock private PageIterable<StoredCredential> mockPageIterable;
    @Mock private Page<StoredCredential> mockPage;
    @Mock private DynamoDbIndex<StoredCredential> mockIndex;
    private CachedCredentialOffer cachedCredentialOffer;
    private DynamoDbService dynamoDbService;
    private StoredCredential storedCredential;

    @BeforeEach
    void setUp() {
        when(mockDynamoDbEnhancedClient.table(
                        eq(CREDENTIAL_OFFER_TABLE),
                        ArgumentMatchers.<TableSchema<CachedCredentialOffer>>any()))
                .thenReturn(mockCredentialOfferTable);
        when(mockDynamoDbEnhancedClient.table(
                        eq(CREDENTIAL_TABLE),
                        ArgumentMatchers.<TableSchema<StoredCredential>>any()))
                .thenReturn(mockCredentialTable);
        cachedCredentialOffer =
                new CachedCredentialOffer(PARTITION_KEY, ITEM_ID, WALLET_SUBJECT_ID, TIME_TO_LIVE);
        storedCredential =
                StoredCredential.builder()
                        .credentialIdentifier(PARTITION_KEY)
                        .notificationId(NOTIFICATION_ID)
                        .walletSubjectId(WALLET_SUBJECT_ID)
                        .timeToLive(TIME_TO_LIVE)
                        .statusList(STATUS_LIST_INFORMATION)
                        .documentId(DOCUMENT_ID)
                        .build();

        dynamoDbService =
                new DynamoDbService(
                        mockDynamoDbEnhancedClient, CREDENTIAL_OFFER_TABLE, CREDENTIAL_TABLE);
    }

    @Test
    void Should_SaveCredentialOfferToCache() throws DataStoreException {
        dynamoDbService.saveCredentialOffer(cachedCredentialOffer);

        verify(mockCredentialOfferTable).putItem(cachedCredentialOffer);
    }

    @Test
    void Should_ThrowDataStoreException_When_ErrorHappensSavingCredentialOffer() {
        doThrow(new UnsupportedOperationException())
                .when(mockCredentialOfferTable)
                .putItem(any(CachedCredentialOffer.class));

        DataStoreException exception =
                assertThrows(
                        DataStoreException.class,
                        () -> dynamoDbService.saveCredentialOffer(cachedCredentialOffer));
        assertEquals("Error saving credential offer", exception.getMessage());
    }

    @Test
    void Should_ReturnCachedCredentialOffer() throws DataStoreException {
        when(mockCredentialOfferTable.getItem(any(Key.class))).thenReturn(cachedCredentialOffer);

        CachedCredentialOffer response = dynamoDbService.getCredentialOffer(PARTITION_KEY);

        assertEquals(cachedCredentialOffer, response);
        verify(mockCredentialOfferTable).getItem(any(Key.class));
    }

    @Test
    void Should_ThrowDataStoreException_On_ErrorGettingCredentialOffer() {
        doThrow(new UnsupportedOperationException())
                .when(mockCredentialOfferTable)
                .getItem(any(Key.class));

        DataStoreException exception =
                assertThrows(
                        DataStoreException.class,
                        () -> dynamoDbService.getCredentialOffer(PARTITION_KEY));
        assertEquals("Error fetching credential offer", exception.getMessage());
    }

    @Test
    void Should_ThrowDataStoreException_On_ErrorDeletingCredentialOffer() {
        doThrow(new UnsupportedOperationException())
                .when(mockCredentialOfferTable)
                .deleteItem(any(Key.class));

        DataStoreException exception =
                assertThrows(
                        DataStoreException.class,
                        () -> dynamoDbService.deleteCredentialOffer(PARTITION_KEY));
        assertEquals("Error deleting credential offer", exception.getMessage());
    }

    @Test
    void Should_SaveStoredCredential() throws DataStoreException {
        dynamoDbService.saveStoredCredential(storedCredential);

        verify(mockCredentialTable).putItem(storedCredential);
    }

    @Test
    void Should_ThrowDataStoreException_On_ErrorSavingStoredCredential() {
        doThrow(new UnsupportedOperationException())
                .when(mockCredentialTable)
                .putItem(any(StoredCredential.class));

        DataStoreException exception =
                assertThrows(
                        DataStoreException.class,
                        () -> dynamoDbService.saveStoredCredential(storedCredential));
        assertEquals("Failed to store credential", exception.getMessage());
    }

    @Test
    void Should_ReturnStoredCredential() throws DataStoreException {
        when(mockCredentialTable.getItem(any(Key.class))).thenReturn(storedCredential);

        StoredCredential response = dynamoDbService.getStoredCredential(PARTITION_KEY);

        assertEquals(storedCredential, response);
        verify(mockCredentialTable).getItem(any(Key.class));
    }

    @Test
    void Should_ThrowDataStoreException_On_ErrorGettingStoredCredential() {
        doThrow(new UnsupportedOperationException())
                .when(mockCredentialTable)
                .getItem(any(Key.class));

        DataStoreException exception =
                assertThrows(
                        DataStoreException.class,
                        () -> dynamoDbService.getStoredCredential(PARTITION_KEY));
        assertEquals("Error fetching credential", exception.getMessage());
    }

    @Test
    void Should_ReturnCredentialsFetchedByDocumentId() throws DataStoreException {
        StoredCredential credential1 =
                StoredCredential.builder()
                        .credentialIdentifier("credentialIdentifier1")
                        .notificationId(NOTIFICATION_ID)
                        .walletSubjectId(WALLET_SUBJECT_ID)
                        .timeToLive(TIME_TO_LIVE)
                        .statusList(STATUS_LIST_INFORMATION)
                        .documentId(DOCUMENT_ID)
                        .build();

        StoredCredential credential2 =
                StoredCredential.builder()
                        .credentialIdentifier("credentialIdentifier2")
                        .notificationId(NOTIFICATION_ID)
                        .walletSubjectId(WALLET_SUBJECT_ID)
                        .timeToLive(TIME_TO_LIVE)
                        .statusList(STATUS_LIST_INFORMATION)
                        .documentId(DOCUMENT_ID)
                        .build();

        when(mockPage.items()).thenReturn(Arrays.asList(credential1, credential2));
        when(mockPageIterable.stream()).thenReturn(Stream.of(mockPage));
        when(mockCredentialTable.index("documentIdIndex")).thenReturn(mockIndex);
        when(mockIndex.query(any(QueryEnhancedRequest.class))).thenReturn(mockPageIterable);

        List<StoredCredential> result = dynamoDbService.getCredentialsByDocumentId(DOCUMENT_ID);

        assertEquals(2, result.size());
        assertEquals("credentialIdentifier1", result.get(0).getCredentialIdentifier());
        assertEquals("credentialIdentifier2", result.get(1).getCredentialIdentifier());
        verify(mockCredentialTable).index("documentIdIndex");
        verify(mockIndex).query(any(QueryEnhancedRequest.class));
    }

    @Test
    void Should_ReturnEmptyList_When_NoCredentialsFound() throws DataStoreException {
        when(mockPage.items()).thenReturn(Collections.emptyList());
        when(mockPageIterable.stream()).thenReturn(Stream.of(mockPage));
        when(mockCredentialTable.index("documentIdIndex")).thenReturn(mockIndex);
        when(mockIndex.query(any(QueryEnhancedRequest.class))).thenReturn(mockPageIterable);

        List<StoredCredential> result = dynamoDbService.getCredentialsByDocumentId(DOCUMENT_ID);

        assertNotNull(result);
        assertTrue(result.isEmpty());
    }

    @Test
    void Should_ThrowDataStoreException_When_QueryFails() {
        when(mockCredentialTable.index("documentIdIndex")).thenReturn(mockIndex);
        when(mockIndex.query(any(QueryEnhancedRequest.class)))
                .thenThrow(new RuntimeException("Some DynamoDB error"));

        DataStoreException exception =
                assertThrows(
                        DataStoreException.class,
                        () -> dynamoDbService.getCredentialsByDocumentId(DOCUMENT_ID));

        assertEquals("Error fetching credentials by documentId", exception.getMessage());
        assertEquals("Some DynamoDB error", exception.getCause().getMessage());
    }
}
