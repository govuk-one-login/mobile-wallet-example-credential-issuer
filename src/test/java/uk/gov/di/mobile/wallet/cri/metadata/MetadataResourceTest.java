package uk.gov.di.mobile.wallet.cri.metadata;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import io.dropwizard.testing.junit5.DropwizardExtensionsSupport;
import io.dropwizard.testing.junit5.ResourceExtension;
import jakarta.ws.rs.core.Response;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mockito;
import org.mockito.junit.jupiter.MockitoExtension;
import uk.gov.di.mobile.wallet.cri.services.ConfigurationService;

import java.io.IOException;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.Mockito.RETURNS_SELF;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(DropwizardExtensionsSupport.class)
@ExtendWith(MockitoExtension.class)
class MetadataResourceTest {

    private final ConfigurationService configurationService = mock(ConfigurationService.class);
    private final MetadataBuilder metadataBuilder = mock(MetadataBuilder.class, RETURNS_SELF);
    private final Metadata metadata = mock(Metadata.class);
    private final ResourceExtension resource =
            ResourceExtension.builder()
                    .addResource(new MetadataResource(configurationService, metadataBuilder))
                    .build();

    @BeforeEach
    void setUp() {
        when(configurationService.getOneLoginAuthServerUrl())
                .thenReturn("https://test-authorization-server.gov.uk");
        when(configurationService.getSelfUrl()).thenReturn("https://test-credential-issuer.gov.uk");
        when(configurationService.getEnvironment()).thenReturn("test");
    }

    @Test
    void Should_Return200AndMetadata() throws IOException {
        when(metadataBuilder.build()).thenReturn(metadata);

        final Response response =
                resource.target("/.well-known/openid-credential-issuer").request().get();

        ObjectMapper objectMapper = new ObjectMapper();
        JsonNode expectedResponseBody =
                objectMapper.readTree(objectMapper.writeValueAsString(metadata));
        JsonNode actualResponseBody = objectMapper.readTree(response.readEntity(String.class));
        assertEquals(200, response.getStatus());
        assertEquals(expectedResponseBody, actualResponseBody);
    }

    @Test
    void Should_CallMetadataBuilderWithIACAsBuildURL_When_EnvironmentIsStaging() {
        when(metadataBuilder.build()).thenReturn(metadata);
        when(configurationService.getEnvironment()).thenReturn("staging");

        final Response response =
                resource.target("/.well-known/openid-credential-issuer").request().get();

        assertEquals(200, response.getStatus());
        verify(metadataBuilder, Mockito.times(1))
                .setIacasEndpoint(
                        "https://example-credential-issuer.mobile.build.account.gov.uk/iacas");
    }

    @Test
    void Should_CallMetadataBuilderWithIACAsTestURL_When_EnvironmentIsNotStaging() {
        when(metadataBuilder.build()).thenReturn(metadata);
        when(configurationService.getEnvironment()).thenReturn("test");

        final Response response =
                resource.target("/.well-known/openid-credential-issuer").request().get();

        assertEquals(200, response.getStatus());
        verify(metadataBuilder, Mockito.times(1))
                .setIacasEndpoint("https://test-credential-issuer.gov.uk/iacas");
    }

    @Test
    void Should_Return500_When_MetadataBuilderThrowsIllegalArgumentException() {
        when(metadataBuilder.build()).thenThrow(IllegalArgumentException.class);

        final Response response =
                resource.target("/.well-known/openid-credential-issuer").request().get();

        assertEquals(500, response.getStatus());
    }
}
