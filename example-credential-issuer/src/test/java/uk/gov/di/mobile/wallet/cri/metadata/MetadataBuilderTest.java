package uk.gov.di.mobile.wallet.cri.metadata;

import com.fasterxml.jackson.core.JsonParseException;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.io.IOException;

import static org.junit.jupiter.api.Assertions.assertArrayEquals;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;

class MetadataBuilderTest {

    private static final String CREDENTIAL_STORE_URL = "https://credential-store.test.gov.uk";

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
                                "credential_configurations_supported.json", CREDENTIAL_STORE_URL)
                        .setDisplay(issuer + "/test-logo.png")
                        .build();

        assertEquals(issuer, metadata.credentialIssuer);
        assertArrayEquals(new String[] {auth}, metadata.authorizationServers);
        assertEquals(credential, metadata.credentialEndpoint);
        assertEquals(notification, metadata.notificationEndpoint);
        assertEquals(iacas, metadata.iacasEndpoint);
        String expectedCredentialConfigurationsSupported =
                "{SocialSecurityCredential={format=jwt_vc_json, credential_definition={type=[VerifiableCredential, SocialSecurityCredential]}, cryptographic_binding_methods_supported=[did:key], credential_signing_alg_values_supported=[ES256], proof_types_supported={jwt={proof_signing_alg_values_supported=[ES256]}}, credential_validity_period_max_days=30, credential_refresh_web_journey_url=https://credential-store.test.gov.uk/refresh/SocialSecurityCredential}, BasicDisclosureCredential={format=jwt_vc_json, credential_definition={type=[VerifiableCredential, BasicDisclosureCredential]}, cryptographic_binding_methods_supported=[did:key], credential_signing_alg_values_supported=[ES256], proof_types_supported={jwt={proof_signing_alg_values_supported=[ES256]}}, credential_validity_period_max_days=30, credential_refresh_web_journey_url=https://credential-store.test.gov.uk/refresh/BasicDisclosureCredential}, DigitalVeteranCard={format=jwt_vc_json, credential_definition={type=[VerifiableCredential, DigitalVeteranCard]}, cryptographic_binding_methods_supported=[did:key], credential_signing_alg_values_supported=[ES256], proof_types_supported={jwt={proof_signing_alg_values_supported=[ES256]}}, credential_validity_period_max_days=30, credential_refresh_web_journey_url=https://credential-store.test.gov.uk/refresh/DigitalVeteranCard}, org.iso.18013.5.1.mDL={format=mso_mdoc, doctype=org.iso.18013.5.1.mDL, cryptographic_binding_methods_supported=[cose_key], credential_signing_alg_values_supported=[ES256], credential_validity_period_max_days=30, credential_refresh_web_journey_url=https://credential-store.test.gov.uk/refresh/org.iso.18013.5.1.mDL}, uk.gov.account.mobile.example-credential-issuer.simplemdoc.1={format=mso_mdoc, doctype=uk.gov.account.mobile.example-credential-issuer.simplemdoc.1, cryptographic_binding_methods_supported=[cose_key], credential_signing_alg_values_supported=[ES256], credential_validity_period_max_days=30, credential_metadata={display=[{locale=en, name=Simple Document, background_color_1=#74ebd5, background_color_2=#acb6e5}, {locale=cy, name=CREDENTIAL_TITLE_WELSH, background_color_1=#74ebd5, background_color_2=#acb6e5}], claims=[{path=[org.iso.18013.5.1, document_number], mandatory=true, value_type=string, display=[{locale=en, name=License number}, {locale=cy, name=LICENSE_NUMBER_WELSH}]}, {path=[org.iso.18013.5.1, portrait], mandatory=true, value_type=jpeg, display=[{locale=en, name=Portrait}, {locale=cy, name=Portrait_WELSH}]}, {path=[org.iso.18013.5.1, given_name], mandatory=true, value_type=string, display=[{locale=en, name=Given Name}, {locale=cy, name=GIVEN_NAME_WELSH}]}, {path=[org.iso.18013.5.1, family_name], mandatory=true, value_type=string, display=[{locale=en, name=Surname}, {locale=cy, name=SURNAME_WELSH}]}, {path=[org.iso.18013.5.1, birth_date], mandatory=true, value_type=full-date, display=[{locale=en, name=DoB}, {locale=cy, name=DOB_WELSH}]}, {path=[uk.gov.account.mobile.example-credential-issuer.simplemdoc.1, type_of_fish], mandatory=true, value_type=string, display=[{locale=en, name=Type of fish}, {locale=cy, name=TYPE_OF_FISH_WELSH}]}, {path=[uk.gov.account.mobile.example-credential-issuer.simplemdoc.1, number_of_fishing_rods], mandatory=true, value_type=string, display=[{locale=en, name=Number of fishing rods}, {locale=cy, name=NUMBER_OF_FISHING_RODS_WELSH}]}, {path=[org.iso.18013.5.1, expiry_date], mandatory=true, value_type=string}, {path=[org.iso.18013.5.1, issue_date], mandatory=true, value_type=full-date}, {path=[org.iso.18013.5.1, issuing_country], mandatory=true, value_type=string}]}, credential_refresh_web_journey_url=https://credential-store.test.gov.uk/refresh/uk.gov.account.mobile.example-credential-issuer.simplemdoc.1}}";
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
                                "test_invalid_credential_configurations_supported.json",
                                CREDENTIAL_STORE_URL));
    }

    @Test
    void Should_ThrowIllegalArgumentException_When_FileDoesNotExist() {
        IllegalArgumentException exceptionThrown =
                assertThrows(
                        IllegalArgumentException.class,
                        () ->
                                metadataBuilder.setCredentialConfigurationsSupported(
                                        "notARealFile.json", CREDENTIAL_STORE_URL));
        assertEquals("resource notARealFile.json not found.", exceptionThrown.getMessage());
    }

    @Test
    void Should_ThrowIllegalArgumentException_When_FileNameIsNull() {
        IllegalArgumentException exceptionThrown =
                assertThrows(
                        IllegalArgumentException.class,
                        () ->
                                metadataBuilder.setCredentialConfigurationsSupported(
                                        null, CREDENTIAL_STORE_URL));
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
