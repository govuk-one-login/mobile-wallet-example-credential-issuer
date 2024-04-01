package uk.gov.di.mobile.wallet.cri.credential;

import jakarta.inject.Singleton;
import jakarta.validation.constraints.NotEmpty;
import jakarta.ws.rs.*;
import jakarta.ws.rs.core.MediaType;
import jakarta.ws.rs.core.Response;

@Singleton
@Path("/credential")
public class CredentialResource {
    private final CredentialService credentialService;

    public CredentialResource(CredentialService credentialService) {
        this.credentialService = credentialService;
    }

    @POST
    public Response getCredential(
            @HeaderParam("Authorization") @NotEmpty String authorizationHeader,
            Object requestBody) {

        String credential;

        try {
            credential = credentialService.buildCredential(authorizationHeader, requestBody);

        } catch (Exception exception) {
            System.out.println(exception);
            return buildFailResponse().build();
        }

        return buildSuccessResponse().entity(credential).build();
    }

    private Response.ResponseBuilder buildSuccessResponse() {
        return Response.status(Response.Status.OK).type(MediaType.APPLICATION_JSON_TYPE);
    }

    private Response.ResponseBuilder buildFailResponse() {
        return Response.status(Response.Status.INTERNAL_SERVER_ERROR)
                .type(MediaType.APPLICATION_JSON_TYPE);
    }
}
