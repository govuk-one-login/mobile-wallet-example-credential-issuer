package uk.gov.di.mobile.wallet.cri.models;

import lombok.Getter;
import lombok.Setter;
import software.amazon.awssdk.enhanced.dynamodb.mapper.annotations.DynamoDbBean;
import software.amazon.awssdk.enhanced.dynamodb.mapper.annotations.DynamoDbPartitionKey;

@Getter
@DynamoDbBean
public class CredentialOfferCacheItem {

    @Setter String credentialIdentifier;
    String walletSubjectId;
    String documentId;
    String notificationId;
    @Setter Boolean redeemed;
    Long expiry;
    Long timeToLive;

    public CredentialOfferCacheItem() {
        // Empty constructor required for DynamoDb BeanTableSchema
    }

    public CredentialOfferCacheItem(
            String credentialIdentifier,
            String documentId,
            String walletSubjectId,
            String notificationId,
            Boolean redeemed,
            Long expiry,
            Long timeToLive) {
        this.credentialIdentifier = credentialIdentifier;
        this.documentId = documentId;
        this.walletSubjectId = walletSubjectId;
        this.notificationId = notificationId;
        this.redeemed = redeemed;
        this.expiry = expiry;
        this.timeToLive = timeToLive;
    }

    @DynamoDbPartitionKey
    public String getCredentialIdentifier() {
        return credentialIdentifier;
    }
}
