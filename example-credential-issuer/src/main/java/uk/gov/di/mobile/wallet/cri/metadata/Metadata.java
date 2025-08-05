package uk.gov.di.mobile.wallet.cri.metadata;

import com.fasterxml.jackson.annotation.JsonProperty;

public class Metadata {

    public Metadata(
            String credentialIssuer,
            String authorizationServers,
            String credentialEndpoint,
            String notificationEndpoint,
            String iacasEndpoint,
            Object credentialConfigurationsSupported) {
        this.credentialIssuer = credentialIssuer;
        this.authorizationServers = new String[] {authorizationServers};
        this.credentialEndpoint = credentialEndpoint;
        this.iacasEndpoint = iacasEndpoint;
        this.notificationEndpoint = notificationEndpoint;

        this.credentialConfigurationsSupported = credentialConfigurationsSupported;
    }

    String credentialIssuer;
    String[] authorizationServers;
    String credentialEndpoint;
    String notificationEndpoint;
    String iacasEndpoint;
    Object credentialConfigurationsSupported;

    @JsonProperty("credential_issuer")
    public String getCredentialIssuer() {
        return credentialIssuer;
    }

    @JsonProperty("authorization_servers")
    public String[] getAuthorizationServers() {
        return authorizationServers;
    }

    @JsonProperty("credential_endpoint")
    public String getCredentialEndpoint() {
        return credentialEndpoint;
    }

    @JsonProperty("notification_endpoint")
    public String getNotificationEndpoint() {
        return notificationEndpoint;
    }

    @JsonProperty("mdoc_iacas_uri")
    public String getIacasEndpoint() {
        return iacasEndpoint;
    }

    @JsonProperty("credential_configurations_supported")
    public Object getCredentialsSupported() {
        return credentialConfigurationsSupported;
    }
}
