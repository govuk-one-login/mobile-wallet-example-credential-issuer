package uk.gov.di.mobile.wallet.cri.metadata;

import com.fasterxml.jackson.core.JsonParseException;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.io.IOException;

import static org.junit.jupiter.api.Assertions.assertArrayEquals;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;

class MetadataBuilderTest {

    private MetadataBuilder metadataBuilder;

    @BeforeEach
    void setUp() {
        metadataBuilder = new MetadataBuilder();
    }

    @Test
    void Should_ReturnMetadata() throws IOException {
        String issuer = "https://test-credential-issuer.gov.uk";
        String credential = issuer + "/credential";
        String auth = "https://test-authorization-server.gov.uk/auth-server";
        String notification = issuer + "/notification";
        String iacas = issuer + "/iacas";

        Metadata metadata =
                metadataBuilder
                        .setCredentialIssuer(issuer)
                        .setCredentialEndpoint(credential)
                        .setAuthorizationServers(auth)
                        .setNotificationEndpoint(notification)
                        .setIacasEndpoint(iacas)
                        .setCredentialConfigurationsSupported(
                                "credential_configurations_supported.json")
                        .setDisplay(issuer + "/test-logo.png")
                        .build();

        assertEquals(issuer, metadata.credentialIssuer);
        assertArrayEquals(new String[] {auth}, metadata.authorizationServers);
        assertEquals(credential, metadata.credentialEndpoint);
        assertEquals(notification, metadata.notificationEndpoint);
        assertEquals(iacas, metadata.iacasEndpoint);
        String expectedCredentialConfigurationsSupported =
                "{SocialSecurityCredential={format=jwt_vc_json, credential_definition={type=[VerifiableCredential, SocialSecurityCredential]}, cryptographic_binding_methods_supported=[did:key], credential_signing_alg_values_supported=[ES256], proof_types_supported={jwt={proof_signing_alg_values_supported=[ES256]}}, credential_validity_period_max_days=30, credential_refresh_web_journey_url=https://test-credential-issuer.gov.uk/refresh/SocialSecurityCredential}, BasicDisclosureCredential={format=jwt_vc_json, credential_definition={type=[VerifiableCredential, BasicDisclosureCredential]}, cryptographic_binding_methods_supported=[did:key], credential_signing_alg_values_supported=[ES256], proof_types_supported={jwt={proof_signing_alg_values_supported=[ES256]}}, credential_validity_period_max_days=30, credential_refresh_web_journey_url=https://test-credential-issuer.gov.uk/refresh/BasicDisclosureCredential}, DigitalVeteranCard={format=jwt_vc_json, credential_definition={type=[VerifiableCredential, DigitalVeteranCard]}, cryptographic_binding_methods_supported=[did:key], credential_signing_alg_values_supported=[ES256], proof_types_supported={jwt={proof_signing_alg_values_supported=[ES256]}}, credential_validity_period_max_days=30, credential_refresh_web_journey_url=https://test-credential-issuer.gov.uk/refresh/DigitalVeteranCard}, org.iso.18013.5.1.mDL={format=mso_mdoc, doctype=org.iso.18013.5.1.mDL, cryptographic_binding_methods_supported=[cose_key], credential_signing_alg_values_supported=[ES256], credential_validity_period_max_days=30, credential_refresh_web_journey_url=https://test-credential-issuer.gov.uk/refresh/org.iso.18013.5.1.mDL}, uk.gov.account.mobile.example-credential-issuer.simplemdoc.1={format=mso_mdoc, doctype=uk.gov.account.mobile.example-credential-issuer.simplemdoc.1, cryptographic_binding_methods_supported=[cose_key], credential_signing_alg_values_supported=[ES256], credential_validity_period_max_days=30, credential_refresh_web_journey_url=https://test-credential-issuer.gov.uk/refresh/uk.gov.account.mobile.example-credential-issuer.simplemdoc.1}}";
        assertEquals(
                expectedCredentialConfigurationsSupported,
                metadata.credentialConfigurationsSupported.toString());
        String expectedDisplay =
                "[{name=GOV.UK Wallet Example Credential Issuer, logo={uri=https://test-credential-issuer.gov.uk/test-logo.png}, locale=en}, {name=ISSUER_NAME_WELSH, logo={uri=https://test-credential-issuer.gov.uk/test-logo.png}, locale=cy}]";
        assertEquals(expectedDisplay, metadata.display.toString());
    }

    @Test
    void Should_ThrowJsonParseException_When_JsonIsInvalid() {
        assertThrows(
                JsonParseException.class,
                () ->
                        metadataBuilder.setCredentialConfigurationsSupported(
                                "test_invalid_credential_configurations_supported.json"));
    }

    @Test
    void Should_ThrowIllegalArgumentException_When_FileDoesNotExist() {
        IllegalArgumentException exceptionThrown =
                assertThrows(
                        IllegalArgumentException.class,
                        () ->
                                metadataBuilder.setCredentialConfigurationsSupported(
                                        "notARealFile.json"));
        assertEquals("resource notARealFile.json not found.", exceptionThrown.getMessage());
    }

    @Test
    void Should_ThrowIllegalArgumentException_When_FileNameIsNull() {
        IllegalArgumentException exceptionThrown =
                assertThrows(
                        IllegalArgumentException.class,
                        () -> metadataBuilder.setCredentialConfigurationsSupported(null));
        assertEquals("fileName must not be null", exceptionThrown.getMessage());
    }

    @Test
    void Should_ThrowIllegalArgumentException_When_CredentialEndpointIsNull() {
        IllegalArgumentException exceptionThrown =
                assertThrows(
                        IllegalArgumentException.class,
                        () -> metadataBuilder.setCredentialEndpoint(null));
        assertEquals("credentialEndpoint must not be null", exceptionThrown.getMessage());
    }

    @Test
    void Should_ThrowIllegalArgumentException_When_AuthorizationServersIsNull() {
        IllegalArgumentException exceptionThrown =
                assertThrows(
                        IllegalArgumentException.class,
                        () -> metadataBuilder.setAuthorizationServers(null));
        assertEquals("authorizationServers must not be null", exceptionThrown.getMessage());
    }

    @Test
    void Should_ThrowIllegalArgumentException_When_CredentialIssuerIsNull() {
        IllegalArgumentException exceptionThrown =
                assertThrows(
                        IllegalArgumentException.class,
                        () -> metadataBuilder.setCredentialIssuer(null));
        assertEquals("credentialIssuer must not be null", exceptionThrown.getMessage());
    }

    @Test
    void Should_ThrowIllegalArgumentException_When_NotificationEndpointIsNull() {
        IllegalArgumentException exceptionThrown =
                assertThrows(
                        IllegalArgumentException.class,
                        () -> metadataBuilder.setNotificationEndpoint(null));
        assertEquals("notificationEndpoint must not be null", exceptionThrown.getMessage());
    }

    @Test
    void Should_ThrowIllegalArgumentException_When_IacasEndpointIsNull() {
        IllegalArgumentException exceptionThrown =
                assertThrows(
                        IllegalArgumentException.class,
                        () -> metadataBuilder.setIacasEndpoint(null));
        assertEquals("iacasEndpoint must not be null", exceptionThrown.getMessage());
    }

    @Test
    void Should_ThrowIllegalArgumentException_When_DisplayLogoEndpointIsNull() {
        IllegalArgumentException exceptionThrown =
                assertThrows(
                        IllegalArgumentException.class, () -> metadataBuilder.setDisplay(null));
        assertEquals("logoEndpoint must not be null", exceptionThrown.getMessage());
    }
}
