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
import uk.gov.di.mobile.wallet.cri.credential.domain.RequestBody;
import uk.gov.di.mobile.wallet.cri.credential.exceptions.NonceValidationException;
import uk.gov.di.mobile.wallet.cri.credential.exceptions.ProofJwtValidationException;
import uk.gov.di.mobile.wallet.cri.responses.ErrorMessages;
import uk.gov.di.mobile.wallet.cri.responses.ResponseUtil;
import uk.gov.di.mobile.wallet.cri.services.authentication.AccessTokenValidationException;
import uk.gov.di.mobile.wallet.cri.services.authentication.AuthorizationHeaderMissingException;
import uk.gov.di.mobile.wallet.cri.shared.CredentialOfferException;

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

        try {
            SignedJWT accessToken = parseAuthorizationHeader(authorizationHeader);
            SignedJWT proofJwt = parseRequestBody(payload);

            CredentialResponse credential = credentialService.getCredential(accessToken, proofJwt);
            return ResponseUtil.ok(credential);
        } catch (Exception exception) {
            LOGGER.error("An error happened trying to create a credential: ", exception);
            if (exception instanceof AuthorizationHeaderMissingException) {
                return ResponseUtil.unauthorized();
            }
            if (exception instanceof AccessTokenValidationException
                    || exception instanceof CredentialOfferException) {
                return ResponseUtil.unauthorized(ErrorMessages.INVALID_TOKEN);
            }
            if (exception instanceof ProofJwtValidationException) {
                return ResponseUtil.badRequest(ErrorMessages.INVALID_PROOF);
            }

            if (exception instanceof NonceValidationException) {
                return ResponseUtil.badRequest(ErrorMessages.INVALID_NONCE);
            }
            return ResponseUtil.internalServerError();
        }
    }

    private SignedJWT parseAuthorizationHeader(String authorizationHeader)
            throws AccessTokenValidationException, AuthorizationHeaderMissingException {
        if (authorizationHeader == null || authorizationHeader.isEmpty()) {
            throw new AuthorizationHeaderMissingException();
        }
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
}
