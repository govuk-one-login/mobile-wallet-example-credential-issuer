package uk.gov.di.mobile.wallet.cri.services;

import io.dropwizard.core.Configuration;

import java.net.URI;

public class ConfigurationService extends Configuration {

    public String getSigningKeyAlias() {
        return System.getenv().getOrDefault("SIGNING_KEY_ALIAS", "signingKeyAlias");
    }
    public String getSigningKid() {
        return System.getenv().getOrDefault("SIGNING_KID", "signingKid");
    }

    public String getAwsRegion() {
        return System.getenv().getOrDefault("AWS_REGION", "eu-west-2");
    }

    public String getEnvironment() {
        return System.getenv().getOrDefault("ENVIRONMENT", "local");
    }

    public String getMockCriUri() {
        return System.getenv().getOrDefault("MOCK_CRI_URI", "");
    }

    public String getClientId() {
        return System.getenv().getOrDefault("CLIENT_ID", "abc123");
    }

    public String getIssuer() {
        return System.getenv().getOrDefault("ISSUER", "urn:fdc:gov:uk:<HMRC>");
    }

    public String getAudience() {
        return System.getenv().getOrDefault("AUDIENCE", "urn:fdc:gov:uk:wallet");
    }

    public String getLocalstackEndpoint() { return "http://localhost:4566"; };

    public String[] getCredentialTypes() { return new String[]{"BasicDisclosure"}; };



}



