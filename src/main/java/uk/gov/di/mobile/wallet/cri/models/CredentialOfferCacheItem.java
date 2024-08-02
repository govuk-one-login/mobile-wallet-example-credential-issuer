package uk.gov.di.mobile.wallet.cri.models;

import software.amazon.awssdk.enhanced.dynamodb.mapper.annotations.DynamoDbBean;
import software.amazon.awssdk.enhanced.dynamodb.mapper.annotations.DynamoDbPartitionKey;

@DynamoDbBean
public class CredentialOfferCacheItem {

    String credentialIdentifier;
    String walletSubjectId;
    String documentId;
    Long timeToLive;

    // Required for DynamoDb BeanTableSchema
    public CredentialOfferCacheItem() {}

    public CredentialOfferCacheItem(
            String credentialIdentifier, String documentId, String walletSubjectId) {
        this.credentialIdentifier = credentialIdentifier;
        this.documentId = documentId;
        this.walletSubjectId = walletSubjectId;
    }

    // Required for unit testing
    public CredentialOfferCacheItem(
            String credentialIdentifier,
            String documentId,
            String walletSubjectId,
            Long timeToLive) {
        this.credentialIdentifier = credentialIdentifier;
        this.documentId = documentId;
        this.walletSubjectId = walletSubjectId;
        this.timeToLive = timeToLive;
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

    public Long getTimeToLive() {
        return timeToLive;
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

    public void setTimeToLive(Long timeToLive) {
        this.timeToLive = timeToLive;
    }
}
