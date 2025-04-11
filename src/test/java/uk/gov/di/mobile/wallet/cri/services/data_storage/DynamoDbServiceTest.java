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

import java.time.Instant;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class DynamoDbServiceTest {

    private static final String PARTITION_KEY = "4a1b1b18-b495-45ac-b0ce-73848bd32b70";
    private static final Long EXPIRY = Instant.now().plusSeconds(300).getEpochSecond();
    private static final Long TTL = Instant.now().plusSeconds(1000).getEpochSecond();

    @Mock private DynamoDbEnhancedClient mockDynamoDbEnhancedClient;
    @Mock private DynamoDbTable<CachedCredentialOffer> mockDynamoDbTable;
    private CachedCredentialOffer cachedCredentialOffer;
    private DynamoDbService dynamoDbService;

    @BeforeEach
    void setUp() {
        when(mockDynamoDbEnhancedClient.table(
                        anyString(), ArgumentMatchers.<TableSchema<CachedCredentialOffer>>any()))
                .thenReturn(mockDynamoDbTable);

        cachedCredentialOffer =
                new CachedCredentialOffer(
                        PARTITION_KEY,
                        "cb2e831f-b2d9-4c7a-b42e-be5370ea4c77",
                        "urn:fdc:wallet.account.gov.uk:2024:DtPT8x-dp_73tnlY3KNTiCitziN9GEherD16bqxNt9i",
                        "267b1335-fc0e-41cf-a2b1-16134bf62dc4",
                        false,
                        EXPIRY,
                        TTL);

        dynamoDbService = new DynamoDbService(mockDynamoDbEnhancedClient, "test-cache-cri-table");
    }

    @Test
    void Should_SaveCredentialOfferToCache() throws DataStoreException {
        dynamoDbService.saveCredentialOffer(cachedCredentialOffer);

        ArgumentCaptor<CachedCredentialOffer> credentialOfferCacheItemArgumentCaptor =
                ArgumentCaptor.forClass(CachedCredentialOffer.class);
        verify(mockDynamoDbTable).putItem(credentialOfferCacheItemArgumentCaptor.capture());
        assertEquals(
                PARTITION_KEY,
                credentialOfferCacheItemArgumentCaptor.getValue().getCredentialIdentifier());
        assertEquals(
                "cb2e831f-b2d9-4c7a-b42e-be5370ea4c77",
                credentialOfferCacheItemArgumentCaptor.getValue().getDocumentId());
        assertEquals(
                "urn:fdc:wallet.account.gov.uk:2024:DtPT8x-dp_73tnlY3KNTiCitziN9GEherD16bqxNt9i",
                credentialOfferCacheItemArgumentCaptor.getValue().getWalletSubjectId());
        assertEquals(
                "267b1335-fc0e-41cf-a2b1-16134bf62dc4",
                credentialOfferCacheItemArgumentCaptor.getValue().getNotificationId());
        assertEquals(false, credentialOfferCacheItemArgumentCaptor.getValue().getRedeemed());
        assertEquals(EXPIRY, credentialOfferCacheItemArgumentCaptor.getValue().getExpiry());
        assertEquals(TTL, credentialOfferCacheItemArgumentCaptor.getValue().getTimeToLive());
    }

    @Test
    void Should_ThrowDataStoreException_When_ErrorHappensSavingCredentialOffer()
            throws DataStoreException {
        dynamoDbService.saveCredentialOffer(cachedCredentialOffer);

        ArgumentCaptor<CachedCredentialOffer> argumentCaptor =
                ArgumentCaptor.forClass(CachedCredentialOffer.class);
        doThrow(new UnsupportedOperationException())
                .when(mockDynamoDbTable)
                .putItem(argumentCaptor.capture());
        DataStoreException exception =
                assertThrows(
                        DataStoreException.class,
                        () -> dynamoDbService.saveCredentialOffer(cachedCredentialOffer));
        assertEquals("Error saving credential offer", exception.getMessage());
    }

    @Test
    void Should_ReturnCachedCredentialOffer() throws DataStoreException {
        when(mockDynamoDbTable.getItem(any(Key.class))).thenReturn(cachedCredentialOffer);

        CachedCredentialOffer response = dynamoDbService.getCredentialOffer(PARTITION_KEY);

        ArgumentCaptor<Key> argumentCaptor = ArgumentCaptor.forClass(Key.class);
        verify(mockDynamoDbTable).getItem(argumentCaptor.capture());
        assertEquals(PARTITION_KEY, argumentCaptor.getValue().partitionKeyValue().s());
        assertEquals(cachedCredentialOffer, response);
    }

    @Test
    void Should_ThrowDataStoreException_When_ErrorHappensGettingCredentialOffer()
            throws DataStoreException {
        dynamoDbService.getCredentialOffer(PARTITION_KEY);

        ArgumentCaptor<Key> argumentCaptor = ArgumentCaptor.forClass(Key.class);
        doThrow(new UnsupportedOperationException())
                .when(mockDynamoDbTable)
                .getItem(argumentCaptor.capture());
        DataStoreException exception =
                assertThrows(
                        DataStoreException.class,
                        () -> dynamoDbService.getCredentialOffer(PARTITION_KEY));
        assertEquals("Error fetching credential offer", exception.getMessage());
    }

    @Test
    void Should_UpdateCachedCredentialOffer() throws DataStoreException {
        dynamoDbService.updateCredentialOffer(cachedCredentialOffer);

        ArgumentCaptor<CachedCredentialOffer> credentialOfferCacheItemArgumentCaptor =
                ArgumentCaptor.forClass(CachedCredentialOffer.class);
        verify(mockDynamoDbTable).updateItem(credentialOfferCacheItemArgumentCaptor.capture());
        assertEquals(
                PARTITION_KEY,
                credentialOfferCacheItemArgumentCaptor.getValue().getCredentialIdentifier());
        assertEquals(
                "cb2e831f-b2d9-4c7a-b42e-be5370ea4c77",
                credentialOfferCacheItemArgumentCaptor.getValue().getDocumentId());
        assertEquals(
                "urn:fdc:wallet.account.gov.uk:2024:DtPT8x-dp_73tnlY3KNTiCitziN9GEherD16bqxNt9i",
                credentialOfferCacheItemArgumentCaptor.getValue().getWalletSubjectId());
        assertEquals(
                "267b1335-fc0e-41cf-a2b1-16134bf62dc4",
                credentialOfferCacheItemArgumentCaptor.getValue().getNotificationId());
        assertEquals(false, credentialOfferCacheItemArgumentCaptor.getValue().getRedeemed());
        assertEquals(EXPIRY, credentialOfferCacheItemArgumentCaptor.getValue().getExpiry());
        assertEquals(TTL, credentialOfferCacheItemArgumentCaptor.getValue().getTimeToLive());
    }

    @Test
    void Should_ThrowDataStoreException_When_ErrorHappensUpdatingCredentialOffer()
            throws DataStoreException {
        dynamoDbService.updateCredentialOffer(cachedCredentialOffer);

        ArgumentCaptor<CachedCredentialOffer> argumentCaptor =
                ArgumentCaptor.forClass(CachedCredentialOffer.class);
        doThrow(new UnsupportedOperationException())
                .when(mockDynamoDbTable)
                .updateItem(argumentCaptor.capture());
        DataStoreException exception =
                assertThrows(
                        DataStoreException.class,
                        () -> dynamoDbService.updateCredentialOffer(cachedCredentialOffer));
        assertEquals("Error updating credential offer", exception.getMessage());
    }
}
