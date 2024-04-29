package uk.gov.di.mobile.wallet.cri.did_document;

public class PublicKeyJwkBuilder {

    String kty;
    String kid;
    String crv;
    String x;
    String y;

    public PublicKeyJwkBuilder setKty(String kty) throws IllegalArgumentException {
        if (kty == null) {
            throw new IllegalArgumentException("kty attribute must not be null");
        }
        this.kty = kty;
        return this;
    }

    public PublicKeyJwkBuilder setKid(String kid) throws IllegalArgumentException {
        if (kid == null) {
            throw new IllegalArgumentException("kid attribute must not be null");
        }
        this.kid = kid;
        return this;
    }

    public PublicKeyJwkBuilder setCrv(String crv) throws IllegalArgumentException {
        if (crv == null) {
            throw new IllegalArgumentException("crv attribute must not be null");
        }
        this.crv = crv;
        return this;
    }

    public PublicKeyJwkBuilder setX(String x) throws IllegalArgumentException {
        if (x == null) {
            throw new IllegalArgumentException("x attribute must not be null");
        }
        this.x = x;
        return this;
    }

    public PublicKeyJwkBuilder setY(String y) throws IllegalArgumentException {
        if (y == null) {
            throw new IllegalArgumentException("y attribute must not be null");
        }
        this.y = y;
        return this;
    }

    public PublicKeyJwk build() {
        return new PublicKeyJwk(kty, kid, crv, x, y);
    }
}
