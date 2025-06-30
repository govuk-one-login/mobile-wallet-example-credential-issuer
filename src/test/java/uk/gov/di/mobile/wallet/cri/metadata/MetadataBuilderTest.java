package uk.gov.di.mobile.wallet.cri.metadata;

import com.fasterxml.jackson.core.JsonParseException;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
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
        ObjectMapper objectMapper = new ObjectMapper();
        JsonNode expectedCredentialConfigurationsSupported =
                objectMapper.readTree(
                        "{\"SocialSecurityCredential\":{\"format\":\"jwt_vc_json\",\"credential_definition\":{\"type\":[\"VerifiableCredential\",\"SocialSecurityCredential\"]},\"cryptographic_binding_methods_supported\":[\"did:key\"],\"credential_signing_alg_values_supported\":[\"ES256\"],\"proof_types_supported\":{\"jwt\":{\"proof_signing_alg_values_supported\":[\"ES256\"],\"key_attestations_required\":{}}},\"display\":[{\"name\":\"National Insurance number\",\"locale\":\"en-GB\",\"background_color\":\"#12107c\",\"text_color\":\"#FFFFFF\"},{\"name\":\"Rhif Yswiriant Gwladol\",\"locale\":\"en-CY\",\"background_color\":\"#12107c\",\"text_color\":\"#FFFFFF\"}],\"credentialSubject\":{\"name\":[{\"nameParts\":[{\"display\":[{\"name\":\"Name\",\"locale\":\"en-GB\"},{\"name\":\"Enw\",\"locale\":\"cy-GB\"}]}]}],\"socialSecurityRecord\":{\"personalNumber\":{\"display\":[{\"name\":\"National Insurance number\",\"locale\":\"en-GB\"},{\"name\":\"Rhif Yswiriant Gwladol\",\"locale\":\"cy-GB\"}]}}}},\"BasicDisclosureCredential\":{\"format\":\"jwt_vc_json\",\"credential_definition\":{\"type\":[\"VerifiableCredential\",\"BasicDisclosureCredential\"]},\"cryptographic_binding_methods_supported\":[\"did:key\"],\"credential_signing_alg_values_supported\":[\"ES256\"],\"proof_types_supported\":{\"jwt\":{\"proof_signing_alg_values_supported\":[\"ES256\"],\"key_attestations_required\":{}}},\"display\":[{\"name\":\"Basic DBS disclosure certificate\",\"locale\":\"en-GB\",\"logo\":{\"url\":\"https://issuer.gov.uk/assets/logo.png\",\"alt_text\":\"a square logo of DBS\"},\"background_color\":\"#12107c\",\"text_color\":\"#FFFFFF\"},{\"name\":\"Tystysgrif gwiriad DBS sylfaenol\",\"locale\":\"en-CY\",\"logo\":{\"url\":\"https://issuer.gov.uk/assets/logo.png\",\"alt_text\":\"logo sgwâr o DBS\"},\"background_color\":\"#12107c\",\"text_color\":\"#FFFFFF\"}],\"credentialSubject\":{\"lastName\":{\"display\":[{\"field\":\"Auxiliaryfield1\",\"name\":\"Surname\",\"locale\":\"en-GB\"},{\"field\":\"Auxiliaryfield1\",\"name\":\"Cyfenw\",\"locale\":\"cy-GB\"}]},\"givenName\":{\"display\":[{\"field\":\"Auxiliaryfield2\",\"name\":\"Forename(s)\",\"locale\":\"en-GB\"},{\"field\":\"Auxiliaryfield2\",\"name\":\"Enw(au) cyntaf\",\"locale\":\"cy-GB\"}]},\"dateOfBirth\":{\"display\":[{\"field\":\"Auxiliaryfield3\",\"name\":\"Date of birth\",\"locale\":\"en-GB\"},{\"field\":\"Auxiliaryfield3\",\"name\":\"Dyddiad Geni\",\"locale\":\"cy-GB\"}]},\"firstLineOfAddress\":{\"display\":[{\"field\":\"Auxiliaryfield4\",\"name\":\"First line of address\",\"locale\":\"en-GB\"},{\"field\":\"Auxiliaryfield4\",\"name\":\"Llinell gyntaf y cyfeiriad\",\"locale\":\"cy-GB\"}]},\"basicDisclosureRecord\":{\"outcome\":{\"display\":[{\"field\":\"PrimaryField\",\"name\":\"Outcome\",\"locale\":\"en-GB\"},{\"field\":\"PrimaryField\",\"name\":\"Canlyniad\",\"locale\":\"cy-GB\"}]},\"disclosureDate\":{\"display\":[{\"field\":\"KeyField1\",\"name\":\"Disclosure date\",\"locale\":\"en-GB\"},{\"field\":\"KeyField1\",\"name\":\"Dyddiad datgelu\",\"locale\":\"cy-GB\"}]},\"certificateNumber\":{\"display\":[{\"field\":\"KeyField2\",\"name\":\"Certificate number\",\"locale\":\"en-GB\"},{\"field\":\"KeyField2\",\"name\":\"Rhif tystysgrif\",\"locale\":\"cy-GB\"}]}}}},\"DigitalVeteranCard\":{\"format\":\"jwt_vc_json\",\"credential_definition\":{\"type\":[\"VerifiableCredential\",\"DigitalVeteranCard\"]},\"cryptographic_binding_methods_supported\":[\"did:key\"],\"credential_signing_alg_values_supported\":[\"ES256\"],\"proof_types_supported\":{\"jwt\":{\"proof_signing_alg_values_supported\":[\"ES256\"],\"key_attestations_required\":{}}},\"display\":[{\"name\":\"HM Armed Forces Veteran Card\",\"locale\":\"en-GB\"}],\"credentialSubject\":{\"firstName\":{\"display\":[{\"name\":\"First name\",\"locale\":\"en-GB\"}]},\"lastName\":{\"display\":[{\"name\":\"Last name\",\"locale\":\"en-GB\"}]},\"birthDate\":{\"display\":[{\"name\":\"Date of birth\",\"locale\":\"en-GB\"}]},\"veteranCard\":{\"expiryDate\":{\"display\":[{\"name\":\"Expiry date\",\"locale\":\"en-GB\"}]},\"serviceNumber\":{\"display\":[{\"name\":\"Service number\",\"locale\":\"en-GB\"}]},\"serviceBranch\":{\"display\":[{\"name\":\"Service branch\",\"locale\":\"en-GB\"}]},\"photo\":{\"display\":[{\"name\":\"Photo\"}]}}}},\"org.iso.18013.5.1.mDL\": {\"format\": \"mso_mdoc\",\"doctype\": \"org.iso.18013.5.1.mDL\",\"cryptographic_binding_methods_supported\": [\"cose_key\"],\"credential_signing_alg_values_supported\": [\"ES256\"]}}");
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
}
