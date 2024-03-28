package uk.gov.di.mobile.wallet.cri.metadata;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.google.common.io.Resources;
import jakarta.inject.Singleton;
import jakarta.ws.rs.GET;
import jakarta.ws.rs.Path;
import jakarta.ws.rs.core.MediaType;
import jakarta.ws.rs.core.Response;
import uk.gov.di.mobile.wallet.cri.services.ConfigurationService;

import java.io.File;

@Singleton
@Path("/.well-known/openid-credential-issuer")
public class MetadataResource {

    private static final String CREDENTIAL_ENDPOINT = "/credential";
    private static final String CREDENTIALS_SUPPORTED_FILE_NAME = "credentials_supported.json";
    private final ConfigurationService configurationService;

    public MetadataResource(ConfigurationService configurationService) {
        this.configurationService = configurationService;
    }

    @GET
    public Response getMetadata() {
        try {
            File credentialsSupportedFilePath =
                    new File(Resources.getResource(CREDENTIALS_SUPPORTED_FILE_NAME).getPath());

            ObjectMapper mapper = new ObjectMapper();
            Object credentialsSupported =
                    mapper.readValue(credentialsSupportedFilePath, Object.class);

            Metadata metadata = new Metadata();

            metadata.setCredentialsEndpoint(configurationService.getMockCriUrl());
            metadata.setAuthorizationServer(configurationService.getStsStubUrl());
            metadata.setCredentialIssuer(
                    configurationService.getMockCriUrl() + CREDENTIAL_ENDPOINT);
            metadata.setCredentialsSupported(credentialsSupported);

            return buildSuccessResponse().entity(metadata).build();
        } catch (Exception exception) {
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
