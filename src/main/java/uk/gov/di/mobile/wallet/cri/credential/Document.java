package uk.gov.di.mobile.wallet.cri.credential;

import com.fasterxml.jackson.annotation.JsonProperty;

import java.util.Map;
import java.util.UUID;

public class Document {
    @JsonProperty("documentId")
    private String documentId;

    @JsonProperty("data")
    private Map<String, Object> data;

    @JsonProperty("vcType")
    private String vcType;

    @JsonProperty("vcDataModel")
    private String vcDataModel;

    public Document() {}

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

    public void setDocumentId(String documentId) {
        this.documentId = documentId;
    }

    public Map<String, Object> getData() {
        return data;
    }

    public void setData(Map<String, Object> data) {
        this.data = data;
    }

    public String getVcType() {
        return vcType;
    }

    public void setVcType(String vcType) {
        this.vcType = vcType;
    }

    public String getVcDataModel() {
        return vcDataModel;
    }

    public void setVcDataModel(String vcDataModel) {
        this.vcDataModel = vcDataModel;
    }
}
