package uk.gov.di.mobile.wallet.cri.credential;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Getter;
import lombok.Setter;

import java.util.Map;
import java.util.UUID;

@Setter
public class Document {
    @JsonProperty("documentId")
    private String documentId;

    @Getter
    @JsonProperty("data")
    private Map<String, Object> data;

    @Getter
    @JsonProperty("vcType")
    private String vcType;

    @Getter
    @JsonProperty("vcDataModel")
    private String vcDataModel;

    public Document(
            String documentId, Map<String, Object> data, String vcType, String vcDataModel) {
        this.documentId = documentId;
        this.data = data;
        this.vcType = vcType;
        this.vcDataModel = vcDataModel;
    }

    public UUID getDocumentId() {
        return UUID.fromString(documentId);
    }
}
