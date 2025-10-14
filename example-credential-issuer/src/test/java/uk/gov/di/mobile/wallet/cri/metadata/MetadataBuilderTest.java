package uk.gov.di.mobile.wallet.cri.metadata;

import com.fasterxml.jackson.core.JsonParseException;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.io.IOException;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.*;

class MetadataBuilderTest {

    private MetadataBuilder metadataBuilder;

    @BeforeEach
    void setUp() {
        metadataBuilder = new MetadataBuilder();
    }

    @Test
    void Should_ReturnMetadata() throws IOException {
        ObjectMapper objectMapper = new ObjectMapper();
        JsonNode expectedCredentialConfigurationsSupported =
                objectMapper.readTree(
                        "{\"SocialSecurityCredential\":{\"format\":\"jwt_vc_json\",\"credential_definition\":{\"type\":[\"VerifiableCredential\",\"SocialSecurityCredential\"]},\"cryptographic_binding_methods_supported\":[\"did:key\"],\"credential_signing_alg_values_supported\":[\"ES256\"],\"proof_types_supported\":{\"jwt\":{\"proof_signing_alg_values_supported\":[\"ES256\"]}}, \"credential_refresh_web_journey_url\": \"https://test-credential-issuer.gov.uk/refresh/SocialSecurityCredential\"},\"BasicDisclosureCredential\":{\"format\":\"jwt_vc_json\",\"credential_definition\":{\"type\":[\"VerifiableCredential\",\"BasicDisclosureCredential\"]},\"cryptographic_binding_methods_supported\":[\"did:key\"],\"credential_signing_alg_values_supported\":[\"ES256\"],\"proof_types_supported\":{\"jwt\":{\"proof_signing_alg_values_supported\":[\"ES256\"]}}, \"credential_refresh_web_journey_url\": \"https://test-credential-issuer.gov.uk/refresh/BasicDisclosureCredential\"},\"DigitalVeteranCard\":{\"format\":\"jwt_vc_json\",\"credential_definition\":{\"type\":[\"VerifiableCredential\",\"DigitalVeteranCard\"]},\"cryptographic_binding_methods_supported\":[\"did:key\"],\"credential_signing_alg_values_supported\":[\"ES256\"],\"proof_types_supported\":{\"jwt\":{\"proof_signing_alg_values_supported\":[\"ES256\"]}}, \"credential_refresh_web_journey_url\": \"https://test-credential-issuer.gov.uk/refresh/DigitalVeteranCard\"},\"org.iso.18013.5.1.mDL\": {\"format\": \"mso_mdoc\",\"doctype\": \"org.iso.18013.5.1.mDL\",\"cryptographic_binding_methods_supported\": [\"cose_key\"],\"credential_signing_alg_values_supported\": [\"ES256\"], \"credential_refresh_web_journey_url\": \"https://test-credential-issuer.gov.uk/refresh/org.iso.18013.5.1.mDL\"}}");
        Metadata metadata =
                metadataBuilder
                        .setCredentialIssuer("https://test-credential-issuer.gov.uk")
                        .setCredentialEndpoint("https://test-credential-issuer.gov.uk/credential")
                        .setAuthorizationServers(
                                "https://test-authorization-server.gov.uk/auth-server")
                        .setNotificationEndpoint(
                                "https://test-credential-issuer.gov.uk/notification")
                        .setIacasEndpoint("https://test-credential-issuer.gov.uk/iacas")
                        .setCredentialConfigurationsSupported(
                                "test_valid_credential_configurations_supported.json")
                        .build();

        assertEquals("https://test-credential-issuer.gov.uk", metadata.credentialIssuer);
        assertArrayEquals(
                new String[] {"https://test-authorization-server.gov.uk/auth-server"},
                metadata.authorizationServers);
        assertEquals(
                "https://test-credential-issuer.gov.uk/credential", metadata.credentialEndpoint);
        assertEquals(
                "https://test-credential-issuer.gov.uk/notification",
                metadata.notificationEndpoint);
        assertEquals("https://test-credential-issuer.gov.uk/iacas", metadata.iacasEndpoint);
        JsonNode actualCredentialConfigurationsSupported =
                objectMapper.readTree(
                        objectMapper.writeValueAsString(
                                metadata.credentialConfigurationsSupported));
        assertEquals(
                expectedCredentialConfigurationsSupported, actualCredentialConfigurationsSupported);
    }

    @Test
    @DisplayName(
            "Should throw a JsonParseException when credential_configurations_supported is not a valid JSON")
    void Should_ThrowJsonParseException_When_JsonIsInvalid() {
        assertThrows(
                JsonParseException.class,
                () ->
                        metadataBuilder.setCredentialConfigurationsSupported(
                                "test_invalid_credential_configurations_supported.json"));
    }

