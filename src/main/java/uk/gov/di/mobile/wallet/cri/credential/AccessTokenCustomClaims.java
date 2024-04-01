package uk.gov.di.mobile.wallet.cri.credential;

public class AccessTokenCustomClaims {
    private String credential_identifier;
    private String sub;
    private String c_nonce;

    public AccessTokenCustomClaims() {}
    ;

    public AccessTokenCustomClaims(String credential_identifier, String sub, String c_nonce) {
        this.credential_identifier = credential_identifier;
        this.sub = sub;
        this.c_nonce = c_nonce;
    }

    public String getCredentialIdentifier() {
        return credential_identifier;
    }

    public void setCredentialIdentifier(String credential_identifier) {
        this.credential_identifier = credential_identifier;
    }

    public String getSub() {
        return sub;
    }

    public void setSub(String sub) {
        this.sub = sub;
    }

    public String getCNonce() {
        return c_nonce;
    }

    public void setCNonce(String c_nonce) {
        this.c_nonce = c_nonce;
    }
}
