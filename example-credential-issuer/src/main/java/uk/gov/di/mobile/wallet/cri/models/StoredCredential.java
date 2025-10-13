package uk.gov.di.mobile.wallet.cri.models;

import lombok.Getter;
import lombok.Setter;
import software.amazon.awssdk.enhanced.dynamodb.mapper.annotations.DynamoDbAttribute;
import software.amazon.awssdk.enhanced.dynamodb.mapper.annotations.DynamoDbBean;
import software.amazon.awssdk.enhanced.dynamodb.mapper.annotations.DynamoDbPartitionKey;
import software.amazon.awssdk.enhanced.dynamodb.mapper.annotations.DynamoDbSecondaryPartitionKey;
import uk.gov.di.mobile.wallet.cri.credential.StatusListClient;

@Setter
@DynamoDbBean
public class StoredCredential {

    private String credentialIdentifier;
    @Getter private String notificationId;
    @Getter private String walletSubjectId;
    @Getter private Integer statusListIndex;
    @Getter private String statusListUri;
    @Getter private Long timeToLive;
    private String documentId;

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
        this.documentId = builder.documentId;
    }

    public static Builder builder() {
        return new Builder();
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

    public static class Builder {
        private String credentialIdentifier;
        private String notificationId;
        private String walletSubjectId;
        private Integer statusListIndex;
        private String statusListUri;
        private Long timeToLive;
        private String documentId;

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

        public Builder documentId(String documentId) {
            this.documentId = documentId;
            return this;
        }

        public StoredCredential build() {
            return new StoredCredential(this);
        }
    }
}
