package uk.gov.di.mobile.wallet.cri.helpers;

import software.amazon.awssdk.enhanced.dynamodb.mapper.annotations.DynamoDbBean;
import software.amazon.awssdk.enhanced.dynamodb.mapper.annotations.DynamoDbPartitionKey;

@DynamoDbBean
public class CredentialOfferCacheItem {
    String credentialIdentifier;
    String walletSubjectId;
    String documentId;

    // Required for DynamoDb BeanTableSchema
    public CredentialOfferCacheItem() {}

    public CredentialOfferCacheItem(
            String credentialIdentifier, String walletSubject, String documentId) {
        this.credentialIdentifier = credentialIdentifier;
        this.walletSubjectId = walletSubject;
        this.documentId = documentId;
    }

    @DynamoDbPartitionKey
    public String getCredentialIdentifier() {
        return credentialIdentifier;
    }

    public void setCredentialIdentifier(String credentialIdentifier) {
        this.credentialIdentifier = credentialIdentifier;
    }

    public void setWalletSubjectId(String walletSubjectId) {
        this.walletSubjectId = walletSubjectId;
    }

    public void setDocumentId(String documentId) {
        this.documentId = documentId;
    }

    public String getWalletSubjectId() {
        return walletSubjectId;
    }

    public String getDocumentId() {
        return documentId;
    }
}
