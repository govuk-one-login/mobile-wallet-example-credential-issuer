package uk.gov.di.mobile.wallet.cri.models;

public class CredentialOfferCacheItemBuilder {
    private String credentialIdentifier;
    private String documentId;
    private String walletSubjectId;
    private String notificationId;
    private Boolean redeemed;
    private Long expiry;
    private Long timeToLive;

    public CredentialOfferCacheItemBuilder credentialIdentifier(String credentialIdentifier) {
        this.credentialIdentifier = credentialIdentifier;
        return this;
    }

    public CredentialOfferCacheItemBuilder documentId(String documentId) {
        this.documentId = documentId;
        return this;
    }

    public CredentialOfferCacheItemBuilder walletSubjectId(String walletSubjectId) {
        this.walletSubjectId = walletSubjectId;
        return this;
    }

    public CredentialOfferCacheItemBuilder notificationId(String notificationId) {
        this.notificationId = notificationId;
        return this;
    }

    public CredentialOfferCacheItemBuilder redeemed(Boolean redeemed) {
        this.redeemed = redeemed;
        return this;
    }

    public CredentialOfferCacheItemBuilder expiry(Long expiry) {
        this.expiry = expiry;
        return this;
    }

    public CredentialOfferCacheItemBuilder timeToLive(Long timeToLive) {
        this.timeToLive = timeToLive;
        return this;
    }

    public CredentialOfferCacheItem build() {
        return new CredentialOfferCacheItem(
                credentialIdentifier,
                documentId,
                walletSubjectId,
                notificationId,
                redeemed,
                expiry,
                timeToLive);
    }
}
