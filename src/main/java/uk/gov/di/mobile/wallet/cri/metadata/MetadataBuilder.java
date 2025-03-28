package uk.gov.di.mobile.wallet.cri.metadata;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.google.common.io.Resources;

import java.io.File;
import java.io.IOException;

public class MetadataBuilder {

    String credentialIssuer;
    String authorizationServers;
    String credentialEndpoint;
    String notificationEndpoint;
    Object credentialConfigurationsSupported;

    public MetadataBuilder setCredentialIssuer(String credentialIssuer)
            throws IllegalArgumentException {
        if (credentialIssuer == null) {
            throw new IllegalArgumentException("credentialIssuer must not be null");
        }
        this.credentialIssuer = credentialIssuer;
        return this;
    }

    public MetadataBuilder setAuthorizationServers(String authorizationServers)
            throws IllegalArgumentException {
        if (authorizationServers == null) {
            throw new IllegalArgumentException("authorizationServers must not be null");
        }
        this.authorizationServers = authorizationServers;
        return this;
    }

    public MetadataBuilder setCredentialEndpoint(String credentialEndpoint)
            throws IllegalArgumentException {
        if (credentialEndpoint == null) {
            throw new IllegalArgumentException("credentialEndpoint must not be null");
        }
        this.credentialEndpoint = credentialEndpoint;
        return this;
    }

    public MetadataBuilder setNotificationEndpoint(String notificationEndpoint)
            throws IllegalArgumentException {
        if (notificationEndpoint == null) {
            throw new IllegalArgumentException("notificationEndpoint must not be null");
        }
        this.notificationEndpoint = notificationEndpoint;
        return this;
    }

    public MetadataBuilder setCredentialConfigurationsSupported(String fileName)
            throws IOException, IllegalArgumentException {
        if (fileName == null) {
            throw new IllegalArgumentException("fileName must not be null");
        }
        File credentialConfigurationsSupportedFilePath =
                new File(Resources.getResource(fileName).getPath());
        ObjectMapper mapper = new ObjectMapper();
        this.credentialConfigurationsSupported =
                mapper.readValue(credentialConfigurationsSupportedFilePath, Object.class);
        return this;
    }

    public Metadata build() {
        return new Metadata(
                credentialIssuer,
                authorizationServers,
                credentialEndpoint,
                notificationEndpoint,
                credentialConfigurationsSupported);
    }
}