    @Test
    @DisplayName(
            "Should throw IllegalArgumentException when setCredentialConfigurationsSupported is called with a file name that does not exist")
    void Should_ThrowIllegalArgumentException_When_FileDoesNotExist() {
        IllegalArgumentException exceptionThrown =
                assertThrows(
                        IllegalArgumentException.class,
                        () ->
                                metadataBuilder.setCredentialConfigurationsSupported(
                                        "notARealFile.json"));
        Assertions.assertEquals(
                "resource notARealFile.json not found.", exceptionThrown.getMessage());
    }

    @Test
    @DisplayName(
            "Should throw IllegalArgumentException when setCredentialConfigurationsSupported is called with null")
    void Should_ThrowIllegalArgumentException_When_FileNameIsNull() {
        IllegalArgumentException exceptionThrown =
                assertThrows(
                        IllegalArgumentException.class,
                        () -> metadataBuilder.setCredentialConfigurationsSupported(null));
        Assertions.assertEquals("fileName must not be null", exceptionThrown.getMessage());
    }

    @Test
    @DisplayName(
            "Should throw IllegalArgumentException when setCredentialEndpoint is called with null")
    void Should_ThrowIllegalArgumentException_When_ACredentialEndpointIsNull() {
        IllegalArgumentException exceptionThrown =
                assertThrows(
                        IllegalArgumentException.class,
                        () -> metadataBuilder.setCredentialEndpoint(null));
        Assertions.assertEquals(
                "credentialEndpoint must not be null", exceptionThrown.getMessage());
    }

    @Test
    @DisplayName(
            "Should throw IllegalArgumentException when setAuthorizationServers is called with null")
    void Should_ThrowIllegalArgumentException_When_AuthorizationServersIsNull() {
        IllegalArgumentException exceptionThrown =
                assertThrows(
                        IllegalArgumentException.class,
                        () -> metadataBuilder.setAuthorizationServers(null));
        Assertions.assertEquals(
                "authorizationServers must not be null", exceptionThrown.getMessage());
    }

    @Test
    @DisplayName(
            "Should throw IllegalArgumentException when setCredentialIssuer is called with null")
    void Should_ThrowIllegalArgumentException_When_CredentialIssuerIsNull() {
        IllegalArgumentException exceptionThrown =
                assertThrows(
                        IllegalArgumentException.class,
                        () -> metadataBuilder.setCredentialIssuer(null));
        Assertions.assertEquals("credentialIssuer must not be null", exceptionThrown.getMessage());
    }

    @Test
    @DisplayName(
            "Should throw IllegalArgumentException when setNotificationEndpoint is called with 'null'")
    void Should_ThrowIllegalArgumentException_When_NotificationEndpointIsNull() {
        IllegalArgumentException exceptionThrown =
                assertThrows(
                        IllegalArgumentException.class,
                        () -> metadataBuilder.setNotificationEndpoint(null));
        Assertions.assertEquals(
                "notificationEndpoint must not be null", exceptionThrown.getMessage());
    }

    @Test
    @DisplayName(
            "Should throw IllegalArgumentException when setIacasEndpoint is called with 'null'")
    void Should_ThrowIllegalArgumentException_When_IacasEndpointIsNull() {
        IllegalArgumentException exceptionThrown =
                assertThrows(
                        IllegalArgumentException.class,
                        () -> metadataBuilder.setIacasEndpoint(null));
        Assertions.assertEquals("iacasEndpoint must not be null", exceptionThrown.getMessage());
    }

    @Test
    @SuppressWarnings("unchecked")
    void should_SetCredentialRefreshUrls_ForEach_Credential() throws IOException {
        String selfUrl = "https://test-credential-issuer.gov.uk";
        Metadata metadata =
                metadataBuilder
                        .setCredentialIssuer("https://test-credential-issuer.gov.uk")
                        .setCredentialEndpoint("https://test-credential-issuer.gov.uk/credential")
                        .setAuthorizationServers(
                                "https://test-authorization-server.gov.uk/auth-server")
                        .setNotificationEndpoint(
                                "https://test-credential-issuer.gov.uk/notification")
                        .setIacasEndpoint("https://test-credential-issuer.gov.uk/iacas")
                        .setCredentialConfigurationsSupported(
                                "test_valid_credential_configurations_supported.json")
                        .setCredentialRefreshUrls(selfUrl)
                        .build();

        Map<String, Object> configs =
                (Map<String, Object>) metadata.credentialConfigurationsSupported;
        assertNotNull(configs);

        for (Map.Entry<String, Object> entry : configs.entrySet()) {
            String credentialName = entry.getKey();

            Map<String, Object> perCredential =
                    assertInstanceOf(Map.class, entry.getValue(), "Credentials are of type Map");

            Object url = perCredential.get("credential_refresh_web_journey_url");

            assertNotNull(url, "missing credential_refresh_web_journey_url for" + credentialName);
            assertEquals(
                    selfUrl + "/refresh/" + credentialName, url, "wrong url for " + credentialName);
        }
    }
}
