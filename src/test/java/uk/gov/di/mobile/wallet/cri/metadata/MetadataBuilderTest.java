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

public class MetadataBuilderTest {

    @Test
    void shouldReturnCredentialMetadata() throws IOException {
        ObjectMapper objectMapper = new ObjectMapper();

        Metadata response =
                new MetadataBuilder()
                        .setCredentialIssuer("https://test-credential-issuer.gov.uk")
                        .setCredentialsEndpoint("https://test-credential-issuer.gov.uk/credential")
                        .setAuthorizationServers(
                                "https://test-authorization-server.gov.uk/auth-server")
                        .setCredentialsSupported("test_valid_credentials_supported.json")
                        .build();

        JsonNode expectedCredentialsSupportedAsString =
                objectMapper.readTree(
                        "{\"socialSecurity\":{\"format\":\"jwt_vc_json\",\"id\":\"SocialSecurity_JWT\",\"types\":[\"VerifiableCredential\",\"SocialSecurityCredential\"],\"cryptographic_binding_methods_supported\":[\"did:key\"],\"credential_signing_alg_values_supported\":[\"ES256\"],\"display\":[{\"name\":\"National Insurance number\",\"locale\":\"en-GB\",\"background_color\":\"#12107c\",\"text_color\":\"#FFFFFF\"},{\"name\":\"Rhif Yswiriant Gwladol\",\"locale\":\"en-CY\",\"background_color\":\"#12107c\",\"text_color\":\"#FFFFFF\"}],\"credentialSubject\":{\"name\":[{\"nameParts\":[{\"display\":[{\"name\":\"Name\",\"locale\":\"en-GB\"},{\"name\":\"Enw\",\"locale\":\"cy-GB\"}]}]}],\"socialSecurityRecord\":{\"personalNumber\":{\"display\":[{\"name\":\"National Insurance number\",\"locale\":\"en-GB\"},{\"name\":\"Rhif Yswiriant Gwladol\",\"locale\":\"cy-GB\"}]}}}}}");
        JsonNode actualCredentialsSupportedAsString =
                objectMapper.readTree(
                        objectMapper.writeValueAsString(
                                response.credential_configurations_supported));

        assertEquals("https://test-credential-issuer.gov.uk", response.credential_issuer);
        assertArrayEquals(
                new String[] {"https://test-authorization-server.gov.uk/auth-server"},
                response.authorization_servers);
        assertEquals(
                "https://test-credential-issuer.gov.uk/credential", response.credentials_endpoint);
        assertEquals(expectedCredentialsSupportedAsString, actualCredentialsSupportedAsString);
    }

    @Test
    @DisplayName("Should throw JsonParseException when credentials_supported is invalid JSON")
    void shouldThrowJsonParseExceptionOnInvalidJson() {
        MetadataBuilder metadataBuilder = new MetadataBuilder();
        assertThrows(
                JsonParseException.class,
                () ->
                        metadataBuilder.setCredentialsSupported(
                                "test_invalid_credentials_supported.json"));
    }

    @Test
    @DisplayName(
            "Should throw error when setCredentialsSupported is called with a file name that does not exist")
    void shouldThrowIllegalArgumentExceptionOnInvalidFileName() {
        MetadataBuilder metadataBuilder = new MetadataBuilder();
        IllegalArgumentException thrown =
                assertThrows(
                        IllegalArgumentException.class,
                        () -> metadataBuilder.setCredentialsSupported("notARealFile.json"));
        Assertions.assertEquals("resource notARealFile.json not found.", thrown.getMessage());
    }

    @Test
    @DisplayName(
            "Should throw IllegalArgumentException when setCredentialsSupported is called with null value")
    void shouldThrowIllegalArgumentExceptionOnNullCredentialsSupported() {
        MetadataBuilder metadataBuilder = new MetadataBuilder();
        IllegalArgumentException thrown =
                assertThrows(
                        IllegalArgumentException.class,
                        () -> metadataBuilder.setCredentialsSupported(null));
        Assertions.assertEquals("fileName must not be null", thrown.getMessage());
    }

    @Test
    @DisplayName(
            "Should throw IllegalArgumentException when setCredentialsEndpoint is called with null value")
    void shouldThrowIllegalArgumentExceptionOnNullCredentialsEndpoint() {
        MetadataBuilder metadataBuilder = new MetadataBuilder();
        IllegalArgumentException thrown =
                assertThrows(
                        IllegalArgumentException.class,
                        () -> metadataBuilder.setCredentialsEndpoint(null));
        Assertions.assertEquals("credentials_endpoint must not be null", thrown.getMessage());
    }

    @Test
    @DisplayName(
            "Should throw IllegalArgumentException when setAuthorizationServer is called with null value")
    void shouldThrowIllegalArgumentExceptionOnNullAuthorizationServer() {
        MetadataBuilder metadataBuilder = new MetadataBuilder();
        IllegalArgumentException thrown =
                assertThrows(
                        IllegalArgumentException.class,
                        () -> metadataBuilder.setAuthorizationServers(null));
        Assertions.assertEquals("authorization_servers must not be null", thrown.getMessage());
    }

    @Test
    @DisplayName(
            "Should throw IllegalArgumentException when setCredentialIssuer is called with null value")
    void shouldThrowIllegalArgumentExceptionOnNullCredentialIssuer() {
        MetadataBuilder metadataBuilder = new MetadataBuilder();
        IllegalArgumentException thrown =
                assertThrows(
                        IllegalArgumentException.class,
                        () -> metadataBuilder.setCredentialIssuer(null));
        Assertions.assertEquals("credential_issuer must not be null", thrown.getMessage());
    }
}
