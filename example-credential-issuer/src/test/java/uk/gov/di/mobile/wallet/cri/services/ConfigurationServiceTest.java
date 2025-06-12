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
    void should_Get_SigningKeyAlias_Default_Value_When_EnvVar_Unset() {
        assertEquals("alias/localSigningKeyAlias", configurationService.getSigningKeyAlias());
    }

    @Test
    void should_Get_SigningKeyAlias_EnvVar_Value() {
        environmentVariables.set("SIGNING_KEY_ALIAS", "alias/testSigningKeyAlias-1");
        assertEquals("alias/testSigningKeyAlias-1", configurationService.getSigningKeyAlias());
    }

    @Test
    void should_Get_AwsRegion_Default_Value_When_EnvVar_Unset() {
        assertEquals("eu-west-2", configurationService.getAwsRegion());
    }

    @Test
    void should_Get_AwsRegion_EnvVar_Value() {
        environmentVariables.set("AWS_REGION", "eu-west-1");
        assertEquals("eu-west-1", configurationService.getAwsRegion());
    }

    @Test
    void should_Get_Environment_Default_Value_When_EnvVar_Unset() {
        assertEquals("local", configurationService.getEnvironment());
    }

    @Test
    void should_Get_Environment_EnvVar_Value() {
        environmentVariables.set("ENVIRONMENT", "test");
        assertEquals("test", configurationService.getEnvironment());
    }

    @Test
    void should_Get_ExampleCriUrl_Default_Value_When_EnvVar_Unset() {
        assertEquals("http://localhost:8080", configurationService.getSelfUrl());
    }

    @Test
    void should_Get_SelfUrl_EnvVar_Value() {
        environmentVariables.set(
                "SELF_URL", "https://example-credential-issuer.mobile.test.account.gov.uk");
        assertEquals(
                "https://example-credential-issuer.mobile.test.account.gov.uk",
                configurationService.getSelfUrl());
    }

    @Test
    void should_Get_DidController_Default_Value_When_EnvVar_Unset() {
        assertEquals("localhost:8080", configurationService.getDidController());
    }

    @Test
    void should_Get_DidController_EnvVar_Value() {
        environmentVariables.set("DID_CONTROLLER", "https://example-credential-issuer.gov.uk");
        assertEquals(
                "https://example-credential-issuer.gov.uk",
                configurationService.getDidController());
    }

    @Test
    void should_Get_OneLogin_AuthServerUrl_Default_Value_When_EnvVar_Unset() {
        assertEquals("http://localhost:8001", configurationService.getOneLoginAuthServerUrl());
    }

    @Test
    void should_Get_OneLogin_AuthServerUrl_EnvVar_Value() {
        environmentVariables.set(
                "ONE_LOGIN_AUTH_SERVER_URL", "https://credential-store.com/auth-server");
        assertEquals(
                "https://credential-store.com/auth-server",
                configurationService.getOneLoginAuthServerUrl());
    }

    @Test
    void should_Get_CredentialStoreUrl_Default_Value_When_EnvVar_Unset() {
        assertEquals("http://localhost:8001", configurationService.getCredentialStoreUrl());
    }

    @Test
    void should_Get_CredentialStoreUrl_EnvVar_Value() {
        environmentVariables.set("CREDENTIAL_STORE_URL", "https://credential-store.test.com");
        assertEquals(
                "https://credential-store.test.com", configurationService.getCredentialStoreUrl());
    }

    @Test
    void should_Get_WalletUrl_Default_Value_When_EnvVar_Unset() {
        assertEquals(
                "https://mobile.account.gov.uk/wallet",
                configurationService.getWalletDeepLinkUrl());
    }

    @Test
    void should_Get_WalletUrl_EnvVar_Value() {
        environmentVariables.set(
                "WALLET_APP_DEEP_LINK_URL", "https://mobile.test.account.gov.uk/wallet");
        assertEquals(
                "https://mobile.test.account.gov.uk/wallet",
                configurationService.getWalletDeepLinkUrl());
    }

    @Test
    void should_Get_CredentialOffer_CacheTableName_Default_Value_When_EnvVar_Unset() {
        assertEquals(
                "credential_offer_cache", configurationService.getCredentialOfferCacheTableName());
    }

    @Test
    void should_Get_CredentialOffer_CacheTableName_EnvVar_Value() {
        environmentVariables.set("CREDENTIAL_OFFER_CACHE", "test-table-name");
        assertEquals("test-table-name", configurationService.getCredentialOfferCacheTableName());
    }

    @Test
    void should_Get_ClientId_Value() {
        assertEquals("TEST_CLIENT_ID", configurationService.getClientId());
    }

    @Test
    void should_Get_Localstack_Endpoint_Value() {
        assertEquals("http://localhost:4560", configurationService.getLocalstackEndpoint());
    }

    @Test
    void should_Get_PreAuthCode_Ttl_Value() {
        assertEquals(300, configurationService.getPreAuthorizedCodeTtlInSecs());
    }

    @Test
    void should_Get_Credential_Ttl_Value() {
        assertEquals(365, configurationService.getCredentialTtlInDays());
    }

    @Test
    void should_Get_CredentialOffer_Ttl_Value() {
        assertEquals(900, configurationService.getCredentialOfferTtlInSecs());
    }

    @Test
    void should_Get_Document_Endpoint() {
        assertEquals("/document/", configurationService.getDocumentEndpoint());
    }
}
