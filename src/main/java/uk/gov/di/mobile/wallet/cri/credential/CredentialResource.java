package uk.gov.di.mobile.wallet.cri.credential;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.DeserializationFeature;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.nimbusds.jwt.SignedJWT;
import com.nimbusds.oauth2.sdk.ParseException;
import com.nimbusds.oauth2.sdk.token.BearerAccessToken;
import jakarta.inject.Singleton;
import jakarta.ws.rs.Consumes;
import jakarta.ws.rs.HeaderParam;
import jakarta.ws.rs.POST;
import jakarta.ws.rs.Path;
import jakarta.ws.rs.Produces;
import jakarta.ws.rs.core.MediaType;
import jakarta.ws.rs.core.Response;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.Objects;

@Singleton
@Produces(MediaType.APPLICATION_JSON)
@Consumes(MediaType.APPLICATION_JSON)
@Path("/credential")
public class CredentialResource {

    private final CredentialService credentialService;
    private static final Logger LOGGER = LoggerFactory.getLogger(CredentialResource.class);

    public CredentialResource(CredentialService credentialService) {
        this.credentialService = credentialService;
    }

    @POST
    public Response getCredential(
            @HeaderParam("Authorization") String authorizationHeader, String payload) {

        Credential credential;
        try {
            SignedJWT accessToken = parseAuthorizationHeader(authorizationHeader);
            SignedJWT proofJwt = parseRequestBody(payload);

            credential = credentialService.getCredential(accessToken, proofJwt);
        } catch (Exception exception) {
            LOGGER.error("An error happened trying to create a credential: ", exception);
            if (exception instanceof AccessTokenValidationException) {
                return buildBadRequestResponse().entity("invalid_credential_request").build();
            }

            if (exception instanceof ProofJwtValidationException) {
                return buildBadRequestResponse().entity("invalid_proof").build();
            }

            return buildInternalErrorResponse().entity("server_error").build();
        }

        return buildSuccessResponse().entity(credential).build();
    }

    private SignedJWT parseAuthorizationHeader(String authorizationHeader)
            throws AccessTokenValidationException {
        try {
            BearerAccessToken bearerAccessToken = BearerAccessToken.parse(authorizationHeader);
            return SignedJWT.parse(bearerAccessToken.getValue());
        } catch (ParseException | java.text.ParseException exception) {
            throw new AccessTokenValidationException(
                    "Failed to parse authorization header as Signed JWT: ", exception);
        }
    }

    private SignedJWT parseRequestBody(String payload) throws ProofJwtValidationException {
        ObjectMapper mapper =
                new ObjectMapper()
                        .configure(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES, true);

        RequestBody requestBody;
        try {
            requestBody = mapper.readValue(payload, RequestBody.class);
        } catch (JsonProcessingException exception) {
            throw new ProofJwtValidationException(
                    "Failed to parse request body as Proof: ", exception);
        }

        if (!Objects.equals(requestBody.getProof().getProofType(), "jwt")) {
            throw new ProofJwtValidationException("Invalid proof type");
        }

        try {
            return SignedJWT.parse(requestBody.getProof().getJwt());
        } catch (java.text.ParseException exception) {
            throw new ProofJwtValidationException(
                    "Failed to parse proof JWT as Signed JWT: ", exception);
        }
    }

    private Response.ResponseBuilder buildSuccessResponse() {
        return Response.status(Response.Status.OK).type(MediaType.APPLICATION_JSON_TYPE);
    }

    private Response.ResponseBuilder buildBadRequestResponse() {
        return Response.status(Response.Status.BAD_REQUEST);
    }

    private Response.ResponseBuilder buildInternalErrorResponse() {
        return Response.status(Response.Status.INTERNAL_SERVER_ERROR);
    }
}
