package uk.gov.di.mobile.wallet.cri.did_document;

import com.fasterxml.jackson.annotation.JsonProperty;

public class Did {

    public Did(String id, String type, String controller, PublicKeyJwk publicKeyJwk) {
        this.id = id;
        this.type = type;
        this.controller = controller;
        this.publicKeyJwk = publicKeyJwk;
    }

    String id;
    String type;
    String controller;
    PublicKeyJwk publicKeyJwk;

    @JsonProperty("id")
    public String getId() {
        return id;
    }

    @JsonProperty("type")
    public String getType() {
        return type;
    }

    @JsonProperty("controller")
    public String getController() {
        return controller;
    }

    @JsonProperty("publicKeyJwk")
    public PublicKeyJwk getPublicKeyJwk() {
        return publicKeyJwk;
    }
}
