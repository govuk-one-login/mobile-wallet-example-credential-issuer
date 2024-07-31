package uk.gov.di.mobile.wallet.cri.services;

import io.dropwizard.core.Configuration;

public class ConfigurationService extends Configuration {

    public String getSigningKeyAlias() {
        return System.getenv().getOrDefault("SIGNING_KEY_ALIAS", "alias/localSigningKeyAlias");
    }

    public String getAwsRegion() {
        return System.getenv().getOrDefault("AWS_REGION", "eu-west-2");
    }

    public String getEnvironment() {
        return System.getenv().getOrDefault("ENVIRONMENT", "local");
    }

    public String getSelfUrl() {
        return System.getenv().getOrDefault("SELF_URL", "http://localhost:8080");
    }

    public String getDidController() {
        return System.getenv().getOrDefault("DID_CONTROLLER", "localhost:8080");
    }

    public String getOneLoginAuthServerUrl() {
        return System.getenv().getOrDefault("ONE_LOGIN_AUTH_SERVER_URL", "http://localhost:8888");
    }

    public String getCredentialStoreUrl() {
        return System.getenv().getOrDefault("CREDENTIAL_STORE_URL", "http://localhost:8888");
    }

    public String getWalletUrl() {
        return System.getenv().getOrDefault("WALLET_URL", "https://mobile.account.gov.uk/wallet");
    }

    public String getCredentialOfferCacheTableName() {
        return System.getenv().getOrDefault("CREDENTIAL_OFFER_CACHE", "credential_offer_cache");
    }

    public String getClientId() {
        return System.getenv().getOrDefault("OIDC_CLIENT_ID", "TEST_CLIENT_ID");
    }

    public String getIssuer() {
        return "urn:fdc:gov:uk:example-credential-issuer";
    }

    public String getLocalstackEndpoint() {
        return "http://localhost:4560";
    }

    public String getAuthServerJwksPath() {
        return "/.well-known/jwks.json";
    }

    public String getCredentialStoreDocumentPath() {
        return "/document/";
    }

    public long getPreAuthorizedCodeTtl() {
        return 300;
    }

    public long getCredentialTtl() {
        return 365;
    }
}
