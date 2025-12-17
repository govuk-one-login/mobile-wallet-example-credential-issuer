package uk.gov.di.mobile.wallet.cri.metadata;

import jakarta.ws.rs.core.Response;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;
import org.mockito.junit.jupiter.MockitoExtension;
import uk.gov.di.mobile.wallet.cri.services.ConfigurationService;

import java.io.IOException;
import java.net.URI;
import java.util.stream.Stream;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertInstanceOf;
import static org.mockito.Mockito.RETURNS_SELF;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class MetadataResourceTest {

    private static final String SELF_URL = "https://credential-issuer.test.gov.uk";
    private static final String AUTH_SERVER_URL = "https://authorization-server.test.gov.uk";

    private final ConfigurationService configurationService = mock(ConfigurationService.class);
    private final MetadataBuilder metadataBuilder = mock(MetadataBuilder.class, RETURNS_SELF);
    private final Metadata metadata = mock(Metadata.class);

    private MetadataResource metadataResource;

    @BeforeEach
    void setUp() {
        when(configurationService.getOneLoginAuthServerUrl()).thenReturn(AUTH_SERVER_URL);
        when(configurationService.getSelfUrl()).thenReturn(URI.create(SELF_URL));
        when(configurationService.getEnvironment()).thenReturn("test");
        when(metadataBuilder.build()).thenReturn(metadata);

        metadataResource = new MetadataResource(configurationService, metadataBuilder);
    }

    @Test
    void Should_Return200AndMetadata() {
        final Response response = metadataResource.getMetadata();

        assertEquals(200, response.getStatus());
        assertEquals(
                "application/json;charset=UTF-8",
                response.getMediaType().toString(),
                "Content-Type should be JSON with UTF-8 charset");
        assertEquals(
                null,
                response.getHeaderString("Cache-Control"),
                "Cache-Control header should not be set");
        assertInstanceOf(
                Metadata.class,
                response.getEntity(),
                "Response entity should be a Metadata object");
    }

    @Test
    void Should_CallAllBuilderMethods() throws IOException {
        metadataResource.getMetadata();

        verify(metadataBuilder).setCredentialIssuer(SELF_URL);
        verify(metadataBuilder).setCredentialEndpoint(SELF_URL + "/credential");
        verify(metadataBuilder).setAuthorizationServers(AUTH_SERVER_URL);
        verify(metadataBuilder).setNotificationEndpoint(SELF_URL + "/notification");
        verify(metadataBuilder).setIacasEndpoint(SELF_URL + "/iacas");
        verify(metadataBuilder)
                .setCredentialConfigurationsSupported("credential_configurations_supported.json");
        verify(metadataBuilder).setDisplay(SELF_URL + "/logo.png");
        verify(metadataBuilder).build();
    }

    @Test
    void Should_Return500_When_ConfigurationServiceThrowsRuntimeException() {
        when(configurationService.getSelfUrl())
                .thenThrow(new RuntimeException("Configuration error"));

        final Response response = metadataResource.getMetadata();

        assertEquals(500, response.getStatus());
    }

    @Test
    void Should_Return500_When_MetadataBuilderThrowsIllegalArgumentException() {
        when(metadataBuilder.build())
                .thenThrow(new IllegalArgumentException("Invalid configuration"));

        final Response response = metadataResource.getMetadata();

        assertEquals(500, response.getStatus());
    }

    @Test
    void Should_Return500_When_MetadataBuilderThrowsRuntimeException() {
        when(metadataBuilder.build()).thenThrow(new RuntimeException("Unexpected error"));

        final Response response = metadataResource.getMetadata();

        assertEquals(500, response.getStatus());
    }

    @ParameterizedTest
    @MethodSource("iacasEndpointScenarios")
    void Should_UseCorrectIacasEndpoint(
            String environment, String selfUrl, String expectedIacasEndpoint, String testName) {
        when(configurationService.getEnvironment()).thenReturn(environment);
        when(configurationService.getSelfUrl()).thenReturn(URI.create(selfUrl));

        final Response response = metadataResource.getMetadata();

        assertEquals(200, response.getStatus());
        verify(metadataBuilder).setIacasEndpoint(expectedIacasEndpoint);
    }

    private static Stream<Arguments> iacasEndpointScenarios() {
        return Stream.of(
                Arguments.of(
                        "staging", // environment
                        "https://credential-issuer.staging.gov.uk", // selfUrl
                        "https://credential-issuer.build.gov.uk/iacas", // expected IACAs endpoint
                        "Should_UseBuildEndpoint_When_StagingEnvironmentAndStagingUrl" // test name
                        ),
                Arguments.of(
                        "staging", // environment
                        "https://credential-issuer.test.gov.uk", // selfUrl
                        "https://credential-issuer.test.gov.uk/iacas", // expected IACAs endpoint
                        "Should_UseSelfUrl_When_StagingEnvironmentAndNonStagingUrl" // test name
                        ),
                Arguments.of(
                        "test", // environment
                        "https://credential-issuer.dev.gov.uk", // selfUrl
                        "https://credential-issuer.dev.gov.uk/iacas", // expected IACAs endpoint
                        "Should_UseSelfUrl_When_NonStagingEnvironment" // test name
                        ));
    }
}
