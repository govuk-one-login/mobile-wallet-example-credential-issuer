package uk.gov.di.mobile.wallet.cri.models;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;

class CredentialOfferCacheItemTest {

    private CredentialOfferCacheItem credentialOfferCacheItem;

    @BeforeEach
    void setUp() {
        credentialOfferCacheItem =
                new CredentialOfferCacheItem(
                        "test-credential-identifier",
                        "test-document-id",
                        "test-wallet-subject-id",
                        "test-notification-id",
                        900L);
    }

    @Test
    void should_Create_CredentialOffer_CacheItem() {
        assertEquals(
                "test-credential-identifier", credentialOfferCacheItem.getCredentialIdentifier());
        assertEquals("test-document-id", credentialOfferCacheItem.getDocumentId());
        assertEquals("test-wallet-subject-id", credentialOfferCacheItem.getWalletSubjectId());
        assertEquals("test-notification-id", credentialOfferCacheItem.getNotificationId());
        assertEquals(900, credentialOfferCacheItem.getTimeToLive());
    }
}
