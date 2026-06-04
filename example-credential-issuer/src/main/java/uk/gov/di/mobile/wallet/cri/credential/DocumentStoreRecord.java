package uk.gov.di.mobile.wallet.cri.credential;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Getter;
import uk.gov.di.mobile.wallet.cri.annotations.ExcludeFromGeneratedCoverageReport;

import java.util.Map;
import java.util.Optional;

@Getter
public class DocumentStoreRecord {
    @JsonProperty("itemId")
    private String itemId;

    @JsonProperty("documentId")
    private String documentId;

    @JsonProperty("data")
    private Map<String, Object> data;

    @JsonProperty("vcType")
    private String vcType;

    @JsonProperty("credentialTtlSeconds")
    private long credentialTtlSeconds;

    /**
     * Optional duration in seconds from credential issuance when the credential is expected to be
     * updated. When absent, no expectedUpdate claim is included in the issued credential.
     */
    @JsonProperty("expectedUpdateSeconds")
    private Long expectedUpdateSeconds;

    @ExcludeFromGeneratedCoverageReport
    public DocumentStoreRecord() {
        // Empty constructor required for deserialization
    }

    @JsonCreator
    public DocumentStoreRecord(
            @JsonProperty(value = "itemId", required = true) String itemId,
            @JsonProperty(value = "documentId", required = true) String documentId,
            @JsonProperty(value = "data", required = true) Map<String, Object> data,
            @JsonProperty(value = "vcType", required = true) String vcType,
            @JsonProperty(value = "credentialTtlSeconds", required = true)
                    long credentialTtlSeconds,
            @JsonProperty(value = "expectedUpdateSeconds") Long expectedUpdateSeconds) {
        this.itemId = itemId;
        this.documentId = documentId;
        this.data = data;
        this.vcType = vcType;
        this.credentialTtlSeconds = credentialTtlSeconds;
        this.expectedUpdateSeconds = expectedUpdateSeconds;
    }

    /**
     * Returns the optional expected update duration in seconds.
     *
     * @return An {@link Optional} containing the duration in seconds from credential issuance when
     *     the credential is expected to be updated, or empty if not specified.
     */
    public Optional<Long> getExpectedUpdateSeconds() {
        return Optional.ofNullable(expectedUpdateSeconds);
    }
}
