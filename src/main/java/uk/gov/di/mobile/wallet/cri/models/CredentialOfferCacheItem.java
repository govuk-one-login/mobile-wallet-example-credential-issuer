package uk.gov.di.mobile.wallet.cri.models;

import lombok.Getter;
import lombok.Setter;
import software.amazon.awssdk.enhanced.dynamodb.mapper.annotations.DynamoDbBean;
import software.amazon.awssdk.enhanced.dynamodb.mapper.annotations.DynamoDbPartitionKey;

@Setter
@Getter
@DynamoDbBean
public class CredentialOfferCacheItem {

    String credentialIdentifier;
    String walletSubjectId;
    String documentId;
    String notificationId;
    Long timeToLive;

    // Required for DynamoDb BeanTableSchema
    public CredentialOfferCacheItem() {
        // Empty constructor required for DynamoDb BeanTableSchema
    }

    public CredentialOfferCacheItem(
            String credentialIdentifier, String documentId, String walletSubjectId, String notificationId) {
        this.credentialIdentifier = credentialIdentifier;
        this.documentId = documentId;
        this.walletSubjectId = walletSubjectId;
        this.notificationId = notificationId;
    }

    // Required for unit testing
    public CredentialOfferCacheItem(
            String credentialIdentifier,
            String documentId,
            String walletSubjectId,
            String notificationId,
            Long timeToLive) {
        this.credentialIdentifier = credentialIdentifier;
        this.documentId = documentId;
        this.walletSubjectId = walletSubjectId;
        this.notificationId = notificationId;
        this.timeToLive = timeToLive;
    }

    @DynamoDbPartitionKey
    public String getCredentialIdentifier() {
        return credentialIdentifier;
    }
}
