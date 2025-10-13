package uk.gov.di.mobile.wallet.cri.models;

public class CredentialOfferCacheItemBuilder {
    private String credentialIdentifier;
    private String itemId;
    private String walletSubjectId;
    private Long timeToLive;

    public CredentialOfferCacheItemBuilder credentialIdentifier(String credentialIdentifier) {
        this.credentialIdentifier = credentialIdentifier;
        return this;
    }

    public CredentialOfferCacheItemBuilder itemId(String itemId) {
        this.itemId = itemId;
        return this;
    }

    public CredentialOfferCacheItemBuilder walletSubjectId(String walletSubjectId) {
        this.walletSubjectId = walletSubjectId;
        return this;
    }

    public CredentialOfferCacheItemBuilder timeToLive(Long timeToLive) {
        this.timeToLive = timeToLive;
        return this;
    }

    public CachedCredentialOffer build() {
        return new CachedCredentialOffer(credentialIdentifier, itemId, walletSubjectId, timeToLive);
    }
}
