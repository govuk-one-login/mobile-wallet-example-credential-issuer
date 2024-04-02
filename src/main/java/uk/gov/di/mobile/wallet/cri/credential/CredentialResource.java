package uk.gov.di.mobile.wallet.cri.credential;

import com.fasterxml.jackson.databind.JsonNode;
import com.nimbusds.oauth2.sdk.ParseException;
import com.nimbusds.oauth2.sdk.token.BearerAccessToken;
import jakarta.inject.Singleton;
import jakarta.validation.constraints.NotEmpty;
import jakarta.ws.rs.*;
import jakarta.ws.rs.core.MediaType;
import jakarta.ws.rs.core.Response;
import uk.gov.di.mobile.wallet.cri.services.data_storage.DataStoreException;
import uk.gov.di.mobile.wallet.cri.services.signing.SigningException;

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

        Credential verifiableCredential;
        try {
            CredentialRequest credentialRequest = CredentialRequest.from(payload);
            BearerAccessToken bearerAccessToken = parseAuthorizationHeader(authorizationHeader);

            verifiableCredential = credentialService.run(bearerAccessToken, credentialRequest);

        } catch (BadRequestException
                | AccessTokenValidationException
                | java.text.ParseException
                | DataStoreException
                | ProofJwtValidationException
                | WalletSubjectIdMismatchException
                | NonceMismatchException
                | SigningException
                | java.net.URISyntaxException exception) {
            System.out.println("An error happened trying to build the credential: " + exception);
            if (exception instanceof BadRequestException) {
                return buildBadRequestResponse().build();
            }

            return buildFailResponse().build();
        }

        return buildSuccessResponse().entity(verifiableCredential).build();
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
