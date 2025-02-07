package uk.gov.di.mobile.wallet.cri.did_document;

import lombok.Getter;

@Getter
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

}
