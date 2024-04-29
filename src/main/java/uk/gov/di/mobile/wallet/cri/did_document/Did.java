package uk.gov.di.mobile.wallet.cri.did_document;

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

    public String getId() {
        return id;
    }

    public String getType() {
        return type;
    }

    public String getController() {
        return controller;
    }

    public PublicKeyJwk getPublicKeyJwk() {
        return publicKeyJwk;
    }
}
