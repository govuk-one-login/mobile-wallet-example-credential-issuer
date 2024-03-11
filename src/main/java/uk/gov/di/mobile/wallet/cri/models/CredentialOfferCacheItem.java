package uk.gov.di.mobile.wallet.cri.models;

import software.amazon.awssdk.enhanced.dynamodb.mapper.annotations.DynamoDbBean;
import software.amazon.awssdk.enhanced.dynamodb.mapper.annotations.DynamoDbPartitionKey;

@DynamoDbBean
public class CredentialOfferCacheItem {

    private String credentialIdentifier;
    private String walletSubjectId;
    private String documentId;

    // Required for DynamoDb BeanTableSchema
    public CredentialOfferCacheItem() {}

    public CredentialOfferCacheItem(
            String credentialIdentifier, String documentId, String walletSubjectId) {
        this.credentialIdentifier = credentialIdentifier;
        this.documentId = documentId;
        this.walletSubjectId = walletSubjectId;
    }

    @DynamoDbPartitionKey
    public String getCredentialIdentifier() {
        return credentialIdentifier;
    }

    public String getDocumentId() {
        return documentId;
    }

    public String getWalletSubjectId() {
        return walletSubjectId;
    }

    public void setCredentialIdentifier(String credentialIdentifier) {
        this.credentialIdentifier = credentialIdentifier;
    }

    public void setDocumentId(String documentId) {
        this.documentId = documentId;
    }

    public void setWalletSubjectId(String walletSubjectId) {
        this.walletSubjectId = walletSubjectId;
    }
}
