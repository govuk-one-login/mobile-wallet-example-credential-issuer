package uk.gov.di.mobile.wallet.cri.helpers;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;

public class CredentialOfferCacheItemTest {

    CredentialOfferCacheItem credentialOfferCacheItem;
    private static final String credentialIdentifier = "test-credential-identifier";
    private static final String documentId = "test-document-id";
    private static final String walletSubjectId = "test-wallet-subject-id";

    @BeforeEach
    void setUp() {
        credentialOfferCacheItem =
                new CredentialOfferCacheItem(credentialIdentifier, documentId, walletSubjectId);
    }

    @Test
    @DisplayName("Creates a credential offer cache item")
    public void testCredentialOfferCacheItem() {
        assertEquals("test-credential-identifier", credentialOfferCacheItem.credentialIdentifier);
        assertEquals("test-document-id", credentialOfferCacheItem.documentId);
        assertEquals("test-wallet-subject-id", credentialOfferCacheItem.walletSubjectId);
    }
}
