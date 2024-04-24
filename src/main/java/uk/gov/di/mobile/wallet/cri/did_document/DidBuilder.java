package uk.gov.di.mobile.wallet.cri.did_document;

import com.fasterxml.jackson.annotation.JsonProperty;
import com.nimbusds.jose.jwk.ECKey;
import com.nimbusds.jose.jwk.JWK;

public class DidBuilder {

    String id;
    String type;
    String controller;
    PublicKeyJwk jwk;

    public DidBuilder() {}

    @JsonProperty("id")
    public DidBuilder setId(String id) throws IllegalArgumentException {
        if (id == null) {
            throw new IllegalArgumentException("id must not be null");
        }
        this.id = id;
        return this;
    }

    @JsonProperty("type")
    public DidBuilder setType(String type) throws IllegalArgumentException {
        if (type == null) {
            throw new IllegalArgumentException("type must not be null");
        }
        this.type = type;
        return this;
    }

    @JsonProperty("controller")
    public DidBuilder setController(String controller) throws IllegalArgumentException {
        if (controller == null) {
            throw new IllegalArgumentException("controller must not be null");
        }
        this.controller = controller;
        return this;
    }

    @JsonProperty("publicKeyJwk")
    public DidBuilder setPublicKeyJwk(JWK jwk) throws IllegalArgumentException {
        if (jwk == null) {
            throw new IllegalArgumentException("publicKeyJwk must not be null");
        }

        if (jwk instanceof ECKey ecKey) {
            this.jwk =
                    new PublicKeyJwkBuilder()
                            .setKid(jwk.getKeyID())
                            .setKty(jwk.getKeyType().getValue())
                            .setCrv(ecKey.getCurve().toString())
                            .setX(ecKey.getX().toString())
                            .setY(ecKey.getY().toString())
                            .build();
            return this;
        }
        return null;
    }

    public Did build() {
        return new Did(id, type, controller, jwk);
    }
}
