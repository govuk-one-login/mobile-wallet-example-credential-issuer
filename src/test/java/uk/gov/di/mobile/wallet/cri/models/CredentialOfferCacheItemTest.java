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
                        "test-credential-identifier", "test-document-id", "test-wallet-subject-id");
    }

    @Test
    void shouldCreateCredentialOfferCacheItem() {
        assertEquals(
                "test-credential-identifier", credentialOfferCacheItem.getCredentialIdentifier());
        assertEquals("test-document-id", credentialOfferCacheItem.getDocumentId());
        assertEquals("test-wallet-subject-id", credentialOfferCacheItem.getWalletSubjectId());
    }
}
