package uk.gov.di.mobile.wallet.cri.services;

import com.fasterxml.jackson.annotation.JsonProperty;
import io.dropwizard.client.HttpClientConfiguration;
import io.dropwizard.core.Configuration;

import java.net.URI;

/**
 * Configuration service for managing application settings. Provides environment variable-based
 * configuration with defaults for local development.
 */
public class ConfigurationService extends Configuration {

    private HttpClientConfiguration httpClient = new HttpClientConfiguration();

    @JsonProperty("httpClient")
    public HttpClientConfiguration getHttpClient() {
        return httpClient;
    }

    @JsonProperty("httpClient")
    public void setHttpClient(HttpClientConfiguration httpClient) {
        this.httpClient = httpClient;
    }

    /**
     * Gets the environment name (local, dev, build, staging).
     *
     * @return The current environment name
     */
    public String getEnvironment() {
        return getEnvOrDefault("ENVIRONMENT", "local");
    }

    // ===========================================
    // AWS
    // ===========================================
    /**
     * Gets the AWS region for all AWS service calls.
     *
     * @return The AWS region
     */
    public String getAwsRegion() {
        return getEnvOrDefault("AWS_REGION", "eu-west-2");
    }

    /**
     * Gets the LocalStack endpoint URL for local development. This is hardcoded as it's only used
     * in local development environments.
     *
     * @return The LocalStack endpoint URL
     */
    public String getLocalstackEndpoint() {
        return "http://localhost:4560";
    }

    /**
     * Gets the DynamoDB table name for credential offer caching.
     *
     * @return The DynamoDB table name
     */
    public String getCredentialOfferCacheTableName() {
        return getEnvOrDefault("CREDENTIAL_OFFER_CACHE", "credential_offer_cache");
    }

    /**
     * Gets the DynamoDB table name for stored credential.
     *
     * @return The DynamoDB table name
     */
    public String getCredentialStoreTableName() {
        return getEnvOrDefault("CREDENTIAL_STORE", "credential_store");
    }

    /**
     * Gets the alias of the signing key.
     *
     * @return The signing key alias
     */
    public String getSigningKeyAlias() {
        return getEnvOrDefault("SIGNING_KEY_ALIAS", "alias/localSigningKeyAlias");
    }

    /**
     * Gets the ARN of the document signing key.
     *
     * @return The document signing key ARN
     */
    public String getDocumentSigningKey1Arn() {
        return getEnvOrDefault(
                "DOCUMENT_SIGNING_KEY_1_ARN",
                "arn:aws:kms:eu-west-2:000000000000:key/1291b7bc-3d2c-47f0-a52a-cb6cb0fba6b4");
    }

    /**
     * Gets the ARN of the certificate authority used for certificate generation.
     *
     * @return The certificate authority ARN
     */
    public String getCertificateAuthorityArn() {
        return getEnvOrDefault(
                "CERTIFICATE_AUTHORITY_ARN",
                "arn:aws:acm-pca:eu-west-2:000000000000:certificate-authority/6bb42872-f4ed-4d55-a937-b8ffb8760de4");
    }

    /**
     * Gets the S3 bucket name for storing root and document signing certificates.
     *
     * @return The S3 bucket name
     */
    public String getCertificatesBucketName() {
        return getEnvOrDefault("CERTIFICATES_BUCKET_NAME", "certificates");
    }

    // ===========================================
    // AUTHENTICATION
    // ===========================================
    /**
     * Gets the OIDC client ID for authentication.
     *
     * @return The OIDC client identifier
     */
    public String getOIDCClientId() {
        return getEnvOrDefault("OIDC_CLIENT_ID", "TEST_CLIENT_ID");
    }

    /**
     * Gets the One Login authentication server URL.
     *
     * @return The authentication server URL as a string
     */
    public String getOneLoginAuthServerUrl() {
        return createValidatedUri("ONE_LOGIN_AUTH_SERVER_URL", "http://localhost:8001").toString();
    }

    // ===========================================
    // ENDPOINTS AND URLS
    // ===========================================
    /**
     * Gets this service's own URL.
     *
     * @return The service's own URL as a string
     */
    public String getSelfUrl() {
        return createValidatedUri("SELF_URL", "http://localhost:8080").toString();
    }

