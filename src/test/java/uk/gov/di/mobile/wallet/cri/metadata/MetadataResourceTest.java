package uk.gov.di.mobile.wallet.cri.metadata;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import io.dropwizard.testing.junit5.DropwizardExtensionsSupport;
import io.dropwizard.testing.junit5.ResourceExtension;
import jakarta.ws.rs.core.Response;
import org.junit.jupiter.api.BeforeEach;
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

    private final ConfigurationService configurationService = mock(ConfigurationService.class);
    private final MetadataBuilder metadataBuilder = mock(MetadataBuilder.class, RETURNS_SELF);
    private final ResourceExtension resource =
            ResourceExtension.builder()
                    .addResource(new MetadataResource(configurationService, metadataBuilder))
                    .build();

    @BeforeEach
    void setUp() {
        when(configurationService.getOneLoginAuthServerUrl())
                .thenReturn("https://test-authorization-server.gov.uk");
        when(configurationService.getSelfUrl()).thenReturn("https://test-credential-issuer.gov.uk");
    }

    @Test
    void Should_Return200AndMetadata() throws IOException {
        Metadata mockCriMetadata = getMockCRIMetadata();
        when(metadataBuilder.build()).thenReturn(mockCriMetadata);

        final Response response =
                resource.target("/.well-known/openid-credential-issuer").request().get();

        ObjectMapper objectMapper = new ObjectMapper();
        JsonNode expectedResponseBody =
                objectMapper.readTree(objectMapper.writeValueAsString(mockCriMetadata));
        JsonNode actualResponseBody = objectMapper.readTree(response.readEntity(String.class));
        assertEquals(200, response.getStatus());
        assertEquals(expectedResponseBody, actualResponseBody);
    }

    @Test
    void Should_Return500_When_MetadataBuilderThrowsIllegalArgumentException() {
        when(metadataBuilder.build()).thenThrow(IllegalArgumentException.class);

        final Response response =
                resource.target("/.well-known/openid-credential-issuer").request().get();

        assertEquals(500, response.getStatus());
    }

    private static Metadata getMockCRIMetadata() throws JsonProcessingException {
        ObjectMapper objectMapper = new ObjectMapper();

        Object credentialConfigurationsSupported =
                objectMapper.readValue(
                        "{\"SocialSecurityCredential\":{\"format\":\"jwt_vc_json\",\"credential_definition\":{\"types\":[\"VerifiableCredential\",\"SocialSecurityCredential\"]},\"cryptographic_binding_methods_supported\":[\"did:key\"],\"credential_signing_alg_values_supported\":[\"ES256\"],\"proof_types_supported\":{\"jwt\":{\"proof_signing_alg_values_supported\":[\"ES256\"],\"key_attestations_required\":{}}}},\"BasicDisclosureCredential\":{\"format\":\"jwt_vc_json\",\"credential_definition\":{\"types\":[\"VerifiableCredential\",\"BasicDisclosureCredential\"]},\"cryptographic_binding_methods_supported\":[\"did:key\"],\"credential_signing_alg_values_supported\":[\"ES256\"],\"proof_types_supported\":{\"jwt\":{\"proof_signing_alg_values_supported\":[\"ES256\"],\"key_attestations_required\":{}}}},\"DigitalVeteranCard\":{\"format\":\"jwt_vc_json\",\"credential_definition\":{\"type\":[\"VerifiableCredential\",\"DigitalVeteranCard\"]},\"cryptographic_binding_methods_supported\":[\"did:key\"],\"credential_signing_alg_values_supported\":[\"ES256\"],\"proof_types_supported\":{\"jwt\":{\"proof_signing_alg_values_supported\":[\"ES256\"],\"key_attestations_required\":{}}}},\"org.iso.18013.5.1.mDL\": {\"format\": \"mso_mdoc\",\"doctype\": \"org.iso.18013.5.1.mDL\",\"cryptographic_binding_methods_supported\": [\"cose_key\"],\"credential_signing_alg_values_supported\": [\"ES256\"]}}",
                        Object.class);
        return new Metadata(
                "https://test-credential-issuer.gov.uk",
                "https://test-authorization-server.gov.uk",
                "https://test-credential-issuer.gov.uk/credential",
                "https://test-credential-issuer.gov.uk/notification",
                "https://test-credential-issuer.gov.uk/iacas",
                credentialConfigurationsSupported);
    }
}
