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

    private StoredCredential(StoredCredentialBuilder builder) {
        this.credentialIdentifier = builder.credentialIdentifier;
        this.notificationId = builder.notificationId;
        this.walletSubjectId = builder.walletSubjectId;
        this.statusListIndex = builder.statusListIndex;
        this.statusListUri = builder.statusListUri;
        this.timeToLive = builder.timeToLive;
        this.documentId = builder.documentId;
    }

    public static StoredCredentialBuilder builder() {
        return new StoredCredentialBuilder();
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

    public static class StoredCredentialBuilder {
        private String credentialIdentifier;
        private String notificationId;
        private String walletSubjectId;
        private Integer statusListIndex;
        private String statusListUri;
        private Long timeToLive;
        private String documentId;

        private StoredCredentialBuilder() {}

        public StoredCredentialBuilder credentialIdentifier(String credentialIdentifier) {
            this.credentialIdentifier = credentialIdentifier;
            return this;
        }

        public StoredCredentialBuilder notificationId(String notificationId) {
            this.notificationId = notificationId;
            return this;
        }

        public StoredCredentialBuilder walletSubjectId(String walletSubjectId) {
            this.walletSubjectId = walletSubjectId;
            return this;
        }

        public StoredCredentialBuilder statusList(StatusListClient.IssueResponse issuerResponse) {
            if (issuerResponse != null) {
                this.statusListIndex = issuerResponse.idx();
                this.statusListUri = issuerResponse.uri();
            }
            return this;
        }

        public StoredCredentialBuilder timeToLive(Long timeToLive) {
            this.timeToLive = timeToLive;
            return this;
        }

        public StoredCredentialBuilder documentId(String documentId) {
            this.documentId = documentId;
            return this;
        }

        public StoredCredential build() {
            return new StoredCredential(this);
        }
    }
}
