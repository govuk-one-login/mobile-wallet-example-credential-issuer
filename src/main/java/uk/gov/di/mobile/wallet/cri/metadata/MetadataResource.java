package uk.gov.di.mobile.wallet.cri.metadata;

import jakarta.inject.Singleton;
import jakarta.ws.rs.GET;
import jakarta.ws.rs.Path;
import jakarta.ws.rs.core.MediaType;
import jakarta.ws.rs.core.Response;
import uk.gov.di.mobile.wallet.cri.services.ConfigurationService;

import java.io.IOException;

@Singleton
@Path("/.well-known/openid-credential-issuer")
public class MetadataResource {

    private static final String CREDENTIAL_ENDPOINT = "/credential";
    private static final String CREDENTIALS_SUPPORTED_FILE_NAME = "credentials_supported.json";

    private final ConfigurationService configurationService;
    private final MetadataBuilder metadataBuilder;

    public MetadataResource(
            ConfigurationService configurationService, MetadataBuilder metadataBuilder) {
        this.configurationService = configurationService;
        this.metadataBuilder = metadataBuilder;
    }

    @GET
    public Response getMetadata() {
        try {

            Metadata metadata =
                    metadataBuilder
                            .setCredentialIssuer(configurationService.getExampleCriUrl())
                            .setCredentialsEndpoint(
                                    configurationService.getExampleCriUrl() + CREDENTIAL_ENDPOINT)
                            .setAuthorizationServers(
                                    configurationService.getOneLoginAuthServerUrl())
                            .setCredentialsSupported(CREDENTIALS_SUPPORTED_FILE_NAME)
                            .build();

            return buildSuccessResponse().entity(metadata).build();
        } catch (IllegalArgumentException | IOException exception) {
            System.out.println("An error happened trying to get the metadata: " + exception);
            return buildFailResponse().build();
        }
    }

    private Response.ResponseBuilder buildSuccessResponse() {
        return Response.status(Response.Status.OK).type(MediaType.APPLICATION_JSON_TYPE);
    }

    private Response.ResponseBuilder buildFailResponse() {
        return Response.status(Response.Status.INTERNAL_SERVER_ERROR)
                .type(MediaType.APPLICATION_JSON_TYPE);
    }
}
