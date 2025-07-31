package uk.gov.di.mobile.wallet.cri.did_document;

import com.fasterxml.jackson.annotation.JsonProperty;

public class PublicKeyJwk {

    public PublicKeyJwk(String kty, String kid, String crv, String x, String y, String alg) {
        this.kty = kty;
        this.kid = kid;
        this.crv = crv;
        this.x = x;
        this.y = y;
        this.alg = alg;
    }

    String kty;
    String kid;
    String crv;
    String x;
    String y;
    String alg;

    @JsonProperty("kty")
    public String getKty() {
        return kty;
    }

    @JsonProperty("kid")
    public String getKid() {
        return kid;
    }

    @JsonProperty("crv")
    public String getCrv() {
        return crv;
    }

    @JsonProperty("x")
    public String getX() {
        return x;
    }

    @JsonProperty("y")
    public String getY() {
        return y;
    }

    @JsonProperty("alg")
    public String getAlg() {
        return alg;
    }
}
