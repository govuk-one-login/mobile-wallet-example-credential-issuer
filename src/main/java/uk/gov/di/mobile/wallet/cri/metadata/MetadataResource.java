package uk.gov.di.mobile.wallet.cri.metadata;

import jakarta.inject.Singleton;
import jakarta.ws.rs.GET;
import jakarta.ws.rs.Path;
import jakarta.ws.rs.core.MediaType;
import jakarta.ws.rs.core.Response;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import uk.gov.di.mobile.wallet.cri.services.ConfigurationService;

@Singleton
@Path("/.well-known/openid-credential-issuer")
public class MetadataResource {

    private static final String CREDENTIAL_ENDPOINT = "/credential";
    private static final String CREDENTIALS_SUPPORTED_FILE_NAME = "credentials_supported.json";
    private final ConfigurationService configurationService;
    private final MetadataBuilder metadataBuilder;
    private static final Logger LOGGER = LoggerFactory.getLogger(MetadataResource.class);

    public MetadataResource(
            ConfigurationService configurationService, MetadataBuilder metadataBuilder) {
        this.configurationService = configurationService;
        this.metadataBuilder = metadataBuilder;
    }

    @GET
    public Response getMetadata() {
        try {

            String selfUrl = configurationService.getSelfUrl();

            Metadata metadata =
                    metadataBuilder
                            .setCredentialIssuer(selfUrl)
                            .setCredentialsEndpoint(selfUrl + CREDENTIAL_ENDPOINT)
                            .setAuthorizationServers(
                                    configurationService.getOneLoginAuthServerUrl())
                            .setCredentialsSupported(CREDENTIALS_SUPPORTED_FILE_NAME)
                            .build();

            return buildSuccessResponse().entity(metadata).build();
        } catch (Exception exception) {
            LOGGER.error("An error happened trying to get the metadata: ", exception);
            return buildInternalErrorResponse().build();
        }
    }

    private Response.ResponseBuilder buildSuccessResponse() {
        return Response.status(Response.Status.OK).type(MediaType.APPLICATION_JSON_TYPE);
    }

    private Response.ResponseBuilder buildInternalErrorResponse() {
        return Response.status(Response.Status.INTERNAL_SERVER_ERROR)
                .type(MediaType.APPLICATION_JSON_TYPE);
    }
}
