package uk.gov.di.mobile.wallet.cri.metadata;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.google.common.io.Resources;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class MetadataBuilder {

    private static final String LOCALE_EN = "en";
    private static final String LOCALE_CY = "cy";
    private static final String ISSUER_NAME_EN = "GOV.UK Wallet Example Credential Issuer";
    private static final String ISSUER_NAME_CY = "ISSUER_NAME_WELSH";

    String credentialIssuer;
    String authorizationServers;
    String credentialEndpoint;
    String notificationEndpoint;
    String iacasEndpoint;
    Map<String, Object> credentialConfigurationsSupported;
    List<Map<String, Object>> display;

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

    public MetadataBuilder setCredentialConfigurationsSupported(
            String fileName, String credentialStore) throws IOException, IllegalArgumentException {
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
            Map<String, Object> perCredential = (Map<String, Object>) credentialValue;
            perCredential.putIfAbsent(
                    "credential_refresh_web_journey_url",
                    credentialStore + "/refresh/" + credentialName);
        }

        return this;
    }

    public MetadataBuilder setDisplay(String logoEndpoint) throws IllegalArgumentException {
        if (logoEndpoint == null) {
            throw new IllegalArgumentException("logoEndpoint must not be null");
        }

        this.display = new ArrayList<>();

        Map<String, Object> englishDisplay = new HashMap<>();
        englishDisplay.put("locale", LOCALE_EN);
        englishDisplay.put("name", ISSUER_NAME_EN);

        Map<String, Object> welshDisplay = new HashMap<>();
        welshDisplay.put("locale", LOCALE_CY);
        welshDisplay.put("name", ISSUER_NAME_CY);

        Map<String, Object> logo = new HashMap<>();
        logo.put("uri", logoEndpoint);
        welshDisplay.put("logo", logo);
        englishDisplay.put("logo", logo);

        this.display.add(englishDisplay);
        this.display.add(welshDisplay);

        return this;
    }

    public Metadata build() {
        return new Metadata(
                credentialIssuer,
                authorizationServers,
                credentialEndpoint,
                notificationEndpoint,
                iacasEndpoint,
                credentialConfigurationsSupported,
                display);
    }
}
