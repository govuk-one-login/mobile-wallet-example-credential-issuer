package uk.gov.di.mobile.wallet.cri.metadata;

import com.fasterxml.jackson.core.JsonParseException;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.junit.jupiter.MockitoExtension;

import java.io.IOException;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;

@ExtendWith(MockitoExtension.class)
public class MetadataBuilderTest {

    private MetadataBuilder metadataBuilder;

    @Test
    @DisplayName("Should return the credential metadata")
    void testItReturns200AndCredentialMetadata() throws IOException {
        ObjectMapper objectMapper = new ObjectMapper();

        Metadata response =
                new MetadataBuilder()
                        .setCredentialIssuer("https://test-credential-issuer.gov.uk")
                        .setCredentialsEndpoint("https://test-credential-issuer.gov.uk/credential")
                        .setAuthorizationServer("https://test-auhtorization-server.gov.uk/sts-stub")
                        .setCredentialsSupported("test_valid_credentials_supported.json")
                        .build();

        JsonNode expectedCredentialsSupportedAsString =
                objectMapper.readTree(
                        "{\"socialSecurity\":{\"format\":\"jwt_vc_json\",\"id\":\"SocialSecurity_JWT\",\"types\":[\"VerifiableCredential\",\"SocialSecurityCredential\"],\"cryptographic_binding_methods_supported\":[\"did\"],\"cryptographic_suites_supported\":[\"ES256K\"],\"display\":[{\"name\":\"National Insurance number\",\"locale\":\"en-GB\",\"background_color\":\"#12107c\",\"text_color\":\"#FFFFFF\"},{\"name\":\"Rhif Yswiriant Gwladol\",\"locale\":\"en-CY\",\"background_color\":\"#12107c\",\"text_color\":\"#FFFFFF\"}],\"credentialSubject\":{\"name\":[{\"nameParts\":[{\"display\":[{\"name\":\"Name\",\"locale\":\"en-GB\"},{\"name\":\"Enw\",\"locale\":\"cy-GB\"}]}]}],\"socialSecurityRecord\":{\"personalNumber\":{\"display\":[{\"name\":\"National Insurance number\",\"locale\":\"en-GB\"},{\"name\":\"Rhif Yswiriant Gwladol\",\"locale\":\"cy-GB\"}]}}}}}");
        JsonNode actualCredentialsSupportedAsString =
                objectMapper.readTree(
                        objectMapper.writeValueAsString(response.credentials_supported));

        assertEquals("https://test-credential-issuer.gov.uk", response.credential_issuer);
        assertEquals(
                "https://test-auhtorization-server.gov.uk/sts-stub", response.authorization_server);
        assertEquals(
                "https://test-credential-issuer.gov.uk/credential", response.credentials_endpoint);
        assertEquals(expectedCredentialsSupportedAsString, actualCredentialsSupportedAsString);
    }

    @Test
    @DisplayName("Should throw JsonParseException when credentials_supported is invalid JSON")
    void testItThrowsJsonParseExceptionOnInvalidJson() {
        metadataBuilder = new MetadataBuilder();
        assertThrows(
                JsonParseException.class,
                () ->
                        metadataBuilder.setCredentialsSupported(
                                "test_invalid_credentials_supported.json"));
    }

    @Test
    @DisplayName(
            "Should throw error when setCredentialsSupported is called with a file name that does not exist")
    void testItThrowsIllegalArgumentExceptionOnInvalidFileName() {
        metadataBuilder = new MetadataBuilder();
        IllegalArgumentException thrown =
                assertThrows(
                        IllegalArgumentException.class,
                        () -> metadataBuilder.setCredentialsSupported("notARealFile.json"));
        Assertions.assertEquals("resource notARealFile.json not found.", thrown.getMessage());
    }

    @Test
    @DisplayName(
            "Should throw IllegalArgumentException when setCredentialsSupported is called with null value")
    void testItThrowsIllegalArgumentExceptionOnNullCredentialsSupported() {
        metadataBuilder = new MetadataBuilder();
        IllegalArgumentException thrown =
                assertThrows(
                        IllegalArgumentException.class,
                        () -> metadataBuilder.setCredentialsSupported(null));
        Assertions.assertEquals("fileName must not be null", thrown.getMessage());
    }

    @Test
    @DisplayName(
            "Should throw IllegalArgumentException when setCredentialsEndpoint is called with null value")
    void testItThrowsIllegalArgumentExceptionOnNullCredentialsEndpoint() {
        metadataBuilder = new MetadataBuilder();
        IllegalArgumentException thrown =
                assertThrows(
                        IllegalArgumentException.class,
                        () -> metadataBuilder.setCredentialsEndpoint(null));
        Assertions.assertEquals("credentials_endpoint must not be null", thrown.getMessage());
    }

    @Test
    @DisplayName(
            "Should throw IllegalArgumentException when setAuthorizationServer is called with null value")
    void testItThrowsIllegalArgumentExceptionOnNullAuthorizationServer() {
        metadataBuilder = new MetadataBuilder();
        IllegalArgumentException thrown =
                assertThrows(
                        IllegalArgumentException.class,
                        () -> metadataBuilder.setAuthorizationServer(null));
        Assertions.assertEquals("authorization_server must not be null", thrown.getMessage());
    }

    @Test
    @DisplayName(
            "Should throw IllegalArgumentException when setCredentialIssuer is called with null value")
    void testItThrowsIllegalArgumentExceptionOnNullCredentialIssuer() {
        metadataBuilder = new MetadataBuilder();
        IllegalArgumentException thrown =
                assertThrows(
                        IllegalArgumentException.class,
                        () -> metadataBuilder.setCredentialIssuer(null));
        Assertions.assertEquals("credential_issuer must not be null", thrown.getMessage());
    }
}
