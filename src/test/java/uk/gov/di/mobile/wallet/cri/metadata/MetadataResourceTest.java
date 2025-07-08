package uk.gov.di.mobile.wallet.cri.metadata;

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
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(DropwizardExtensionsSupport.class)
@ExtendWith(MockitoExtension.class)
class MetadataResourceTest {

    private static final String SELF_URL = "https://credential-issuer.test.gov.uk";
    private static final String AUTH_SERVER_URL = "https://authorization-server.test.gov.uk";
    private static final String OPENID_CREDENTIAL_ISSUER_PATH =
            "/.well-known/openid-credential-issuer";

    private final ConfigurationService configurationService = mock(ConfigurationService.class);
    private final MetadataBuilder metadataBuilder = mock(MetadataBuilder.class, RETURNS_SELF);
    private final Metadata metadata = mock(Metadata.class);
    private final ResourceExtension resource =
            ResourceExtension.builder()
                    .addResource(new MetadataResource(configurationService, metadataBuilder))
                    .build();
    private ObjectMapper objectMapper;

    @BeforeEach
    void setUp() {
        objectMapper = new ObjectMapper();

        // Setup common mocks
        when(configurationService.getOneLoginAuthServerUrl()).thenReturn(AUTH_SERVER_URL);
        when(configurationService.getSelfUrl()).thenReturn(SELF_URL);
        when(configurationService.getEnvironment()).thenReturn("test");
        when(metadataBuilder.build()).thenReturn(metadata);
    }

    @Test
    void Should_Return200AndMetadata() throws IOException {
        final Response response = resource.target(OPENID_CREDENTIAL_ISSUER_PATH).request().get();

        assertEquals(200, response.getStatus());
        assertEquals(parseJson(metadata), parseJsonResponse(response));
    }

    @Test
    void Should_CallAllBuilderMethods() throws IOException {
        resource.target(OPENID_CREDENTIAL_ISSUER_PATH).request().get();

        verify(metadataBuilder).setCredentialIssuer(SELF_URL);
        verify(metadataBuilder).setCredentialEndpoint(SELF_URL + "/credential");
        verify(metadataBuilder).setAuthorizationServers(AUTH_SERVER_URL);
        verify(metadataBuilder).setNotificationEndpoint(SELF_URL + "/notification");
        verify(metadataBuilder)
                .setCredentialConfigurationsSupported("credential_configurations_supported.json");
        verify(metadataBuilder).build();
    }

    @Test
    void Should_UseBuildEndpoint_When_StagingEnvironmentAndStagingUrl() {
        when(configurationService.getEnvironment()).thenReturn("staging");
        when(configurationService.getSelfUrl())
                .thenReturn("https://credential-issuer.staging.gov.uk");

        final Response response = resource.target(OPENID_CREDENTIAL_ISSUER_PATH).request().get();

        assertEquals(200, response.getStatus());
        verify(metadataBuilder).setIacasEndpoint("https://credential-issuer.build.gov.uk/iacas");
    }

    @Test
    void Should_UseConfigEndpoint_When_StagingEnvironmentAndNonStagingUrl() {
        when(configurationService.getEnvironment()).thenReturn("staging");
        when(configurationService.getSelfUrl()).thenReturn("https://credential-issuer.test.gov.uk");

        final Response response = resource.target(OPENID_CREDENTIAL_ISSUER_PATH).request().get();

        assertEquals(200, response.getStatus());
        verify(metadataBuilder).setIacasEndpoint("https://credential-issuer.test.gov.uk/iacas");
    }

    @Test
    void Should_UseConfigEndpoint_WhenNonStagingEnvironment() {
        when(configurationService.getEnvironment()).thenReturn("test");
        when(configurationService.getSelfUrl()).thenReturn("https://credential-issuer.dev.gov.uk");

        final Response response = resource.target(OPENID_CREDENTIAL_ISSUER_PATH).request().get();

        assertEquals(200, response.getStatus());
        verify(metadataBuilder).setIacasEndpoint("https://credential-issuer.dev.gov.uk/iacas");
    }

    @Test
    void Should_Return500_When_MetadataBuilderThrowsIllegalArgumentException() {
        when(metadataBuilder.build())
                .thenThrow(new IllegalArgumentException("Invalid configuration"));

        final Response response = resource.target(OPENID_CREDENTIAL_ISSUER_PATH).request().get();

        assertEquals(500, response.getStatus());
    }

    @Test
    void Should_Return500_When_ConfigurationServiceThrowsRuntimeException() {
        when(configurationService.getSelfUrl())
                .thenThrow(new RuntimeException("Configuration error"));

        final Response response = resource.target(OPENID_CREDENTIAL_ISSUER_PATH).request().get();

        assertEquals(500, response.getStatus());
    }

    @Test
    void Should_Return500_When_MetadataBuilderThrowsRuntimeException() {
        when(metadataBuilder.build()).thenThrow(new RuntimeException("Unexpected error"));

        final Response response = resource.target(OPENID_CREDENTIAL_ISSUER_PATH).request().get();

        assertEquals(500, response.getStatus());
    }

    private JsonNode parseJsonResponse(Response response) throws IOException {
        return objectMapper.readTree(response.readEntity(String.class));
    }

    private JsonNode parseJson(Object object) throws IOException {
        return objectMapper.readTree(objectMapper.writeValueAsString(object));
    }
}
