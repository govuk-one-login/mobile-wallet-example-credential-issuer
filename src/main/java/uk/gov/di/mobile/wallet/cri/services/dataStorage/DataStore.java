package uk.gov.di.mobile.wallet.cri.services.dataStorage;

import uk.gov.di.mobile.wallet.cri.models.CredentialOfferCacheItem;

public interface DataStore {

    public void saveCredentialOffer(CredentialOfferCacheItem credentialOfferCacheItem);
}
