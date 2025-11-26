package uk.gov.di.mobile.wallet.cri.services;

import io.dropwizard.client.JerseyClientConfiguration;
import io.dropwizard.util.Duration;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import uk.org.webcompere.systemstubs.environment.EnvironmentVariables;
import uk.org.webcompere.systemstubs.jupiter.SystemStub;
import uk.org.webcompere.systemstubs.jupiter.SystemStubsExtension;

import java.net.URI;
import java.net.URISyntaxException;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertThrows;

@ExtendWith({SystemStubsExtension.class})
class ConfigurationServiceTest {

    private ConfigurationService configurationService;

    @SystemStub private EnvironmentVariables environmentVariables;

    @BeforeEach
    void setUp() {
        configurationService = new ConfigurationService();
    }

    @Test
    void Should_HaveNonNullDefaultHttpClient_On_Initialisation() {
        ConfigurationService config = new ConfigurationService();
        assertNotNull(config.getHttpClient(), "Default httpClient should not be null");
    }

    @Test
    void Should_ReturnSameHttpClient_After_SettingCustomClient() {
        ConfigurationService config = new ConfigurationService();
        JerseyClientConfiguration customClient = new JerseyClientConfiguration();
        customClient.setTimeout(Duration.seconds(10));
        config.setHttpClient(customClient);
        assertEquals(
                customClient,
                config.getHttpClient(),
                "Should return the client instance set by setter");
    }

    @Test
    void Should_ReturnEnvironmentDefaultValue_When_EnvVarNotSet() {
        assertEquals("local", configurationService.getEnvironment());
    }

    @Test
    void Should_ReturnEnvironmentEnvVarValue() {
        environmentVariables.set("ENVIRONMENT", "test");
        assertEquals("test", configurationService.getEnvironment());
    }

    @Test
    void Should_ReturnAwsRegionDefaultValue_When_EnvVarNotSet() {
        assertEquals("eu-west-2", configurationService.getAwsRegion());
    }

    @Test
    void Should_ReturnAwsRegionEnvVarValue() {
        environmentVariables.set("AWS_REGION", "eu-west-1");
        assertEquals("eu-west-1", configurationService.getAwsRegion());
    }

    @Test
    void Should_ReturnLocalStackEndpointEnvVarValue() {
        assertEquals("http://localhost:4560", configurationService.getLocalstackEndpoint());
    }

    @Test
    void Should_ReturnCredentialOfferCacheTableNameDefaultValue_When_EnvVarNotSet() {
        assertEquals(
                "credential_offer_cache", configurationService.getCredentialOfferCacheTableName());
    }

    @Test
    void Should_ReturnCredentialOfferCacheTableNameEnvVarValue() {
        environmentVariables.set("CREDENTIAL_OFFER_CACHE", "test-table-name");
        assertEquals("test-table-name", configurationService.getCredentialOfferCacheTableName());
    }

    @Test
    void Should_ReturnSigningKeyAliasDefaultValue_When_EnvVarNotSet() {
        assertEquals("alias/localSigningKeyAlias", configurationService.getSigningKeyAlias());
    }

    @Test
    void Should_ReturnSigningKeyAliasEnvVarValue() {
        environmentVariables.set("SIGNING_KEY_ALIAS", "alias/testSigningKeyAlias-1");
        assertEquals("alias/testSigningKeyAlias-1", configurationService.getSigningKeyAlias());
    }

    @Test
    void Should_ReturnDocumentSigningKeyArnDefaultValue_When_EnvVarNotSet() {
        assertEquals(
                "arn:aws:kms:eu-west-2:000000000000:key/1291b7bc-3d2c-47f0-a52a-cb6cb0fba6b4",
                configurationService.getDocumentSigningKey1Arn());
    }

    @Test
    void Should_ReturnDocumentSigningKeyArnEnvVarValue() {
        environmentVariables.set(
                "DOCUMENT_SIGNING_KEY_1_ARN",
                "arn:aws:kms:eu-west-2:000000000000:key/3211b7bc-3d2c-47f0-a52a-cb6cb0fba6f8");
        assertEquals(
                "arn:aws:kms:eu-west-2:000000000000:key/3211b7bc-3d2c-47f0-a52a-cb6cb0fba6f8",
                configurationService.getDocumentSigningKey1Arn());
    }

    @Test
    void Should_ReturnCertificateAuthorityArnDefaultValue_When_EnvVarNotSet() {
        assertEquals(
                "arn:aws:acm-pca:eu-west-2:000000000000:certificate-authority/6bb42872-f4ed-4d55-a937-b8ffb8760de4",
                configurationService.getCertificateAuthorityArn());
    }

    @Test
    void Should_ReturnCertificateAuthorityArnEnvVarValue() {
        environmentVariables.set(
                "CERTIFICATE_AUTHORITY_ARN",
                "arn:aws:acm-pca:eu-west-2:000000000000:certificate-authority/7cc42872-f4ed-4d55-a937-b8ffb8760dd7");
        assertEquals(
                "arn:aws:acm-pca:eu-west-2:000000000000:certificate-authority/7cc42872-f4ed-4d55-a937-b8ffb8760dd7",
                configurationService.getCertificateAuthorityArn());
    }

