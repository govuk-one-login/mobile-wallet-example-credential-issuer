package uk.gov.di.mobile.wallet.cri.models;

import org.junit.jupiter.api.Test;

import java.time.Instant;

import static org.junit.jupiter.api.Assertions.assertEquals;

class CredentialOfferCacheItemBuilderTest {

    @Test
    void Should_BuildCredentialOfferCacheItem() {
        Long expiry = Instant.now().plusSeconds(300).getEpochSecond();
        Long ttl = Instant.now().plusSeconds(1000).getEpochSecond();

        CredentialOfferCacheItem credentialOfferCacheItem =
                new CredentialOfferCacheItemBuilder()
                        .credentialIdentifier("4a1b1b18-b495-45ac-b0ce-73848bd32b70")
                        .documentId("cb2e831f-b2d9-4c7a-b42e-be5370ea4c77")
                        .walletSubjectId(
                                "urn:fdc:wallet.account.gov.uk:2024:DtPT8x-dp_73tnlY3KNTiCitziN9GEherD16bqxNt9i")
                        .notificationId("267b1335-fc0e-41cf-a2b1-16134bf62dc4")
                        .expiry(expiry)
                        .redeemed(false)
                        .timeToLive(ttl)
                        .build();

        assertEquals(
                "4a1b1b18-b495-45ac-b0ce-73848bd32b70",
                credentialOfferCacheItem.getCredentialIdentifier());
        assertEquals(
                "cb2e831f-b2d9-4c7a-b42e-be5370ea4c77", credentialOfferCacheItem.getDocumentId());
        assertEquals(
                "urn:fdc:wallet.account.gov.uk:2024:DtPT8x-dp_73tnlY3KNTiCitziN9GEherD16bqxNt9i",
                credentialOfferCacheItem.getWalletSubjectId());
        assertEquals(
                "267b1335-fc0e-41cf-a2b1-16134bf62dc4",
                credentialOfferCacheItem.getNotificationId());
        assertEquals(false, credentialOfferCacheItem.getRedeemed());
        assertEquals(expiry, credentialOfferCacheItem.getExpiry());
        assertEquals(ttl, credentialOfferCacheItem.getTimeToLive());
    }
}
