package uk.gov.di.mobile.wallet.cri.helpers;

import java.util.Map;

public class CredentialOffer {
    String credential_issuer;
    String[] credentials;
    Map<String, Map<String, String>> grants;

    public CredentialOffer(
            String credential_issuer,
            String[] credentials,
            Map<String, Map<String, String>> grants) {
        this.credential_issuer = credential_issuer;
        this.credentials = credentials;
        this.grants = grants;
    }

    public String getCredential_issuer() {
        return credential_issuer;
    }

    public String[] getCredentials() {
        return credentials;
    }

    public Map<String, Map<String, String>> getGrants() {
        return grants;
    }
}
