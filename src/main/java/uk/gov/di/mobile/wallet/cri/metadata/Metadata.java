package uk.gov.di.mobile.wallet.cri.metadata;

import com.fasterxml.jackson.annotation.JsonProperty;

public class Metadata {

    public Metadata(
            String credentialIssuer,
            String authorizationServers,
            String credentialsEndpoint,
            String credentialEndpoint,
            Object credentialConfigurationsSupported) {
        this.credential_issuer = credentialIssuer;
        this.authorization_servers = new String[] {authorizationServers};

        this.credentials_endpoint = credentialsEndpoint;
        // TODO: remove credentials_endpoint once SDK has been updated:
        // https://govukverify.atlassian.net/browse/DCMAW-11040
        // https://govukverify.atlassian.net/browse/DCMAW-11043

        this.credential_endpoint = credentialEndpoint;
        this.credential_configurations_supported = credentialConfigurationsSupported;
    }

    String credential_issuer; // NOSONAR
    String[] authorization_servers; // NOSONAR
    String credentials_endpoint; // NOSONAR
    String credential_endpoint; // NOSONAR
    Object credential_configurations_supported; // NOSONAR

    @JsonProperty("credentials_endpoint")
    public String getCredentialsEndpoint() {
        return credentials_endpoint;
    }

    @JsonProperty("credential_endpoint")
    public String getCredentialEndpoint() {
        return credential_endpoint;
    }

    @JsonProperty("authorization_servers")
    public String[] getAuthorizationServers() {
        return authorization_servers;
    }

    @JsonProperty("credential_issuer")
    public String getCredentialIssuer() {
        return credential_issuer;
    }

    @JsonProperty("credential_configurations_supported")
    public Object getCredentialsSupported() {
        return credential_configurations_supported;
    }
}
