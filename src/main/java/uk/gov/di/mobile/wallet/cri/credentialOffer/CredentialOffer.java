package uk.gov.di.mobile.wallet.cri.credentialOffer;

import java.util.Map;

public class CredentialOffer {

    private final String credential_issuer; // NOSONAR
    private final String[] credentials;
    private final Map<String, Map<String, String>> grants;

    public CredentialOffer(
            String credential_issuer, // NOSONAR
            String[] credentials,
            Map<String, Map<String, String>> grants) {
        this.credential_issuer = credential_issuer;
        this.credentials = credentials;
        this.grants = grants;
    }

    public String getCredentialIssuer() {
        return credential_issuer;
    }

    public String[] getCredentials() {
        return credentials;
    }

    public Map<String, Map<String, String>> getGrants() {
        return grants;
    }
}
