package uk.gov.di.mobile.wallet.cri.models;

import lombok.Getter;
import software.amazon.awssdk.enhanced.dynamodb.mapper.annotations.DynamoDbAttribute;
import software.amazon.awssdk.enhanced.dynamodb.mapper.annotations.DynamoDbBean;
import software.amazon.awssdk.enhanced.dynamodb.mapper.annotations.DynamoDbPartitionKey;
import software.amazon.awssdk.enhanced.dynamodb.mapper.annotations.DynamoDbSecondaryPartitionKey;
import uk.gov.di.mobile.wallet.cri.credential.StatusListClient;

@DynamoDbBean
public class StoredCredential {

    private String credentialIdentifier;
    @Getter private String notificationId;
    @Getter private String walletSubjectId;
    @Getter private Integer statusListIndex;
    @Getter private String statusListUri;
    @Getter private Long timeToLive;
    private String documentPrimaryIdentifier;

    public StoredCredential() {
        // Empty constructor needed for dynamoDb deserialization
    }

    private StoredCredential(Builder builder) {
        this.credentialIdentifier = builder.credentialIdentifier;
        this.notificationId = builder.notificationId;
        this.walletSubjectId = builder.walletSubjectId;
        this.statusListIndex = builder.statusListIndex;
        this.statusListUri = builder.statusListUri;
        this.timeToLive = builder.timeToLive;
        this.documentPrimaryIdentifier = builder.documentPrimaryIdentifier;
    }

    public static Builder builder() {
        return new Builder();
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

    public void setCredentialIdentifier(String credentialIdentifier) {
        this.credentialIdentifier = credentialIdentifier;
    }

    public void setNotificationId(String notificationId) {
        this.notificationId = notificationId;
    }

    public void setWalletSubjectId(String walletSubjectId) {
        this.walletSubjectId = walletSubjectId;
    }

    public void setStatusListIndex(Integer statusListIndex) {
        this.statusListIndex = statusListIndex;
    }

    public void setStatusListUri(String statusListUri) {
        this.statusListUri = statusListUri;
    }

    public void setTimeToLive(Long timeToLive) {
        this.timeToLive = timeToLive;
    }

    public void setDocumentPrimaryIdentifier(String documentPrimaryIdentifier) {
        this.documentPrimaryIdentifier = documentPrimaryIdentifier;
    }

    public static class Builder {
        private String credentialIdentifier;
        private String notificationId;
        private String walletSubjectId;
        private Integer statusListIndex;
        private String statusListUri;
        private Long timeToLive;
        private String documentPrimaryIdentifier;

        private Builder() {}

        public Builder credentialIdentifier(String credentialIdentifier) {
            this.credentialIdentifier = credentialIdentifier;
            return this;
        }

        public Builder notificationId(String notificationId) {
            this.notificationId = notificationId;
            return this;
        }

        public Builder walletSubjectId(String walletSubjectId) {
            this.walletSubjectId = walletSubjectId;
            return this;
        }

        public Builder statusList(StatusListClient.IssueResponse issuerResponse) {
            if (issuerResponse != null) {
                this.statusListIndex = issuerResponse.idx();
                this.statusListUri = issuerResponse.uri();
            }
            return this;
        }

        public Builder timeToLive(Long timeToLive) {
            this.timeToLive = timeToLive;
            return this;
        }

        public Builder documentPrimaryIdentifier(String documentPrimaryIdentifier) {
            this.documentPrimaryIdentifier = documentPrimaryIdentifier;
            return this;
        }

        public StoredCredential build() {
            return new StoredCredential(this);
        }
    }
}
