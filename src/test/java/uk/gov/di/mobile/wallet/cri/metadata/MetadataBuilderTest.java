package uk.gov.di.mobile.wallet.cri.metadata;

import com.fasterxml.jackson.core.JsonParseException;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.io.IOException;

import static org.junit.jupiter.api.Assertions.assertArrayEquals;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;

class MetadataBuilderTest {

    @Test
    @DisplayName("Should return the CRI metadata")
    void testReturnsCRIMetadata() throws IOException {
        ObjectMapper objectMapper = new ObjectMapper();
        JsonNode expectedCredentialConfigurationsSupported =
                objectMapper.readTree(
                        "{\"socialSecurity\":{\"format\":\"jwt_vc_json\",\"credential_definition\":{\"types\":[\"VerifiableCredential\",\"SocialSecurityCredential\"]},\"cryptographic_binding_methods_supported\":[\"did:key\"],\"credential_signing_alg_values_supported\":[\"ES256\"],\"proof_types_supported\":{\"jwt\":{\"proof_signing_alg_values_supported\":[\"ES256\"],\"key_attestations_required\":{}}},\"display\":[{\"name\":\"National Insurance number\",\"locale\":\"en-GB\",\"background_color\":\"#12107c\",\"text_color\":\"#FFFFFF\"},{\"name\":\"Rhif Yswiriant Gwladol\",\"locale\":\"en-CY\",\"background_color\":\"#12107c\",\"text_color\":\"#FFFFFF\"}],\"credentialSubject\":{\"name\":[{\"nameParts\":[{\"display\":[{\"name\":\"Name\",\"locale\":\"en-GB\"},{\"name\":\"Enw\",\"locale\":\"cy-GB\"}]}]}],\"socialSecurityRecord\":{\"personalNumber\":{\"display\":[{\"name\":\"National Insurance number\",\"locale\":\"en-GB\"},{\"name\":\"Rhif Yswiriant Gwladol\",\"locale\":\"cy-GB\"}]}}}},\"basicDisclosure\":{\"format\":\"jwt_vc_json\",\"credential_definition\":{\"types\":[\"VerifiableCredential\",\"BasicDisclosureCredential\"]},\"cryptographic_binding_methods_supported\":[\"did:key\"],\"credential_signing_alg_values_supported\":[\"ES256\"],\"proof_types_supported\":{\"jwt\":{\"proof_signing_alg_values_supported\":[\"ES256\"],\"key_attestations_required\":{}}},\"display\":[{\"name\":\"Basic DBS disclosure certificate\",\"locale\":\"en-GB\",\"logo\":{\"url\":\"https://issuer.gov.uk/assets/logo.png\",\"alt_text\":\"a square logo of DBS\"},\"background_color\":\"#12107c\",\"text_color\":\"#FFFFFF\"},{\"name\":\"Tystysgrif gwiriad DBS sylfaenol\",\"locale\":\"en-CY\",\"logo\":{\"url\":\"https://issuer.gov.uk/assets/logo.png\",\"alt_text\":\"logo sgwâr o DBS\"},\"background_color\":\"#12107c\",\"text_color\":\"#FFFFFF\"}],\"credentialSubject\":{\"lastName\":{\"display\":[{\"field\":\"Auxiliaryfield1\",\"name\":\"Surname\",\"locale\":\"en-GB\"},{\"field\":\"Auxiliaryfield1\",\"name\":\"Cyfenw\",\"locale\":\"cy-GB\"}]},\"givenName\":{\"display\":[{\"field\":\"Auxiliaryfield2\",\"name\":\"Forename(s)\",\"locale\":\"en-GB\"},{\"field\":\"Auxiliaryfield2\",\"name\":\"Enw(au) cyntaf\",\"locale\":\"cy-GB\"}]},\"dateOfBirth\":{\"display\":[{\"field\":\"Auxiliaryfield3\",\"name\":\"Date of birth\",\"locale\":\"en-GB\"},{\"field\":\"Auxiliaryfield3\",\"name\":\"Dyddiad Geni\",\"locale\":\"cy-GB\"}]},\"firstLineOfAddress\":{\"display\":[{\"field\":\"Auxiliaryfield4\",\"name\":\"First line of address\",\"locale\":\"en-GB\"},{\"field\":\"Auxiliaryfield4\",\"name\":\"Llinell gyntaf y cyfeiriad\",\"locale\":\"cy-GB\"}]},\"basicDisclosureRecord\":{\"outcome\":{\"display\":[{\"field\":\"PrimaryField\",\"name\":\"Outcome\",\"locale\":\"en-GB\"},{\"field\":\"PrimaryField\",\"name\":\"Canlyniad\",\"locale\":\"cy-GB\"}]},\"disclosureDate\":{\"display\":[{\"field\":\"KeyField1\",\"name\":\"Disclosure date\",\"locale\":\"en-GB\"},{\"field\":\"KeyField1\",\"name\":\"Dyddiad datgelu\",\"locale\":\"cy-GB\"}]},\"certificateNumber\":{\"display\":[{\"field\":\"KeyField2\",\"name\":\"Certificate number\",\"locale\":\"en-GB\"},{\"field\":\"KeyField2\",\"name\":\"Rhif tystysgrif\",\"locale\":\"cy-GB\"}]}}}},\"veteranCard\":{\"format\":\"jwt_vc_json\",\"credential_definition\":{\"type\":[\"VerifiableCredential\",\"digitalVeteranCard\"]},\"cryptographic_binding_methods_supported\":[\"did:key\"],\"credential_signing_alg_values_supported\":[\"ES256\"],\"proof_types_supported\":{\"jwt\":{\"proof_signing_alg_values_supported\":[\"ES256\"],\"key_attestations_required\":{}}},\"display\":[{\"name\":\"HM Armed Forces Veteran Card\",\"locale\":\"en-GB\"}],\"credentialSubject\":{\"firstName\":{\"display\":[{\"name\":\"First name\",\"locale\":\"en-GB\"}]},\"lastName\":{\"display\":[{\"name\":\"Last name\",\"locale\":\"en-GB\"}]},\"birthDate\":{\"display\":[{\"name\":\"Date of birth\",\"locale\":\"en-GB\"}]},\"veteranCard\":{\"expiryDate\":{\"display\":[{\"name\":\"Expiry date\",\"locale\":\"en-GB\"}]},\"serviceStart\":{\"display\":[{\"name\":\"Start date\",\"locale\":\"en-GB\"}]},\"serviceEnd\":{\"display\":[{\"name\":\"End date\",\"locale\":\"en-GB\"}]},\"serviceNumber\":{\"display\":[{\"name\":\"Service number\",\"locale\":\"en-GB\"}]},\"serviceHistory\":{\"display\":[{\"name\":\"Service History\",\"locale\":\"en-GB\"}]},\"serviceRecord\":{\"display\":[{\"name\":\"Service record\",\"locale\":\"en-GB\"}]},\"branch\":{\"display\":[{\"name\":\"Branch\",\"locale\":\"en-GB\"}]},\"photo\":{\"display\":[{\"name\":\"Photo\"}]}}}}}");

        Metadata metadata =
                new MetadataBuilder()
                        .setCredentialIssuer("https://test-credential-issuer.gov.uk")
                        .setCredentialsEndpoint("https://test-credential-issuer.gov.uk/credential")
                        .setCredentialEndpoint("https://test-credential-issuer.gov.uk/credential")
                        .setAuthorizationServers(
                                "https://test-authorization-server.gov.uk/auth-server")
                        .setCredentialConfigurationsSupported(
                                "test_valid_credentials_supported.json")
                        .build();

        assertEquals("https://test-credential-issuer.gov.uk", metadata.credential_issuer);
        assertArrayEquals(
                new String[] {"https://test-authorization-server.gov.uk/auth-server"},
                metadata.authorization_servers);
        assertEquals(
                "https://test-credential-issuer.gov.uk/credential", metadata.credentials_endpoint);
        assertEquals(
                "https://test-credential-issuer.gov.uk/credential", metadata.credential_endpoint);
        JsonNode actualCredentialConfigurationsSupported =
                objectMapper.readTree(
                        objectMapper.writeValueAsString(
                                metadata.credential_configurations_supported));
        assertEquals(
                expectedCredentialConfigurationsSupported, actualCredentialConfigurationsSupported);
    }

