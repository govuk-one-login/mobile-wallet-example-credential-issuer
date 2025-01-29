package uk.gov.di.mobile.wallet.cri.credential;

import net.minidev.json.JSONObject;

import java.util.UUID;

public class Document {

  private UUID documentId;
  private JSONObject data;
  private String vcType;
  private String vcDataModel;

  public Document(UUID documentId, JSONObject data, String vcType, String vcDataModel) {
    this.documentId = documentId;
    this.data = data;
    this.vcType = vcType;
    this.vcDataModel = vcDataModel;
  }

  public UUID getDocumentId() {
    return documentId;
  }

  public void setDocumentId(UUID documentId) {
    this.documentId = documentId;
  }

  public JSONObject getData() {
    return data;
  }

  public void setData(JSONObject data)  {
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