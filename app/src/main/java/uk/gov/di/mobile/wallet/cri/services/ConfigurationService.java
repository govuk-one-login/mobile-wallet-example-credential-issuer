package uk.gov.di.mobile.wallet.cri.services;

import io.dropwizard.core.Configuration;

public class ConfigurationService extends Configuration {

    public String getSigningKeyAlias() {
        return System.getenv().getOrDefault("SIGNING_KEY_ALIAS", "alias/localSigningKeyAlias");
    }

    public String getSigningKid() {
        return System.getenv().getOrDefault("SIGNING_KID", "ff275b92-0def-4dfc-b0f6-87c96b26c6c7");
    }

    public String getAwsRegion() {
        return System.getenv().getOrDefault("AWS_REGION", "eu-west-2");
    }

    public String getEnvironment() {
        return System.getenv().getOrDefault("ENVIRONMENT", "local");
    }

    public String getMockCriUri() {
        return System.getenv()
                .getOrDefault("MOCK_CRI_URI", "https://credential-issuer.example.com");
    }

    public long getPreAuthorizedCodeTtl() {
        return Long.parseLong(
                System.getenv().getOrDefault("PRE_AUTHORIZED_CODE_TTL_IN_SECS", "300"));
    }

    public String getClientId() {
        return System.getenv().getOrDefault("CLIENT_ID", "abc123");
    }

    public String getCriCacheTableName() {
        return System.getenv().getOrDefault("CRI_CACHE_TABLE_NAME", "CRI_CACHE");
    }

    public String getIssuer() {
        return System.getenv().getOrDefault("ISSUER", "urn:fdc:gov:uk:<HMRC>");
    }

    public String getAudience() {
        return System.getenv().getOrDefault("AUDIENCE", "urn:fdc:gov:uk:wallet");
    }

    public String getLocalstackEndpoint() {
        return "http://localhost:4566";
    }
    ;

    public String[] getCredentialTypes() {
        return new String[] {"BasicDisclosure"};
    }
    ;
}
