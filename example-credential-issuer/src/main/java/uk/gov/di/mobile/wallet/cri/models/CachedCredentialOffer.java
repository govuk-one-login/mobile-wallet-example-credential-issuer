package uk.gov.di.mobile.wallet.cri.models;

import lombok.Getter;
import lombok.Setter;
import software.amazon.awssdk.enhanced.dynamodb.mapper.annotations.DynamoDbBean;
import software.amazon.awssdk.enhanced.dynamodb.mapper.annotations.DynamoDbPartitionKey;

@Getter
@Setter
@DynamoDbBean
public class CachedCredentialOffer {

    String credentialIdentifier;
    String walletSubjectId;
    String documentId;
    Long expiry;
    Long timeToLive;

    public CachedCredentialOffer() {
        // Empty constructor required for DynamoDb BeanTableSchema
    }

    public CachedCredentialOffer(
            String credentialIdentifier,
            String documentId,
            String walletSubjectId,
            Long expiry,
            Long timeToLive) {
        this.credentialIdentifier = credentialIdentifier;
        this.documentId = documentId;
        this.walletSubjectId = walletSubjectId;
        this.expiry = expiry;
        this.timeToLive = timeToLive;
    }

    @DynamoDbPartitionKey
    public String getCredentialIdentifier() {
        return credentialIdentifier;
    }
}
