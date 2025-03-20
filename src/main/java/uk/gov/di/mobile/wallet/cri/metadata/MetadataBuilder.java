package uk.gov.di.mobile.wallet.cri.metadata;

import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.google.common.io.Resources;

import java.io.File;
import java.io.IOException;

public class MetadataBuilder {

    String credential_issuer; // NOSONAR
    String authorization_servers; // NOSONAR
    String credentials_endpoint; // NOSONAR
    String credential_endpoint; // NOSONAR
    String notification_endpoint;
    Object credential_configurations_supported; // NOSONAR

    @JsonProperty("credential_issuer")
    public MetadataBuilder setCredentialIssuer(String credentialIssuer)
            throws IllegalArgumentException {
        if (credentialIssuer == null) {
            throw new IllegalArgumentException("credential_issuer must not be null");
        }
        this.credential_issuer = credentialIssuer;
        return this;
    }

    @JsonProperty("authorization_servers")
    public MetadataBuilder setAuthorizationServers(String authorizationServers)
            throws IllegalArgumentException {
        if (authorizationServers == null) {
            throw new IllegalArgumentException("authorization_servers must not be null");
        }
        this.authorization_servers = authorizationServers;
        return this;
    }

    @JsonProperty("credentials_endpoint")
    public MetadataBuilder setCredentialsEndpoint(String credentialsEndpoint)
            throws IllegalArgumentException {
        if (credentialsEndpoint == null) {
            throw new IllegalArgumentException("credentials_endpoint must not be null");
        }
        this.credentials_endpoint = credentialsEndpoint;
        return this;
    }

    @JsonProperty("credential_endpoint")
    public MetadataBuilder setCredentialEndpoint(String credentialEndpoint)
            throws IllegalArgumentException {
        if (credentialEndpoint == null) {
            throw new IllegalArgumentException("credential_endpoint must not be null");
        }
        this.credential_endpoint = credentialEndpoint;
        return this;
    }

    @JsonProperty("notification_endpoint")
    public MetadataBuilder setNotificationEndpoint(String notificationEndpoint)
            throws IllegalArgumentException {
        if (notificationEndpoint == null) {
            throw new IllegalArgumentException("notification_endpoint must not be null");
        }
        this.notification_endpoint = notificationEndpoint;
        return this;
    }

    @JsonProperty("credential_configurations_supported")
    public MetadataBuilder setCredentialConfigurationsSupported(String fileName)
            throws IOException, IllegalArgumentException {
        if (fileName == null) {
            throw new IllegalArgumentException("fileName must not be null");
        }
        File credentialConfigurationsSupportedFilePath =
                new File(Resources.getResource(fileName).getPath());
        ObjectMapper mapper = new ObjectMapper();
        this.credential_configurations_supported =
                mapper.readValue(credentialConfigurationsSupportedFilePath, Object.class);
        return this;
    }

    public Metadata build() {
        return new Metadata(
                credential_issuer,
                authorization_servers,
                credentials_endpoint,
                credential_endpoint,
                notification_endpoint,
                credential_configurations_supported);
    }
}
