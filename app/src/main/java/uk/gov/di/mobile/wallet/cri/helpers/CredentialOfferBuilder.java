package uk.gov.di.mobile.wallet.cri.helpers;

public class CredentialOfferBuilder {

    String credential_issuer;
    String[] credentials;
    Object grants;

    public CredentialOfferBuilder(String credential_issuer, String[] credentials, Object grants){
        this.credential_issuer = credential_issuer;
        this.credentials = credentials;
        this.grants = grants;
    }
}

