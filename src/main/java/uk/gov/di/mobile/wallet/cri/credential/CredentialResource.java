package uk.gov.di.mobile.wallet.cri.credential;

import com.fasterxml.jackson.databind.JsonNode;
import com.nimbusds.oauth2.sdk.ParseException;
import com.nimbusds.oauth2.sdk.token.BearerAccessToken;
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
            @HeaderParam("Authorization") @NotEmpty String authorizationHeader, JsonNode payload) {

        String credential;

        CredentialRequest credentialRequest = CredentialRequest.from(payload);
        BearerAccessToken bearerAccessToken = parseAuthorizationHeader(authorizationHeader);

        try {
            credential = credentialService.buildCredential(bearerAccessToken, credentialRequest);

        } catch (Exception exception) {
            System.out.println("EXCEPTION: " + exception);
            if (exception instanceof BadRequestException) {
                return buildBadRequestResponse().build();
            }

            return buildFailResponse().build();
        }

        return buildSuccessResponse().entity(credential).build();
    }

    private BearerAccessToken parseAuthorizationHeader(String authorizationHeader) {
        try {
            return BearerAccessToken.parse(authorizationHeader);
        } catch (ParseException exception) {
            throw new BadRequestException(
                    String.format("Invalid authorization header: %s", exception.getMessage()),
                    exception);
        }
    }

    private Response.ResponseBuilder buildSuccessResponse() {
        return Response.status(Response.Status.OK).type(MediaType.APPLICATION_JSON_TYPE);
    }

    private Response.ResponseBuilder buildBadRequestResponse() {
        return Response.status(Response.Status.BAD_REQUEST);
    }

    private Response.ResponseBuilder buildFailResponse() {
        return Response.status(Response.Status.INTERNAL_SERVER_ERROR);
    }
}
