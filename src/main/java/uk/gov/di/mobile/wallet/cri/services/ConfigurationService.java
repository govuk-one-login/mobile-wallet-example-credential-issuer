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

    public String getExampleCriUrl() {
        return System.getenv().getOrDefault("EXAMPLE_CRI_URL", "http://localhost:8080");
    }

    public String getDidController() {
        return System.getenv().getOrDefault("DID_CONTROLLER", "localhost:8080");
    }

    public String getOneLoginAuthServerUrl() {
        return System.getenv()
                .getOrDefault("ONE_LOGIN_AUTH_SERVER_URL", "http://localhost:8000/sts-stub");
    }

    public String getCredentialStoreUrl() {
        return System.getenv().getOrDefault("CREDENTIAL_STORE_URL", "http://localhost:8000");
    }

    public String getWalletUrl() {
        return System.getenv().getOrDefault("WALLET_URL", "https://mobile.account.gov.uk/wallet");
    }

    public long getPreAuthorizedCodeTtl() {
        return Long.parseLong(
                System.getenv().getOrDefault("PRE_AUTHORIZED_CODE_TTL_IN_SECS", "300"));
    }

    public long getCredentialTtl() {
        return Long.parseLong(System.getenv().getOrDefault("CREDENTIAL_TTL_IN_DAYS", "365"));
    }

    public String getCredentialOfferCacheTableName() {
        return System.getenv().getOrDefault("CREDENTIAL_OFFER_CACHE", "credential_offer_cache");
    }

    public String getClientId() {
        return "EXAMPLE_CRI";
    }

    public String getIssuer() {
        return "urn:fdc:gov:uk:example-credential-issuer";
    }

    public String getAudience() {
        return "urn:fdc:gov:uk:wallet";
    }

    public String getLocalstackEndpoint() {
        return "http://localhost:4560";
    }

    public String getKeyIdHashingAlgorithm() {
        return "SHA-256";
    }

    public String getAuthServerDidDocumentPath() {
        return "/.well-known/did.json";
    }

    public String getCredentialStoreDocumentPath() {
        return "/document/";
    }
}
