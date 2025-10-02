package uk.gov.di.mobile.wallet.cri.models;

import lombok.Builder;
import lombok.Getter;
import lombok.Setter;
import software.amazon.awssdk.enhanced.dynamodb.mapper.annotations.DynamoDbAttribute;
import software.amazon.awssdk.enhanced.dynamodb.mapper.annotations.DynamoDbBean;
import software.amazon.awssdk.enhanced.dynamodb.mapper.annotations.DynamoDbPartitionKey;
import software.amazon.awssdk.enhanced.dynamodb.mapper.annotations.DynamoDbSecondaryPartitionKey;
import uk.gov.di.mobile.wallet.cri.credential.StatusList;

@Getter
@Setter
@Builder
@DynamoDbBean
public class StoredCredential {

    String credentialIdentifier;
    String notificationId;
    String walletSubjectId;
    StatusList statusList;
    Long timeToLive;
    String documentPrimaryIdentifier;

    public StoredCredential() {
        // Empty constructor needed for dynamoDb deserialization
    }

    public StoredCredential(
            String credentialIdentifier,
            String notificationId,
            String walletSubjectId,
            StatusList statusList,
            Long timeToLive,
            String documentPrimaryIdentifier) {
        this.credentialIdentifier = credentialIdentifier;
        this.notificationId = notificationId;
        this.walletSubjectId = walletSubjectId;
        this.statusList = statusList;
        this.timeToLive = timeToLive;
        this.documentPrimaryIdentifier = documentPrimaryIdentifier;
    }

    @DynamoDbPartitionKey
    public String getCredentialIdentifier() {
        return credentialIdentifier;
    }

    @DynamoDbSecondaryPartitionKey(indexNames = "documentPrimaryIdentifierIndex")
    @DynamoDbAttribute("documentPrimaryIdentifier")
    public String getDocumentPrimaryIdentifier() {
        return documentPrimaryIdentifier;
    }
}
