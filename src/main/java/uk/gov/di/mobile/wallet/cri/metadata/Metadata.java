package uk.gov.di.mobile.wallet.cri.metadata;

import com.fasterxml.jackson.annotation.JsonProperty;

public class Metadata {

    String credential_issuer; // NOSONAR
    String authorization_server; // NOSONAR
    String credentials_endpoint; // NOSONAR
    Object credentials_supported; // NOSONAR

    @JsonProperty("credential_issuer")
    public String getCredentialIssuer() {
        return credential_issuer;
    }

    @JsonProperty("credential_issuer")
    public void setCredentialIssuer(String credential_issuer) {
        this.credential_issuer = credential_issuer;
    }

    @JsonProperty("authorization_server")
    public String getAuthorizationServer() {
        return authorization_server;
    }

    @JsonProperty("authorization_server")
    public void setAuthorizationServer(String authorization_server) {
        this.authorization_server = authorization_server;
    }

    @JsonProperty("credentials_endpoint")
    public String getCredentialsEndpoint() {
        return credentials_endpoint;
    }

    @JsonProperty("credentials_endpoint")
    public void setCredentialsEndpoint(String credentials_endpoint) {
        this.credentials_endpoint = credentials_endpoint;
    }

    @JsonProperty("credentials_supported")
    public Object getCredentialsSupported() {
        return credentials_supported;
    }

    @JsonProperty("credentials_supported")
    public void setCredentialsSupported(Object credentials_supported) {
        this.credentials_supported = credentials_supported;
    }
}
