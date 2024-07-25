package uk.gov.di.mobile.wallet.cri.did_document;

import com.nimbusds.jose.jwk.ECKey;

public class DidBuilder {

    String id;
    String type;
    String controller;
    PublicKeyJwk jwk;

    public DidBuilder setId(String id) throws IllegalArgumentException {
        if (id == null) {
            throw new IllegalArgumentException("id must not be null");
        }
        this.id = id;
        return this;
    }

    public DidBuilder setType(String type) throws IllegalArgumentException {
        if (type == null) {
            throw new IllegalArgumentException("type must not be null");
        }
        this.type = type;
        return this;
    }

    public DidBuilder setController(String controller) throws IllegalArgumentException {
        if (controller == null) {
            throw new IllegalArgumentException("controller must not be null");
        }
        this.controller = controller;
        return this;
    }

    public DidBuilder setPublicKeyJwk(ECKey jwk) throws IllegalArgumentException {
        if (jwk == null) {
            throw new IllegalArgumentException("jwk must not be null");
        }

        this.jwk =
                new PublicKeyJwkBuilder()
                        .setKid(jwk.getKeyID())
                        .setKty(jwk.getKeyType().getValue())
                        .setCrv(jwk.getCurve().toString())
                        .setX(jwk.getX().toString())
                        .setY(jwk.getY().toString())
                        .build();
        return this;
    }

    public Did build() {
        return new Did(id, type, controller, jwk);
    }
}
