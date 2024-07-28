package uk.gov.di.mobile.wallet.cri.credential_offer;

import com.fasterxml.jackson.annotation.JsonProperty;

import java.util.Map;

public class CredentialOffer {

    private final String credential_issuer; // NOSONAR
    // TODO: remove credentialIssuer once SDK has been updated:
    // https://govukverify.atlassian.net/browse/DCMAW-9756
    private final String credentialIssuer;
    private final String[] credentials;
    private final Map<String, Map<String, String>> grants;

    public CredentialOffer(
            String credentialIssuer,
            String credentialType,
            Map<String, Map<String, String>> grants) {
        this.credential_issuer = credentialIssuer;
        this.credentialIssuer = credentialIssuer;
        this.credentials = new String[] {credentialType};
        this.grants = grants;
    }

    @JsonProperty("credential_issuer")
    public String getCredentialIssuer() {
        return credential_issuer;
    }

    @JsonProperty("credentialIssuer")
    public String getCredentialIssuerTemporary() {
        return credentialIssuer;
    }

    public String[] getCredentials() {
        return credentials;
    }

    public Map<String, Map<String, String>> getGrants() {
        return grants;
    }
}
