package uk.gov.di.mobile.wallet.cri.metadata;

import com.fasterxml.jackson.annotation.JsonProperty;

public class Metadata {

    public Metadata(
            String credential_issuer,
            String authorization_server,
            String credentials_endpoint,
            Object credentials_supported) {
        this.credential_issuer = credential_issuer;
        this.authorization_server = authorization_server;
        this.credentials_endpoint = credentials_endpoint;
        this.credentials_supported = credentials_supported;
    }

    String credential_issuer; // NOSONAR
    String authorization_server; // NOSONAR
    String credentials_endpoint; // NOSONAR
    Object credentials_supported; // NOSONAR

    @JsonProperty("credentials_endpoint")
    public String getCredentialsEndpoint() {
        return credentials_endpoint;
    }

    @JsonProperty("authorization_server")
    public String getAuthorizationServer() {
        return authorization_server;
    }

    @JsonProperty("credential_issuer")
    public String getCredentialIssuer() {
        return credential_issuer;
    }

    @JsonProperty("credentials_supported")
    public Object getCredentialsSupported() {
        return credentials_supported;
    }
}
