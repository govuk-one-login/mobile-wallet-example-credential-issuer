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
    String drivingLicenceNumber;

    public StoredCredential() {
        // Empty constructor needed for dynamoDb deserialization
    }

    public StoredCredential(
            String credentialIdentifier,
            String notificationId,
            String walletSubjectId,
            Long timeToLive,
            String drivingLicenceNumber) {
        this.credentialIdentifier = credentialIdentifier;
        this.notificationId = notificationId;
        this.walletSubjectId = walletSubjectId;
        this.timeToLive = timeToLive;
        this.drivingLicenceNumber = drivingLicenceNumber;
    }

    @DynamoDbPartitionKey
    public String getCredentialIdentifier() {
        return credentialIdentifier;
    }

    @DynamoDbSecondaryPartitionKey(indexNames = "DrivingLicenceNumberIndex")
    @DynamoDbAttribute("drivingLicenceNumber")
    public String getDrivingLicenceNumber() {
        return drivingLicenceNumber;
    }
}
