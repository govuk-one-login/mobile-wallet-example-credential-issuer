package uk.gov.di.mobile.wallet.cri.models;

import lombok.Getter;
import lombok.Setter;
import software.amazon.awssdk.enhanced.dynamodb.mapper.annotations.DynamoDbBean;
import software.amazon.awssdk.enhanced.dynamodb.mapper.annotations.DynamoDbPartitionKey;

@Setter
@DynamoDbBean
public class CredentialOfferCacheItem {

    String credentialIdentifier;
    @Getter String walletSubjectId;
    @Getter String documentId;
    @Getter Long timeToLive;

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
}
