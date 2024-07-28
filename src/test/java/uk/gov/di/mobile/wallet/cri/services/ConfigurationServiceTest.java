package uk.gov.di.mobile.wallet.cri.services;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import uk.org.webcompere.systemstubs.environment.EnvironmentVariables;
import uk.org.webcompere.systemstubs.jupiter.SystemStub;
import uk.org.webcompere.systemstubs.jupiter.SystemStubsExtension;

import static org.junit.jupiter.api.Assertions.assertEquals;

@ExtendWith({SystemStubsExtension.class})
class ConfigurationServiceTest {

    private ConfigurationService configurationService;

    @SystemStub private EnvironmentVariables environmentVariables;

    @BeforeEach
    void setUp() {
        configurationService = new ConfigurationService();
    }

    @Test
    void shouldGetSigningKeyAliasDefaultValueWhenEnvVarUnset() {
        assertEquals("alias/localSigningKeyAlias", configurationService.getSigningKeyAlias());
    }

    @Test
    void shouldGetSigningKeyAliasEnvVarValue() {
        environmentVariables.set("SIGNING_KEY_ALIAS", "alias/testSigningKeyAlias-1");
        assertEquals("alias/testSigningKeyAlias-1", configurationService.getSigningKeyAlias());
    }

    @Test
    void shouldGetAwsRegionDefaultValueWhenEnvVarUnset() {
        assertEquals("eu-west-2", configurationService.getAwsRegion());
    }

    @Test
    void shouldGetAwsRegionEnvVarValue() {
        environmentVariables.set("AWS_REGION", "eu-west-1");
        assertEquals("eu-west-1", configurationService.getAwsRegion());
    }

    @Test
    void shouldGetEnvironmentDefaultValueWhenEnvVarUnset() {
        assertEquals("local", configurationService.getEnvironment());
    }

    @Test
    void shouldGetEnvironmentEnvVarValue() {
        environmentVariables.set("ENVIRONMENT", "test");
        assertEquals("test", configurationService.getEnvironment());
    }

    @Test
    void shouldGetExampleCriUrlDefaultValueWhenEnvVarUnset() {
        assertEquals("http://localhost:8080", configurationService.getExampleCriUrl());
    }

    @Test
    void shouldGetExampleCriUrlEnvVarValue() {
        environmentVariables.set(
                "EXAMPLE_CRI_URL", "https://example-credential-issuer.mobile.test.account.gov.uk");
        assertEquals(
                "https://example-credential-issuer.mobile.test.account.gov.uk",
                configurationService.getExampleCriUrl());
    }

    @Test
    void shouldGetDidControllerDefaultValueWhenEnvVarUnset() {
        assertEquals("localhost:8080", configurationService.getDidController());
    }

    @Test
    void shouldGetDidControllerEnvVarValue() {
        environmentVariables.set("DID_CONTROLLER", "https://example-credential-issuer.gov.uk");
        assertEquals(
                "https://example-credential-issuer.gov.uk",
                configurationService.getDidController());
    }

    @Test
    void shouldGetOneLoginAuthServerUrlDefaultValueWhenEnvVarUnset() {
        assertEquals("http://localhost:8888", configurationService.getOneLoginAuthServerUrl());
    }

    @Test
    void shouldGetOneLoginAuthServerUrlEnvVarValue() {
        environmentVariables.set(
                "ONE_LOGIN_AUTH_SERVER_URL", "https://credential-store.com/auth-server");
        assertEquals(
                "https://credential-store.com/auth-server",
                configurationService.getOneLoginAuthServerUrl());
    }

    @Test
    void shouldGetCredentialStoreUrlDefaultValueWhenEnvVarUnset() {
        assertEquals("http://localhost:8888", configurationService.getCredentialStoreUrl());
    }

    @Test
    void shouldGetCredentialStoreUrlEnvVarValue() {
        environmentVariables.set("CREDENTIAL_STORE_URL", "https://credential-store.test.com");
        assertEquals(
                "https://credential-store.test.com", configurationService.getCredentialStoreUrl());
    }

    @Test
    void shouldGetWalletUrlDefaultValueWhenEnvVarUnset() {
        assertEquals("https://mobile.account.gov.uk/wallet", configurationService.getWalletUrl());
    }

    @Test
    void shouldGetWalletUrlEnvVarValue() {
        environmentVariables.set("WALLET_URL", "https://mobile.test.account.gov.uk/wallet");
        assertEquals(
                "https://mobile.test.account.gov.uk/wallet", configurationService.getWalletUrl());
    }

    @Test
    void shouldGetCredentialOfferCacheTableNameDefaultValueWhenEnvVarUnset() {
        assertEquals(
                "credential_offer_cache", configurationService.getCredentialOfferCacheTableName());
    }

    @Test
    void shouldGetGetCredentialOfferCacheTableNameEnvVarValue() {
        environmentVariables.set("CREDENTIAL_OFFER_CACHE", "test-table-name");
        assertEquals("test-table-name", configurationService.getCredentialOfferCacheTableName());
    }

    @Test
    void shouldGetClientIdValue() {
        assertEquals("EXAMPLE_CRI", configurationService.getClientId());
    }

    @Test
    void shouldGetIssuerValue() {
        assertEquals("urn:fdc:gov:uk:example-credential-issuer", configurationService.getIssuer());
    }

    @Test
    void shouldGetAudienceValue() {
        assertEquals("urn:fdc:gov:uk:wallet", configurationService.getAudience());
    }

    @Test
    void shouldGetLocalstackEndpointValue() {
        assertEquals("http://localhost:4560", configurationService.getLocalstackEndpoint());
    }

    @Test
    void shouldGetAuthServerJwksPathValue() {
        assertEquals("/.well-known/jwks.json", configurationService.getAuthServerJwksPath());
    }

    @Test
    void shouldGetCredentialStoreDocumentPathValue() {
        assertEquals("/document/", configurationService.getCredentialStoreDocumentPath());
    }

    @Test
    void shouldGetPreAuthCodeTtlValue() {
        assertEquals(300, configurationService.getPreAuthorizedCodeTtl());
    }

    @Test
    void shouldGetCredentialTtlValue() {
        assertEquals(365, configurationService.getCredentialTtl());
    }
}
