package uk.gov.di.mobile.wallet.cri.services;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import uk.org.webcompere.systemstubs.environment.EnvironmentVariables;
import uk.org.webcompere.systemstubs.jupiter.SystemStub;
import uk.org.webcompere.systemstubs.jupiter.SystemStubsExtension;

import static org.junit.jupiter.api.Assertions.assertArrayEquals;
import static org.junit.jupiter.api.Assertions.assertEquals;

@ExtendWith({SystemStubsExtension.class})
class ConfigurationServiceTest {

    ConfigurationService configurationService;

    @SystemStub private EnvironmentVariables environmentVariables;

    @BeforeEach
    void setUp() {
        configurationService = new ConfigurationService();
    }

    @Test
    void signingKeyAliasShouldEqualDefaultWhenEnvVarUnset() {
        ConfigurationService configurationService = new ConfigurationService();
        assertEquals("alias/localSigningKeyAlias", configurationService.getSigningKeyAlias());
    }

    @Test
    void awsRegionShouldEqualDefaultWhenEnvVarUnset() {
        ConfigurationService configurationService = new ConfigurationService();
        assertEquals("eu-west-2", configurationService.getAwsRegion());
    }

    @Test
    void environmentShouldEqualDefaultWhenEnvVarUnset() {
        ConfigurationService configurationService = new ConfigurationService();
        assertEquals("local", configurationService.getEnvironment());
    }

    @Test
    void mockCriUrlShouldEqualDefaultWhenEnvVarUnset() {
        ConfigurationService configurationService = new ConfigurationService();
        assertEquals("https://credential-issuer.example.com", configurationService.getMockCriUrl());
    }

    @Test
    void wallerUrlShouldEqualDefaultWhenEnvVarUnset() {
        ConfigurationService configurationService = new ConfigurationService();
        assertEquals(
                "https://mobile.staging.account.gov.uk/wallet",
                configurationService.getWalletUrl());
    }

    @Test
    void preAuthorizedCodeTtlShouldEqualDefaultWhenEnvVarUnset() {
        ConfigurationService configurationService = new ConfigurationService();
        assertEquals(300, configurationService.getPreAuthorizedCodeTtl());
    }

    @Test
    void clientIdShouldEqualDefaultWhenEnvVarUnset() {
        ConfigurationService configurationService = new ConfigurationService();
        assertEquals("abc123", configurationService.getClientId());
    }

    @Test
    void criCacheTableNameShouldEqualDefaultWhenEnvVarUnset() {
        ConfigurationService configurationService = new ConfigurationService();
        assertEquals("cri_cache", configurationService.getCriCacheTableName());
    }

    @Test
    void issuerShouldEqualDefaultWhenEnvVarUnset() {
        ConfigurationService configurationService = new ConfigurationService();
        assertEquals("urn:fdc:gov:uk:<HMRC>", configurationService.getIssuer());
    }

    @Test
    void audienceShouldEqualDefaultWhenEnvVarUnset() {
        ConfigurationService configurationService = new ConfigurationService();
        assertEquals("urn:fdc:gov:uk:wallet", configurationService.getAudience());
    }

    @Test
    void localstackEndpointShouldEqualReturnValue() {
        ConfigurationService configurationService = new ConfigurationService();
        assertEquals("http://localhost:4566", configurationService.getLocalstackEndpoint());
    }

    @Test
    void credentialTypesShouldEqualReturnValue() {
        ConfigurationService configurationService = new ConfigurationService();
        assertArrayEquals(
                new String[] {"BasicDisclosure"}, configurationService.getCredentialTypes());
    }

    @Test
    void getSigningKeyAliasEnvironmentVariable() {
        environmentVariables.set("SIGNING_KEY_ALIAS", "alias/testSigningKeyAlias");

        assertEquals("alias/testSigningKeyAlias", configurationService.getSigningKeyAlias());
    }

    @Test
    void getAwsRegionEnvironmentVariable() {
        environmentVariables.set("AWS_REGION", "eu-west-1");

        assertEquals("eu-west-1", configurationService.getAwsRegion());
    }

    @Test
    void getEnvironmentEnvironmentVariable() {
        environmentVariables.set("ENVIRONMENT", "test");

        assertEquals("test", configurationService.getEnvironment());
    }

    @Test
    void getMockCriUrlEnvironmentVariable() {
        environmentVariables.set("MOCK_CRI_URL", "https://credential-issuer.test.example.com");

        assertEquals(
                "https://credential-issuer.test.example.com", configurationService.getMockCriUrl());
    }

    @Test
    void getWalletUrlEnvironmentVariable() {
        environmentVariables.set("WALLET_URL", "https://mobile.test.account.gov.uk/wallet");

        assertEquals(
                "https://mobile.test.account.gov.uk/wallet", configurationService.getWalletUrl());
    }

    @Test
    void getPreAuthorizedCodeTtlEnvironmentVariable() {
        environmentVariables.set("PRE_AUTHORIZED_CODE_TTL_IN_SECS", "600");

        assertEquals(600, configurationService.getPreAuthorizedCodeTtl());
    }

    @Test
    void getClientIdEnvironmentVariable() {
        environmentVariables.set("CLIENT_ID", "test-client-id");

        assertEquals("test-client-id", configurationService.getClientId());
    }

    @Test
    void getCriCacheTableNameEnvironmentVariable() {
        environmentVariables.set("CRI_CACHE_TABLE_NAME", "test-table-name");

        assertEquals("test-table-name", configurationService.getCriCacheTableName());
    }

    @Test
    void getIssuerEnvironmentVariable() {
        environmentVariables.set("ISSUER", "urn:fdc:gov:uk:<TEST>");

        assertEquals("urn:fdc:gov:uk:<TEST>", configurationService.getIssuer());
    }

    @Test
    void getAudienceEnvironmentVariable() {
        environmentVariables.set("AUDIENCE", "urn:fdc:gov:uk:test-wallet");

        assertEquals("urn:fdc:gov:uk:test-wallet", configurationService.getAudience());
    }
}
