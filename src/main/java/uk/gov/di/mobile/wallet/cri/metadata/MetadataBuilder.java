package uk.gov.di.mobile.wallet.cri.metadata;

import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.google.common.io.Resources;

import java.io.File;
import java.io.IOException;

public class MetadataBuilder {

    String credential_issuer; // NOSONAR
    String authorization_server; // NOSONAR
    String credentials_endpoint; // NOSONAR
    Object credentials_supported; // NOSONAR

    public MetadataBuilder() {}

    @JsonProperty("credential_issuer")
    public MetadataBuilder setCredentialIssuer(String credential_issuer)
            throws IllegalArgumentException {
        if (credential_issuer == null) {
            throw new IllegalArgumentException("credential_issuer must not be null");
        }
        this.credential_issuer = credential_issuer;
        return this;
    }

    @JsonProperty("authorization_server")
    public MetadataBuilder setAuthorizationServer(String authorization_server)
            throws IllegalArgumentException {
        if (authorization_server == null) {
            throw new IllegalArgumentException("authorization_server must not be null");
        }
        this.authorization_server = authorization_server;
        return this;
    }

    @JsonProperty("credentials_endpoint")
    public MetadataBuilder setCredentialsEndpoint(String credentials_endpoint)
            throws IllegalArgumentException {
        if (credentials_endpoint == null) {
            throw new IllegalArgumentException("credentials_endpoint must not be null");
        }
        this.credentials_endpoint = credentials_endpoint;
        return this;
    }

    @JsonProperty("credentials_supported")
    public MetadataBuilder setCredentialsSupported(String fileName)
            throws IOException, IllegalArgumentException {
        if (fileName == null) {
            throw new IllegalArgumentException("fileName must not be null");
        }
        File credentialsSupportedFilePath = new File(Resources.getResource(fileName).getPath());
        ObjectMapper mapper = new ObjectMapper();
        this.credentials_supported = mapper.readValue(credentialsSupportedFilePath, Object.class);
        return this;
    }

    public Metadata build() {
        return new Metadata(
                credential_issuer,
                authorization_server,
                credentials_endpoint,
                credentials_supported);
    }
}
