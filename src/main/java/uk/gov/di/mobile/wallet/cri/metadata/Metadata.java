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
        this.credential_issuer = credentialIssuer;
        this.authorization_servers = new String[] {authorizationServers};
        // TO DO: Remove credentials_endpoint once SDK has been updated:
        // https://govukverify.atlassian.net/browse/DCMAW-11040
        // https://govukverify.atlassian.net/browse/DCMAW-11043
        this.credentials_endpoint = credentialsEndpoint;
        this.credential_endpoint = credentialEndpoint;
        this.notification_endpoint = notificationEndpoint;
        this.credential_configurations_supported = credentialConfigurationsSupported;
    }

    String credential_issuer; // NOSONAR
    String[] authorization_servers; // NOSONAR
    String credentials_endpoint; // NOSONAR
    String credential_endpoint; // NOSONAR
    String notification_endpoint; // NOSONAR
    Object credential_configurations_supported; // NOSONAR

    @JsonProperty("credential_issuer")
    public String getCredentialIssuer() {
        return credential_issuer;
    }

    @JsonProperty("authorization_servers")
    public String[] getAuthorizationServers() {
        return authorization_servers;
    }

    @JsonProperty("credentials_endpoint")
    public String getCredentialsEndpoint() {
        return credentials_endpoint;
    }

    @JsonProperty("credential_endpoint")
    public String getCredentialEndpoint() {
        return credential_endpoint;
    }

    @JsonProperty("notification_endpoint")
    public String getNotificationEndpoint() {
        return notification_endpoint;
    }

    @JsonProperty("credential_configurations_supported")
    public Object getCredentialsSupported() {
        return credential_configurations_supported;
    }
}
