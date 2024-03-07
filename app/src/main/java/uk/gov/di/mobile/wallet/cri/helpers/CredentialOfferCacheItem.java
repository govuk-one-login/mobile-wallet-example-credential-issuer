package uk.gov.di.mobile.wallet.cri.helpers;

public class CredentialOfferCacheItem {
    String credential_identifier;
    String walletSubject;
    String documentId;

    public CredentialOfferCacheItem(
            String credential_identifier, String walletSubject, String documentId) {
        this.credential_identifier = credential_identifier;
        this.walletSubject = walletSubject;
        this.documentId = documentId;
    }

    public String getCredential_identifier() {
        return credential_identifier;
    }

    public String getWalletSubject() {
        return walletSubject;
    }

    public String getDocumentId() {
        return documentId;
    }
}
