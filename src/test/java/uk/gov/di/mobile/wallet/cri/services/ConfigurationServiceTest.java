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
    void testSigningKeyAliasIsEqualDefaultWhenEnvVarUnset() {
        assertEquals("alias/localSigningKeyAlias", configurationService.getSigningKeyAlias());
    }

    @Test
    void testAwsRegionIsEqualDefaultWhenEnvVarUnset() {
        ConfigurationService configurationService = new ConfigurationService();
        assertEquals("eu-west-2", configurationService.getAwsRegion());
    }

    @Test
    void testEnvironmentIsEqualDefaultWhenEnvVarUnset() {
        ConfigurationService configurationService = new ConfigurationService();
        assertEquals("local", configurationService.getEnvironment());
    }

    @Test
    void testExampleCriUrlIsEqualDefaultWhenEnvVarUnset() {
        ConfigurationService configurationService = new ConfigurationService();
        assertEquals("http://localhost:8080", configurationService.getExampleCriUrl());
    }

    @Test
    void testStsStubUrlIsEqualDefaultWhenEnvVarUnset() {
        ConfigurationService configurationService = new ConfigurationService();
        assertEquals("http://localhost:8000/sts-stub", configurationService.getStsStubUrl());
    }

    @Test
    void testWalletUrlIsEqualDefaultWhenEnvVarUnset() {
        ConfigurationService configurationService = new ConfigurationService();
        assertEquals("https://mobile.account.gov.uk/wallet", configurationService.getWalletUrl());
    }

    @Test
    void testPreAuthorizedCodeTtlIsEqualDefaultWhenEnvVarUnset() {
        ConfigurationService configurationService = new ConfigurationService();
        assertEquals(300, configurationService.getPreAuthorizedCodeTtl());
    }

    @Test
    void testCriCacheTableNameIsEqualDefaultWhenEnvVarUnset() {
        ConfigurationService configurationService = new ConfigurationService();
        assertEquals("cri_cache", configurationService.getCriCacheTableName());
    }

    @Test
    void testItGetsSigningKeyAliasEnvironmentVariable() {
        environmentVariables.set("SIGNING_KEY_ALIAS", "alias/testSigningKeyAlias");
        assertEquals("alias/testSigningKeyAlias", configurationService.getSigningKeyAlias());
    }

    @Test
    void testItGetsAwsRegionEnvironmentVariable() {
        environmentVariables.set("AWS_REGION", "eu-west-1");
        assertEquals("eu-west-1", configurationService.getAwsRegion());
    }

    @Test
    void testItGetsEnvironmentEnvironmentVariable() {
        environmentVariables.set("ENVIRONMENT", "test");
        assertEquals("test", configurationService.getEnvironment());
    }

    @Test
    void testItGetsExampleCriUrlEnvironmentVariable() {
        environmentVariables.set(
                "EXAMPLE_CRI_URL", "https://example-credential-issuer.mobile.test.account.gov.uk");
        assertEquals(
                "https://example-credential-issuer.mobile.test.account.gov.uk",
                configurationService.getExampleCriUrl());
    }

    @Test
    void testItGetsStsStubUrlEnvironmentVariable() {
        environmentVariables.set("STS_STUB_URL", "https://credential-builder.test.com/sts-stub");

        assertEquals(
                "https://credential-builder.test.com/sts-stub",
                configurationService.getStsStubUrl());
    }

    @Test
    void testItGetsWalletUrlEnvironmentVariable() {
        environmentVariables.set("WALLET_URL", "https://mobile.test.account.gov.uk/wallet");
        assertEquals(
                "https://mobile.test.account.gov.uk/wallet", configurationService.getWalletUrl());
    }

    @Test
    void testItGetsPreAuthorizedCodeTtlEnvironmentVariable() {
        environmentVariables.set("PRE_AUTHORIZED_CODE_TTL_IN_SECS", "600");
        assertEquals(600, configurationService.getPreAuthorizedCodeTtl());
    }

    @Test
    void testItGetsClientIdValue() {
        assertEquals("EXAMPLE_CRI", configurationService.getClientId());
    }

    @Test
    void testItGetsCriCacheTableNameEnvironmentVariable() {
        environmentVariables.set("CRI_CACHE_TABLE_NAME", "test-table-name");
        assertEquals("test-table-name", configurationService.getCriCacheTableName());
    }

    @Test
    void testItGetsIssuerValue() {
        assertEquals("urn:fdc:gov:uk:example-credential-issuer", configurationService.getIssuer());
    }

    @Test
    void testItGetsAudienceValue() {
        assertEquals("urn:fdc:gov:uk:wallet", configurationService.getAudience());
    }

    @Test
    void testItGetsLocalstackEndpointValue() {
        assertEquals("http://localhost:4560", configurationService.getLocalstackEndpoint());
    }
}