    @Test
    @DisplayName(
            "Should throw a JsonParseException when credential_configurations_supported is not a valid JSON")
    void testInvalidJSON() {
        assertThrows(
                JsonParseException.class,
                () ->
                        new MetadataBuilder()
                                .setCredentialConfigurationsSupported(
                                        "test_invalid_credentials_supported.json"));
    }

    @Test
    @DisplayName(
            "Should throw IllegalArgumentException when setCredentialConfigurationsSupported is called with a file name that does not exist")
    void testInvalidFileName() {
        IllegalArgumentException exceptionThrown =
                assertThrows(
                        IllegalArgumentException.class,
                        () ->
                                new MetadataBuilder()
                                        .setCredentialConfigurationsSupported("notARealFile.json"));
        Assertions.assertEquals(
                "resource notARealFile.json not found.", exceptionThrown.getMessage());
    }

    @Test
    @DisplayName(
            "Should throw IllegalArgumentException when setCredentialConfigurationsSupported is called with null")
    void testCredentialConfigurationsSupportedNullValue() {
        IllegalArgumentException exceptionThrown =
                assertThrows(
                        IllegalArgumentException.class,
                        () -> new MetadataBuilder().setCredentialConfigurationsSupported(null));
        Assertions.assertEquals("fileName must not be null", exceptionThrown.getMessage());
    }

    @Test
    @DisplayName(
            "Should throw IllegalArgumentException when setCredentialsEndpoint is called with null")
    void testCredentialsEndpointNullValue() {
        IllegalArgumentException exceptionThrown =
                assertThrows(
                        IllegalArgumentException.class,
                        () -> new MetadataBuilder().setCredentialsEndpoint(null));
        Assertions.assertEquals(
                "credentials_endpoint must not be null", exceptionThrown.getMessage());
    }

    @Test
    @DisplayName(
            "Should throw IllegalArgumentException when setCredentialEndpoint is called with null")
    void testCredentialEndpointNullValue() {
        IllegalArgumentException exceptionThrown =
                assertThrows(
                        IllegalArgumentException.class,
                        () -> new MetadataBuilder().setCredentialEndpoint(null));
        Assertions.assertEquals(
                "credential_endpoint must not be null", exceptionThrown.getMessage());
    }

    @Test
    @DisplayName(
            "Should throw IllegalArgumentException when setAuthorizationServers is called with null")
    void testAuthorizationServersNullValue() {
        IllegalArgumentException exceptionThrown =
                assertThrows(
                        IllegalArgumentException.class,
                        () -> new MetadataBuilder().setAuthorizationServers(null));
        Assertions.assertEquals(
                "authorization_servers must not be null", exceptionThrown.getMessage());
    }

    @Test
    @DisplayName(
            "Should throw IllegalArgumentException when setCredentialIssuer is called with null")
    void testCredentialIssuerNullValue() {
        IllegalArgumentException exceptionThrown =
                assertThrows(
                        IllegalArgumentException.class,
                        () -> new MetadataBuilder().setCredentialIssuer(null));
        Assertions.assertEquals("credential_issuer must not be null", exceptionThrown.getMessage());
    }
}
