package uk.gov.di.mobile.wallet.cri.models;

import lombok.Getter;
import lombok.Setter;
import software.amazon.awssdk.enhanced.dynamodb.mapper.annotations.DynamoDbBean;
import software.amazon.awssdk.enhanced.dynamodb.mapper.annotations.DynamoDbPartitionKey;

@Getter
@Setter
@DynamoDbBean
public class StoredCredential {

    String credentialIdentifier;
    String notificationId;
    String walletSubjectId;
    String documentNumber;
    Integer idx;
    String uri;
    Long timeToLive;

    public StoredCredential() {
        // Empty constructor needed for dynamoDb deserialization
    }

    public StoredCredential(
            String credentialIdentifier,
            String notificationId,
            String walletSubjectId,
            String documentNumber,
            Integer idx,
            String uri,
            Long timeToLive) {
        this.credentialIdentifier = credentialIdentifier;
        this.notificationId = notificationId;
        this.walletSubjectId = walletSubjectId;
        this.documentNumber = documentNumber;
        this.idx = idx;
        this.uri = uri;
        this.timeToLive = timeToLive;
    }

    @DynamoDbPartitionKey
    public String getCredentialIdentifier() {
        return credentialIdentifier;
    }
}
