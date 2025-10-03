package uk.gov.di.mobile.wallet.cri.services.data_storage;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentMatchers;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import software.amazon.awssdk.enhanced.dynamodb.DynamoDbEnhancedClient;
import software.amazon.awssdk.enhanced.dynamodb.DynamoDbTable;
import software.amazon.awssdk.enhanced.dynamodb.Key;
import software.amazon.awssdk.enhanced.dynamodb.TableSchema;
import uk.gov.di.mobile.wallet.cri.credential.StatusListClient;
import uk.gov.di.mobile.wallet.cri.models.CachedCredentialOffer;
import uk.gov.di.mobile.wallet.cri.models.StoredCredential;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class DynamoDbServiceTest {

    private static final String CREDENTIAL_OFFER_CACHE_TABLE_NAME = "credential-offer-cache";
    private static final String CREDENTIAL_STORE_TABLE_NAME = "credential-store";
    private static final String PARTITION_KEY = "efb52887-48d6-43b7-b14c-da7896fbf54d";
    private static final String WALLET_SUBJECT_ID =
            "urn:fdc:wallet.account.gov.uk:2024:DtPT8x-dp_73tnlY3KNTiCitziN9GEherD16bqxNt9i";
    private static final Long TIME_TO_LIVE = 12345L;
    private static final StatusListClient.IssueResponse STATUS_LIST_ISSUER_RESPONSE =
            new StatusListClient.IssueResponse(0, "https://test-status-list.gov.uk/t/3B0F3BD087A7");

    @Mock private DynamoDbEnhancedClient mockDynamoDbEnhancedClient;
    @Mock private DynamoDbTable<CachedCredentialOffer> mockCachedCredentialOfferTable;
    @Mock private DynamoDbTable<StoredCredential> mockStoredCredentialTable;

    private CachedCredentialOffer cachedCredentialOffer;
    private DynamoDbService dynamoDbService;
    private StoredCredential storedCredential;

    @BeforeEach
    void setUp() {
        when(mockDynamoDbEnhancedClient.table(
                        eq(CREDENTIAL_OFFER_CACHE_TABLE_NAME),
                        ArgumentMatchers.<TableSchema<CachedCredentialOffer>>any()))
                .thenReturn(mockCachedCredentialOfferTable);
        when(mockDynamoDbEnhancedClient.table(
                        eq(CREDENTIAL_STORE_TABLE_NAME),
                        ArgumentMatchers.<TableSchema<StoredCredential>>any()))
                .thenReturn(mockStoredCredentialTable);
        cachedCredentialOffer =
                new CachedCredentialOffer(
                        PARTITION_KEY,
                        "01606f33-3a9a-4a17-9c86-e1c3b968880a",
                        WALLET_SUBJECT_ID,
                        TIME_TO_LIVE);
        storedCredential =
                StoredCredential.builder()
                        .credentialIdentifier(PARTITION_KEY)
                        .notificationId("77368ca6-877b-4208-a397-99f1df890400")
                        .walletSubjectId(WALLET_SUBJECT_ID)
                        .timeToLive(TIME_TO_LIVE)
                        .statusList(STATUS_LIST_ISSUER_RESPONSE)
                        .documentPrimaryIdentifier("cb2e831f-b2d9-4c7a-b42e-be5370ea4c77")
                        .build();
        dynamoDbService =
                new DynamoDbService(
                        mockDynamoDbEnhancedClient,
                        CREDENTIAL_OFFER_CACHE_TABLE_NAME,
                        CREDENTIAL_STORE_TABLE_NAME);
    }

    @Test
    void Should_SaveCredentialOfferToCache() throws DataStoreException {
        dynamoDbService.saveCredentialOffer(cachedCredentialOffer);

        verify(mockCachedCredentialOfferTable).putItem(cachedCredentialOffer);
    }

    @Test
    void Should_ThrowDataStoreException_When_ErrorHappensSavingCredentialOffer() {
        doThrow(new UnsupportedOperationException())
                .when(mockCachedCredentialOfferTable)
                .putItem(any(CachedCredentialOffer.class));

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

        assertEquals(cachedCredentialOffer, response);
        verify(mockCachedCredentialOfferTable).getItem(any(Key.class));
    }

    @Test
    void Should_ThrowDataStoreException_On_ErrorGettingCredentialOffer() {
        doThrow(new UnsupportedOperationException())
                .when(mockCachedCredentialOfferTable)
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
                .when(mockCachedCredentialOfferTable)
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

        verify(mockStoredCredentialTable).putItem(storedCredential);
    }

    @Test
    void Should_ThrowDataStoreException_On_ErrorSavingStoredCredential() {
        doThrow(new UnsupportedOperationException())
                .when(mockStoredCredentialTable)
                .putItem(any(StoredCredential.class));

        DataStoreException exception =
                assertThrows(
                        DataStoreException.class,
                        () -> dynamoDbService.saveStoredCredential(storedCredential));
        assertEquals("Failed to store credential", exception.getMessage());
    }

    @Test
    void Should_ReturnStoredCredential() throws DataStoreException {
        when(mockStoredCredentialTable.getItem(any(Key.class))).thenReturn(storedCredential);

        StoredCredential response = dynamoDbService.getStoredCredential(PARTITION_KEY);

        assertEquals(storedCredential, response);
        verify(mockStoredCredentialTable).getItem(any(Key.class));
    }

    @Test
    void Should_ThrowDataStoreException_On_ErrorGettingStoredCredential() {
        doThrow(new UnsupportedOperationException())
                .when(mockStoredCredentialTable)
                .getItem(any(Key.class));

        DataStoreException exception =
                assertThrows(
                        DataStoreException.class,
                        () -> dynamoDbService.getStoredCredential(PARTITION_KEY));
        assertEquals("Error fetching credential", exception.getMessage());
    }
}