    @Test
    void Should_ReturnCertificateBucketNameDefaultValue_When_EnvVarNotSet() {
        assertEquals("certificates", configurationService.getCertificatesBucketName());
    }

    @Test
    void Should_ReturnCertificateBucketNameEnvVarValue() {
        environmentVariables.set("CERTIFICATES_BUCKET_NAME", "test-certificates-bucket-name");
        assertEquals(
                "test-certificates-bucket-name", configurationService.getCertificatesBucketName());
    }

    @Test
    void Should_ReturnClientIdDefaultValue_When_EnvVarNotSet() {
        assertEquals("TEST_CLIENT_ID", configurationService.getOIDCClientId());
    }

    @Test
    void Should_ReturnClientIdEnvVarValue() {
        environmentVariables.set("OIDC_CLIENT_ID", "test-client-id");
        assertEquals("test-client-id", configurationService.getOIDCClientId());
    }

    @Test
    void Should_ReturnAuthServerUrlDefaultValue_When_EnvVarNotSet() {
        assertEquals("http://localhost:8001", configurationService.getOneLoginAuthServerUrl());
    }

    @Test
    void Should_ReturnAuthServerUrlEnvVarValue() {
        environmentVariables.set(
                "ONE_LOGIN_AUTH_SERVER_URL", "https://credential-store.com/auth-server");
        assertEquals(
                "https://credential-store.com/auth-server",
                configurationService.getOneLoginAuthServerUrl());
    }

    @Test
    void Should_ReturnExampleCriUrlDefaultValue_When_EnvVarNotSet() {
        assertEquals("http://localhost:8080", configurationService.getSelfUrl());
    }

    @Test
    void Should_ReturnExampleCriUrlEnvVarValue() {
        environmentVariables.set(
                "SELF_URL", "https://example-credential-issuer.mobile.test.account.gov.uk");
        assertEquals(
                "https://example-credential-issuer.mobile.test.account.gov.uk",
                configurationService.getSelfUrl());
    }

    @Test
    void Should_ReturnDidControllerDefaultValue_When_EnvVarNotSet() {
        assertEquals("localhost:8080", configurationService.getDidController());
    }

    @Test
    void Should_ReturnDidControllerEnvVarValue() {
        environmentVariables.set("DID_CONTROLLER", "https://example-credential-issuer.gov.uk");
        assertEquals(
                "https://example-credential-issuer.gov.uk",
                configurationService.getDidController());
    }

    @Test
    void Should_ReturnWalletUrlDefaultValue_When_EnvVarNotSet() {
        assertEquals(
                "https://mobile.account.gov.uk/wallet",
                configurationService.getWalletDeepLinkUrl());
    }

    @Test
    void Should_ReturnWalletUrlEnvVarValue() {
        environmentVariables.set(
                "WALLET_APP_DEEP_LINK_URL", "https://mobile.test.account.gov.uk/wallet");
        assertEquals(
                "https://mobile.test.account.gov.uk/wallet",
                configurationService.getWalletDeepLinkUrl());
    }

    @Test
    void Should_ReturnCredentialStoreUrlDefaultValue_When_EnvVarNotSet() throws URISyntaxException {
        assertEquals(
                new URI("http://localhost:8001"), configurationService.getCredentialStoreUrl());
    }

    @Test
    void Should_ReturnCredentialStoreUrlEnvVarValue() throws URISyntaxException {
        environmentVariables.set("CREDENTIAL_STORE_URL", "https://credential-store.test.com");
        assertEquals(
                new URI("https://credential-store.test.com"),
                configurationService.getCredentialStoreUrl());
    }

    @Test
    void Should_ReturnDocumentEndpoint() {
        assertEquals("/document/", configurationService.getDocumentEndpoint());
    }

    @Test
    void Should_ReturnJwksEndpoint() {
        assertEquals("/.well-known/jwks.json", configurationService.getJwksEndpoint());
    }

    @Test
    void Should_ReturnPreAuthorizedCodeTtl() {
        assertEquals(900, configurationService.getPreAuthorizedCodeTtlInSecs());
    }

    @Test
    void Should_ReturnCredentialOfferTtl() {
        assertEquals(900, configurationService.getCredentialOfferTtlInSecs());
    }

    @Test
    void Should_ReturnTableItemTtl() {
        assertEquals(3, configurationService.getTableItemTtlInDays());
    }

    @Test
    void Should_ReturnStatusListDefaultValue_When_EnvVarNotSet() throws URISyntaxException {
        assertEquals(new URI("http://localhost:3000"), configurationService.getStatusListUrl());
    }

    @Test
    void Should_ReturnStatusListEnvVarValue() throws URISyntaxException {
        environmentVariables.set("STATUS_LIST_URL", "https://status-list.test.com");
        assertEquals(
                new URI("https://status-list.test.com"), configurationService.getStatusListUrl());
    }

    @Test
    void Should_ThrowException_When_UrlIsInvalid() {
        environmentVariables.set("CREDENTIAL_STORE_URL", "invalid://uri with spaces");

        IllegalArgumentException exception =
                assertThrows(
                        IllegalArgumentException.class,
                        () -> configurationService.getCredentialStoreUrl());
        assertEquals(
                "Invalid URI for CREDENTIAL_STORE_URL: invalid://uri with spaces",
                exception.getMessage());
    }
}
