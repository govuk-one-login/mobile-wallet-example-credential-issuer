package uk.gov.di.mobile.wallet.cri.metadata;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.google.common.io.Resources;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.File;
import java.io.IOException;
import java.util.Map;

public class MetadataBuilder {

    String credentialIssuer;
    String authorizationServers;
    String credentialEndpoint;
    String notificationEndpoint;
    String iacasEndpoint;
    Map<String, Object> credentialConfigurationsSupported;

    private static final Logger LOGGER = LoggerFactory.getLogger(MetadataBuilder.class);

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

    public MetadataBuilder setIacasEndpoint(String iacasEndpoint) throws IllegalArgumentException {
        if (iacasEndpoint == null) {
            throw new IllegalArgumentException("iacasEndpoint must not be null");
        }
        this.iacasEndpoint = iacasEndpoint;
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
                mapper.readValue(
                        credentialConfigurationsSupportedFilePath, new TypeReference<>() {});

        for (Map.Entry<String, Object> entry : credentialConfigurationsSupported.entrySet()) {
            String credentialName = entry.getKey();
            Object credentialValue = entry.getValue();
            if (credentialValue instanceof Map) {
                Map<String, Object> perCredential = (Map<String, Object>) credentialValue;
                perCredential.putIfAbsent(
                        "credential_refresh_web_journey_url",
                        this.credentialIssuer + "/refresh/" + credentialName);
            } else {
                LOGGER.warn("Unexpected type for credential value");
            }
        }

        return this;
    }

    public Metadata build() {
        return new Metadata(
                credentialIssuer,
                authorizationServers,
                credentialEndpoint,
                notificationEndpoint,
                iacasEndpoint,
                credentialConfigurationsSupported);
    }
}
