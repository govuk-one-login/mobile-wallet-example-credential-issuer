package uk.gov.di.mobile.wallet.cri.credential;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Getter;
import uk.gov.di.mobile.wallet.cri.annotations.ExcludeFromGeneratedCoverageReport;

import java.util.Map;

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

    @JsonProperty("credentialTtlMinutes")
    private long credentialTtlMinutes;

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
            @JsonProperty(value = "credentialTtlMinutes", required = true)
                    long credentialTtlMinutes) {
        this.itemId = itemId;
        this.documentId = documentId;
        this.data = data;
        this.vcType = vcType;
        this.credentialTtlMinutes = credentialTtlMinutes;
    }
}
