package uk.gov.di.mobile.wallet.cri.credential;

import com.fasterxml.jackson.databind.JsonNode;
import com.nimbusds.oauth2.sdk.ParseException;
import com.nimbusds.oauth2.sdk.token.BearerAccessToken;
import jakarta.inject.Singleton;
import jakarta.validation.constraints.NotEmpty;
import jakarta.ws.rs.*;
import jakarta.ws.rs.core.MediaType;
import jakarta.ws.rs.core.Response;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

@Singleton
@Path("/credential")
public class CredentialResource {

    private final CredentialService credentialService;
    private static Logger logger = LoggerFactory.getLogger(CredentialResource.class);

    public CredentialResource(CredentialService credentialService) {
        this.credentialService = credentialService;
    }

    @POST
    public Response getCredential(
            @HeaderParam("Authorization") @NotEmpty String authorizationHeader, JsonNode payload) {

        Credential credential;
        try {
            CredentialRequestBody credentialRequest = CredentialRequestBody.from(payload);
            logger.info("Valid request body");

            BearerAccessToken bearerAccessToken = parseAuthorizationHeader(authorizationHeader);
            logger.info("Valid authorization header");

            credential = credentialService.getCredential(bearerAccessToken, credentialRequest);
        } catch (Exception exception) {
            logger.error("An error happened trying to get the credential: ", exception);
            if (exception instanceof BadRequestException) {
                return buildBadRequestResponse().entity(exception.getMessage()).build();
            }

            return buildFailResponse().build();
        }

        return buildSuccessResponse().entity(credential).build();
    }

    private BearerAccessToken parseAuthorizationHeader(String authorizationHeader) {
        try {
            return BearerAccessToken.parse(authorizationHeader);
        } catch (ParseException exception) {
            throw new BadRequestException("Invalid authorization header");
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
