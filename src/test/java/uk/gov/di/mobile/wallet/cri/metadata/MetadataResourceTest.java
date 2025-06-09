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
                        "{\"SocialSecurityCredential\":{\"format\":\"jwt_vc_json\",\"credential_definition\":{\"types\":[\"VerifiableCredential\",\"SocialSecurityCredential\"]},\"cryptographic_binding_methods_supported\":[\"did:key\"],\"credential_signing_alg_values_supported\":[\"ES256\"],\"proof_types_supported\":{\"jwt\":{\"proof_signing_alg_values_supported\":[\"ES256\"],\"key_attestations_required\":{}}},\"display\":[{\"name\":\"National Insurance number\",\"locale\":\"en-GB\",\"background_color\":\"#12107c\",\"text_color\":\"#FFFFFF\"},{\"name\":\"Rhif Yswiriant Gwladol\",\"locale\":\"en-CY\",\"background_color\":\"#12107c\",\"text_color\":\"#FFFFFF\"}],\"credentialSubject\":{\"name\":[{\"nameParts\":[{\"display\":[{\"name\":\"Name\",\"locale\":\"en-GB\"},{\"name\":\"Enw\",\"locale\":\"cy-GB\"}]}]}],\"socialSecurityRecord\":{\"personalNumber\":{\"display\":[{\"name\":\"National Insurance number\",\"locale\":\"en-GB\"},{\"name\":\"Rhif Yswiriant Gwladol\",\"locale\":\"cy-GB\"}]}}}},\"BasicDisclosureCredential\":{\"format\":\"jwt_vc_json\",\"credential_definition\":{\"types\":[\"VerifiableCredential\",\"BasicDisclosureCredential\"]},\"cryptographic_binding_methods_supported\":[\"did:key\"],\"credential_signing_alg_values_supported\":[\"ES256\"],\"proof_types_supported\":{\"jwt\":{\"proof_signing_alg_values_supported\":[\"ES256\"],\"key_attestations_required\":{}}},\"display\":[{\"name\":\"Basic DBS disclosure certificate\",\"locale\":\"en-GB\",\"logo\":{\"url\":\"https://issuer.gov.uk/assets/logo.png\",\"alt_text\":\"a square logo of DBS\"},\"background_color\":\"#12107c\",\"text_color\":\"#FFFFFF\"},{\"name\":\"Tystysgrif gwiriad DBS sylfaenol\",\"locale\":\"en-CY\",\"logo\":{\"url\":\"https://issuer.gov.uk/assets/logo.png\",\"alt_text\":\"logo sgwâr o DBS\"},\"background_color\":\"#12107c\",\"text_color\":\"#FFFFFF\"}],\"credentialSubject\":{\"lastName\":{\"display\":[{\"field\":\"Auxiliaryfield1\",\"name\":\"Surname\",\"locale\":\"en-GB\"},{\"field\":\"Auxiliaryfield1\",\"name\":\"Cyfenw\",\"locale\":\"cy-GB\"}]},\"givenName\":{\"display\":[{\"field\":\"Auxiliaryfield2\",\"name\":\"Forename(s)\",\"locale\":\"en-GB\"},{\"field\":\"Auxiliaryfield2\",\"name\":\"Enw(au) cyntaf\",\"locale\":\"cy-GB\"}]},\"dateOfBirth\":{\"display\":[{\"field\":\"Auxiliaryfield3\",\"name\":\"Date of birth\",\"locale\":\"en-GB\"},{\"field\":\"Auxiliaryfield3\",\"name\":\"Dyddiad Geni\",\"locale\":\"cy-GB\"}]},\"firstLineOfAddress\":{\"display\":[{\"field\":\"Auxiliaryfield4\",\"name\":\"First line of address\",\"locale\":\"en-GB\"},{\"field\":\"Auxiliaryfield4\",\"name\":\"Llinell gyntaf y cyfeiriad\",\"locale\":\"cy-GB\"}]},\"basicDisclosureRecord\":{\"outcome\":{\"display\":[{\"field\":\"PrimaryField\",\"name\":\"Outcome\",\"locale\":\"en-GB\"},{\"field\":\"PrimaryField\",\"name\":\"Canlyniad\",\"locale\":\"cy-GB\"}]},\"disclosureDate\":{\"display\":[{\"field\":\"KeyField1\",\"name\":\"Disclosure date\",\"locale\":\"en-GB\"},{\"field\":\"KeyField1\",\"name\":\"Dyddiad datgelu\",\"locale\":\"cy-GB\"}]},\"certificateNumber\":{\"display\":[{\"field\":\"KeyField2\",\"name\":\"Certificate number\",\"locale\":\"en-GB\"},{\"field\":\"KeyField2\",\"name\":\"Rhif tystysgrif\",\"locale\":\"cy-GB\"}]}}}},\"VeteranCardCredential\":{\"format\":\"jwt_vc_json\",\"credential_definition\":{\"type\":[\"VerifiableCredential\",\"VeteranCardCredential\"]},\"cryptographic_binding_methods_supported\":[\"did:key\"],\"credential_signing_alg_values_supported\":[\"ES256\"],\"proof_types_supported\":{\"jwt\":{\"proof_signing_alg_values_supported\":[\"ES256\"],\"key_attestations_required\":{}}},\"display\":[{\"name\":\"HM Armed Forces Veteran Card\",\"locale\":\"en-GB\"}],\"credentialSubject\":{\"firstName\":{\"display\":[{\"name\":\"First name\",\"locale\":\"en-GB\"}]},\"lastName\":{\"display\":[{\"name\":\"Last name\",\"locale\":\"en-GB\"}]},\"birthDate\":{\"display\":[{\"name\":\"Date of birth\",\"locale\":\"en-GB\"}]},\"veteranCard\":{\"expiryDate\":{\"display\":[{\"name\":\"Expiry date\",\"locale\":\"en-GB\"}]},\"serviceNumber\":{\"display\":[{\"name\":\"Service number\",\"locale\":\"en-GB\"}]},\"serviceBranch\":{\"display\":[{\"name\":\"Service branch\",\"locale\":\"en-GB\"}]},\"photo\":{\"display\":[{\"name\":\"Photo\"}]}}}},\"display\":[{\"name\":\"Office of Veteran Affairs\",\"locale\":\"en-GB\",\"logo\":{\"uri\":\"https://[environment.]issuer.veteran-card-service.gov.uk/logo.png\"}}],\"org.iso.18013.5.1.mDL\": {\"format\": \"mso_mdoc\",\"doctype\": \"org.iso.18013.5.1.mDL\",\"cryptographic_binding_methods_supported\": [\"cose_key\"],\"credential_signing_alg_values_supported\": [\"ES256\"]}}",
                        Object.class);
        return new Metadata(
                "https://test-credential-issuer.gov.uk",
                "https://test-authorization-server.gov.uk",
                "https://test-credential-issuer.gov.uk/credential",
                "https://test-credential-issuer.gov.uk/notification",
                credentialConfigurationsSupported);
    }
}