    /**
     * Gets the DID controller.
     *
     * @return The DID controller
     */
    public String getDidController() {
        return getEnvOrDefault("DID_CONTROLLER", "localhost:8080");
    }

    /**
     * Gets the wallet mobile app deep link URL.
     *
     * @return The wallet deep link URL as a string
     */
    public String getWalletDeepLinkUrl() {
        return createValidatedUri(
                        "WALLET_APP_DEEP_LINK_URL", "https://mobile.account.gov.uk/wallet")
                .toString();
    }

    /**
     * Gets the credential store service URL.
     *
     * @return The credential store URL as a URI object
     */
    public URI getCredentialStoreUrl() {
        return createValidatedUri("CREDENTIAL_STORE_URL", "http://localhost:8001");
    }

    /**
     * Gets the document endpoint path.
     *
     * @return The document endpoint path
     */
    public String getDocumentEndpoint() {
        return "/document/";
    }

    /**
     * Gets the JWKS (JSON Web Key Set) endpoint path.
     *
     * @return The JWKS endpoint path
     */
    public String getJwksEndpoint() {
        return "/.well-known/jwks.json";
    }

    // ===========================================
    // TTL CONFIGURATION
    // ===========================================
    /**
     * Gets the TTL for pre-authorized codes in seconds. Pre-authorized codes are short-lived tokens
     * used in the credential issuance flow.
     *
     * @return The TTL in seconds
     */
    public int getPreAuthorizedCodeTtlInSecs() {
        return 300;
    }

    /**
     * Gets the TTL for credential offers in seconds. Credential offers should last for a limited
     * time to prevent misuse. 15 minutes (900 seconds) has been chosen but may need adjustment
     * based on user testing.
     *
     * @return The TTL in seconds
     */
    public int getCredentialOfferTtlInSecs() {
        return 900;
    }

    /**
     * Gets the TTL for DynamoDB table items in days. Items are automatically deleted after this
     * period to prevent data buildup.
     *
     * @return The TTL in days
     */
    public int getTableItemTtlInDays() {
        return 3;
    }

    // ===========================================
    // STATUS LIST
    // ===========================================
    /**
     * Gets this CRI's status list client ID.
     *
     * @return The client identifier
     */
    public String getStatusListClientId() {
        return getEnvOrDefault("STATUS_LIST_CLIENT_ID", "STATUS_LIST_TEST_CLIENT_ID");
    }

    /**
     * Gets the status list URL.
     *
     * <p>Note: Currently, there is no default value for STATUS_LIST_URL. This is temporary — once a
     * default URL becomes available (i.e. when this ticket is completed <a
     * href="https://govukverify.atlassian.net/browse/DCMAW-15575">...</a>), it should be added
     * here.
     *
     * @return The status list URL as a string
     * @throws IllegalArgumentException if STATUS_LIST_URL is missing or invalid
     */
    public URI getStatusListUrl() {
        String key = "STATUS_LIST_URL";
        String uriString = getEnvOrDefault(key, null);
        if (uriString == null || uriString.isBlank()) {
            throw new IllegalArgumentException("Missing required environment variable: " + key);
        }
        return createValidatedUri(key, uriString);
    }

    // ===========================================
    // HELPER METHODS
    // ===========================================
    /**
     * Helper method to get environment variable or return default value.
     *
     * @param key The environment variable key
     * @param defaultValue The default value if environment variable is not set
     * @return The environment variable value or default
     */
    private String getEnvOrDefault(String key, String defaultValue) {
        return System.getenv().getOrDefault(key, defaultValue);
    }

    /**
     * Helper method to create and validate URI from environment variable.
     *
     * @param key The environment variable key
     * @param defaultValue The default URI string
     * @return A validated URI object
     * @throws IllegalArgumentException if the URI is invalid
     */
    private URI createValidatedUri(String key, String defaultValue) {
        String uriString = getEnvOrDefault(key, defaultValue);
        try {
            return URI.create(uriString);
        } catch (IllegalArgumentException exception) {
            throw new IllegalArgumentException(
                    "Invalid URI for " + key + ": " + uriString, exception);
        }
    }
}
