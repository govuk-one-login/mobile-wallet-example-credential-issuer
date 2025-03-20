package uk.gov.di.mobile.wallet.cri.metadata;

import com.fasterxml.jackson.annotation.JsonProperty;

public class Metadata {

    public Metadata(
            String credentialIssuer,
            String authorizationServers,
            String credentialsEndpoint,
            String credentialEndpoint,
            String notificationEndpoint,
            Object credentialConfigurationsSupported) {
        this.credentialIssuer = credentialIssuer;
        this.authorizationServers = new String[] {authorizationServers};
        // TO DO: Remove credentials_endpoint once SDK has been updated:
        // https://govukverify.atlassian.net/browse/DCMAW-11040
        // https://govukverify.atlassian.net/browse/DCMAW-11043
        this.credentialsEndpoint = credentialsEndpoint;
        this.credentialEndpoint = credentialEndpoint;
        this.notificationEndpoint = notificationEndpoint;
        this.credentialConfigurationsSupported = credentialConfigurationsSupported;
    }

    String credentialIssuer; // NOSONAR
    String[] authorizationServers; // NOSONAR
    String credentialsEndpoint; // NOSONAR
    String credentialEndpoint; // NOSONAR
    String notificationEndpoint; // NOSONAR
    Object credentialConfigurationsSupported; // NOSONAR

    @JsonProperty("credential_issuer")
    public String getCredentialIssuer() {
        return credentialIssuer;
    }

    @JsonProperty("authorization_servers")
    public String[] getAuthorizationServers() {
        return authorizationServers;
    }

    @JsonProperty("credentials_endpoint")
    public String getCredentialsEndpoint() {
        return credentialsEndpoint;
    }

    @JsonProperty("credential_endpoint")
    public String getCredentialEndpoint() {
        return credentialEndpoint;
    }

    @JsonProperty("notification_endpoint")
    public String getNotificationEndpoint() {
        return notificationEndpoint;
    }

    @JsonProperty("credential_configurations_supported")
    public Object getCredentialsSupported() {
        return credentialConfigurationsSupported;
    }
}
