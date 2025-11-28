package uk.gov.di.mobile.wallet.cri.credential_offer;

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
    String itemId;
    Long timeToLive;

    public CachedCredentialOffer() {
        // Empty constructor required for DynamoDb BeanTableSchema
    }

    public CachedCredentialOffer(
            String credentialIdentifier, String itemId, String walletSubjectId, Long timeToLive) {
        this.credentialIdentifier = credentialIdentifier;
        this.itemId = itemId;
        this.walletSubjectId = walletSubjectId;
        this.timeToLive = timeToLive;
    }

    @DynamoDbPartitionKey
    public String getCredentialIdentifier() {
        return credentialIdentifier;
    }
}
