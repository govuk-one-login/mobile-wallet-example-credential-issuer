package uk.gov.di.mobile.wallet.cri.models;

import lombok.Builder;
import lombok.Getter;
import lombok.Setter;
import software.amazon.awssdk.enhanced.dynamodb.mapper.annotations.DynamoDbAttribute;
import software.amazon.awssdk.enhanced.dynamodb.mapper.annotations.DynamoDbBean;
import software.amazon.awssdk.enhanced.dynamodb.mapper.annotations.DynamoDbPartitionKey;
import software.amazon.awssdk.enhanced.dynamodb.mapper.annotations.DynamoDbSecondaryPartitionKey;

@Getter
@Setter
@Builder
@DynamoDbBean
public class StoredCredential {

    String credentialIdentifier;
    String notificationId;
    String walletSubjectId;
    Long timeToLive;
    String documentId;

    public StoredCredential() {
        // Empty constructor needed for dynamoDb deserialization
    }

    public StoredCredential(
            String credentialIdentifier,
            String notificationId,
            String walletSubjectId,
            Long timeToLive,
            String documentId) {
        this.credentialIdentifier = credentialIdentifier;
        this.notificationId = notificationId;
        this.walletSubjectId = walletSubjectId;
        this.timeToLive = timeToLive;
        this.documentId = documentId;
    }

    @DynamoDbPartitionKey
    public String getCredentialIdentifier() {
        return credentialIdentifier;
    }

    @DynamoDbSecondaryPartitionKey(indexNames = "documentIdIndex")
    @DynamoDbAttribute("documentId")
    public String getDocumentId() {
        return documentId;
    }
}
