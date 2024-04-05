package uk.gov.di.mobile.wallet.cri.metadata;

import com.fasterxml.jackson.annotation.JsonProperty;

public class Metadata {

    public Metadata(
            String credential_issuer,
            String authorization_servers,
            String credentials_endpoint,
            Object credential_configurations_supported) {
        this.credential_issuer = credential_issuer;
        this.authorization_servers = new String[] {authorization_servers};
        this.credentials_endpoint = credentials_endpoint;
        this.credential_configurations_supported = credential_configurations_supported;
    }

    String credential_issuer; // NOSONAR
    String[] authorization_servers; // NOSONAR
    String credentials_endpoint; // NOSONAR
    Object credential_configurations_supported; // NOSONAR

    @JsonProperty("credentials_endpoint")
    public String getCredentialsEndpoint() {
        return credentials_endpoint;
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
