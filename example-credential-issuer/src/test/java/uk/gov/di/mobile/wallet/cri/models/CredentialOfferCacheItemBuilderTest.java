package uk.gov.di.mobile.wallet.cri.models;

import org.junit.jupiter.api.Test;

import java.time.Instant;

import static org.junit.jupiter.api.Assertions.assertEquals;

class CredentialOfferCacheItemBuilderTest {

    @Test
    void Should_BuildCredentialOfferCacheItem() {
        Long ttl = Instant.now().plusSeconds(300).getEpochSecond();
        CachedCredentialOffer cachedCredentialOffer =
                new CredentialOfferCacheItemBuilder()
                        .credentialIdentifier("4a1b1b18-b495-45ac-b0ce-73848bd32b70")
                        .itemId("cb2e831f-b2d9-4c7a-b42e-be5370ea4c77")
                        .walletSubjectId(
                                "urn:fdc:wallet.account.gov.uk:2024:DtPT8x-dp_73tnlY3KNTiCitziN9GEherD16bqxNt9i")
                        .timeToLive(ttl)
                        .build();

        assertEquals(
                "4a1b1b18-b495-45ac-b0ce-73848bd32b70",
                cachedCredentialOffer.getCredentialIdentifier());
        assertEquals("cb2e831f-b2d9-4c7a-b42e-be5370ea4c77", cachedCredentialOffer.getItemId());
        assertEquals(
                "urn:fdc:wallet.account.gov.uk:2024:DtPT8x-dp_73tnlY3KNTiCitziN9GEherD16bqxNt9i",
                cachedCredentialOffer.getWalletSubjectId());
        assertEquals(ttl, cachedCredentialOffer.getTimeToLive());
    }
}
