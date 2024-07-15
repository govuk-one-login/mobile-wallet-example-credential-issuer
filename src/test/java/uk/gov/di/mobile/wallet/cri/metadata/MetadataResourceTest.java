package uk.gov.di.mobile.wallet.cri.metadata;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import io.dropwizard.testing.junit5.DropwizardExtensionsSupport;
import io.dropwizard.testing.junit5.ResourceExtension;
import jakarta.ws.rs.core.Response;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.junit.jupiter.MockitoExtension;
import uk.gov.di.mobile.wallet.cri.services.ConfigurationService;

import java.io.IOException;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.Mockito.RETURNS_SELF;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

@ExtendWith(DropwizardExtensionsSupport.class)
@ExtendWith(MockitoExtension.class)
class MetadataResourceTest {

    private static final ConfigurationService configurationService = new ConfigurationService();
    private final MetadataBuilder metadataBuilder = mock(MetadataBuilder.class, RETURNS_SELF);

    private ResourceExtension EXT =
            ResourceExtension.builder()
                    .addResource(new MetadataResource(configurationService, metadataBuilder))
                    .build();

    @Test
    @DisplayName("Should return 200 and the credential metadata")
    void testItReturns200AndCredentialMetadata() throws IOException {
        ObjectMapper objectMapper = new ObjectMapper();

        Metadata testCredentialMetadata = getTestCredentialMetadata(objectMapper);
        when(metadataBuilder.build()).thenReturn(testCredentialMetadata);

        final Response response =
                EXT.target("/.well-known/openid-credential-issuer").request().get();

        JsonNode expectedResponseBodyAsString =
                objectMapper.readTree(objectMapper.writeValueAsString(testCredentialMetadata));
        JsonNode actualResponseBodyAsString =
                objectMapper.readTree(response.readEntity(String.class));

        assertEquals(200, response.getStatus());
        assertEquals(expectedResponseBodyAsString, actualResponseBodyAsString);
    }

    @Test
    @DisplayName("Should return 500 when the Metadata Builder throws IllegalArgumentException")
    void testItReturns500() {
        when(metadataBuilder.build()).thenThrow(IllegalArgumentException.class);

        final Response response =
                EXT.target("/.well-known/openid-credential-issuer").request().get();

        assertEquals(500, response.getStatus());
    }

    private static Metadata getTestCredentialMetadata(ObjectMapper objectMapper)
            throws JsonProcessingException {
        String testCredentialsSupportedString =
                "{\"socialSecurity\": {\"format\": \"test\",\"id\": \"SocialSecurity_JWT\",\"types\": [\"VerifiableCredential\",\"SocialSecurityCredential\"],\"cryptographic_binding_methods_supported\": [\"did\"],\"cryptographic_suites_supported\": [\"ES256K\"],\"display\": [{\"name\": \"National Insurance number\",\"locale\": \"en-GB\",\"background_color\": \"#12107c\",\"text_color\": \"#FFFFFF\"},{\"name\": \"Rhif Yswiriant Gwladol\",\"locale\": \"en-CY\",\"background_color\": \"#12107c\",\"text_color\": \"#FFFFFF\"}],\"credentialSubject\": {\"name\": [{\"nameParts\": [{\"display\": [{\"name\": \"Name\",\"locale\": \"en-GB\"},{\"name\": \"Enw\",\"locale\": \"cy-GB\"}]}]}],\"socialSecurityRecord\": {\"personalNumber\": {\"display\": [{\"name\": \"National Insurance number\",\"locale\": \"en-GB\"},{\"name\": \"Rhif Yswiriant Gwladol\",\"locale\": \"cy-GB\"}]}}}}}}";
        Object testCredentialsSupported =
                objectMapper.readValue(testCredentialsSupportedString, Object.class);
        return new Metadata(
                "https://test-credential-issuer.gov.uk/credential",
                "https://test-authorization-server.gov.uk/auth-server",
                "https://test-credential-issuer.gov.uk",
                testCredentialsSupported);
    }
}
