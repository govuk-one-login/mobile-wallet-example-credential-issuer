package uk.gov.di.mobile.wallet.cri.credential;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Getter;
import lombok.Setter;
import uk.gov.di.mobile.wallet.cri.annotations.ExcludeFromGeneratedCoverageReport;

import java.util.Map;

@Getter
@Setter
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

    public DocumentStoreRecord(
            String itemId,
            String documentId,
            Map<String, Object> data,
            String vcType,
            long credentialTtlMinutes) {
        this.itemId = itemId;
        this.documentId = documentId;
        this.data = data;
        this.vcType = vcType;
        this.credentialTtlMinutes = credentialTtlMinutes;
    }
}
