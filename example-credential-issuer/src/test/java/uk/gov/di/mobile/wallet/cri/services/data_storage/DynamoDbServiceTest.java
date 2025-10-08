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
import uk.gov.di.mobile.wallet.cri.models.CachedCredentialOffer;
import uk.gov.di.mobile.wallet.cri.models.StoredCredential;

import java.time.Instant;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class DynamoDbServiceTest {

    private static final String PARTITION_KEY = "4a1b1b18-b495-45ac-b0ce-73848bd32b70";
    private static final String ITEM_ID = "cb2e831f-b2d9-4c7a-b42e-be5370ea4c77";
    private static final Long TTL = Instant.now().plusSeconds(300).getEpochSecond();
    private static final String WALLET_SUBJECT_ID =
            "urn:fdc:wallet.account.gov.uk:2024:DtPT8x-dp_73tnlY3KNTiCitziN9GEherD16bqxNt9i";
    private static final String NOTIFICATION_ID = "267b1335-fc0e-41cf-a2b1-16134bf62dc4";
    private static final String DOCUMENT_ID = "1234ABCD";

    private static final String CREDENTIAL_OFFER_TABLE = "test-cache-cri-table";
    private static final String CREDENTIAL_TABLE = "test-credential-store-table";

    @Mock private DynamoDbEnhancedClient mockDynamoDbEnhancedClient;
    @Mock private DynamoDbTable<CachedCredentialOffer> mockCredentialOfferTable;
    @Mock private DynamoDbTable<StoredCredential> mockCredentialTable;
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
                new CachedCredentialOffer(PARTITION_KEY, ITEM_ID, WALLET_SUBJECT_ID, TTL);
        storedCredential =
                new StoredCredential(
                        PARTITION_KEY, NOTIFICATION_ID, WALLET_SUBJECT_ID, TTL, DOCUMENT_ID);

        dynamoDbService =
                new DynamoDbService(
                        mockDynamoDbEnhancedClient, CREDENTIAL_OFFER_TABLE, CREDENTIAL_TABLE);
    }

    @Test
    void Should_SaveCredentialOfferToCache() throws DataStoreException {
        dynamoDbService.saveCredentialOffer(cachedCredentialOffer);

        ArgumentCaptor<CachedCredentialOffer> credentialOfferCacheItemArgumentCaptor =
                ArgumentCaptor.forClass(CachedCredentialOffer.class);
        verify(mockCredentialOfferTable).putItem(credentialOfferCacheItemArgumentCaptor.capture());
        assertEquals(
                PARTITION_KEY,
                credentialOfferCacheItemArgumentCaptor.getValue().getCredentialIdentifier());
        assertEquals(ITEM_ID, credentialOfferCacheItemArgumentCaptor.getValue().getItemId());
        assertEquals(
                WALLET_SUBJECT_ID,
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
                .when(mockCredentialOfferTable)
                .putItem(argumentCaptor.capture());
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

        ArgumentCaptor<Key> argumentCaptor = ArgumentCaptor.forClass(Key.class);
        verify(mockCredentialOfferTable).getItem(argumentCaptor.capture());
        assertEquals(PARTITION_KEY, argumentCaptor.getValue().partitionKeyValue().s());
        assertEquals(cachedCredentialOffer, response);
    }

    @Test
    void Should_ThrowDataStoreException_When_ErrorHappensGettingCredentialOffer()
            throws DataStoreException {
        dynamoDbService.getCredentialOffer(PARTITION_KEY);

        ArgumentCaptor<Key> argumentCaptor = ArgumentCaptor.forClass(Key.class);
        doThrow(new UnsupportedOperationException())
                .when(mockCredentialOfferTable)
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
                .when(mockCredentialOfferTable)
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
        verify(mockCredentialTable).putItem(storedCredentialArgumentCaptor.capture());
        assertEquals(
                PARTITION_KEY, storedCredentialArgumentCaptor.getValue().getCredentialIdentifier());
        assertEquals(
                NOTIFICATION_ID, storedCredentialArgumentCaptor.getValue().getNotificationId());
        assertEquals(
                WALLET_SUBJECT_ID, storedCredentialArgumentCaptor.getValue().getWalletSubjectId());
        assertEquals(TTL, storedCredentialArgumentCaptor.getValue().getTimeToLive());
        assertEquals(DOCUMENT_ID, storedCredentialArgumentCaptor.getValue().getDocumentId());
    }

    @Test
    void Should_ThrowDataStoreException_When_ErrorHappensSavingStoredCredential()
            throws DataStoreException {
        dynamoDbService.saveStoredCredential(storedCredential);

        ArgumentCaptor<StoredCredential> argumentCaptor =
                ArgumentCaptor.forClass(StoredCredential.class);
        doThrow(new UnsupportedOperationException())
                .when(mockCredentialTable)
                .putItem(argumentCaptor.capture());
        DataStoreException exception =
                assertThrows(
                        DataStoreException.class,
                        () -> dynamoDbService.saveStoredCredential(storedCredential));
        assertEquals("Failed to store credential in DynamoDB", exception.getMessage());
    }

    @Test
    void Should_ReturnStoredCredential() throws DataStoreException {
        when(mockCredentialTable.getItem(any(Key.class))).thenReturn(storedCredential);

        StoredCredential response = dynamoDbService.getStoredCredential(PARTITION_KEY);

        ArgumentCaptor<Key> argumentCaptor = ArgumentCaptor.forClass(Key.class);
        verify(mockCredentialTable).getItem(argumentCaptor.capture());
        assertEquals(PARTITION_KEY, argumentCaptor.getValue().partitionKeyValue().s());
        assertEquals(storedCredential, response);
    }

    @Test
    void Should_ThrowDataStoreException_When_ErrorHappensGettingStoredCredential()
            throws DataStoreException {
        dynamoDbService.getStoredCredential(PARTITION_KEY);

        ArgumentCaptor<Key> argumentCaptor = ArgumentCaptor.forClass(Key.class);
        doThrow(new UnsupportedOperationException())
                .when(mockCredentialTable)
                .getItem(argumentCaptor.capture());
        DataStoreException exception =
                assertThrows(
                        DataStoreException.class,
                        () -> dynamoDbService.getStoredCredential(PARTITION_KEY));
        assertEquals("Error fetching credential", exception.getMessage());
    }
}
