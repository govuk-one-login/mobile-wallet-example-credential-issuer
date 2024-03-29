package uk.gov.di.mobile.wallet.cri.credential;

import com.nimbusds.oauth2.sdk.token.BearerAccessToken;
import jakarta.inject.Singleton;
import jakarta.validation.constraints.NotEmpty;
import jakarta.ws.rs.*;
import jakarta.ws.rs.core.MediaType;
import jakarta.ws.rs.core.Response;
import uk.gov.di.mobile.wallet.cri.services.ConfigurationService;
import uk.gov.di.mobile.wallet.cri.services.data_storage.DataStore;

@Singleton
@Path("/credential")
public class CredentialResource {

    private final CredentialService credentialService;
    private final ConfigurationService configurationService;
    private final DataStore dataStore;
    private final TokenService tokenService;

    public CredentialResource(
            CredentialService credentialService,
            ConfigurationService configurationService,
            DataStore dataStore,
            TokenService tokenService) {
        this.credentialService = credentialService;
        this.configurationService = configurationService;
        this.dataStore = dataStore;
        this.tokenService = tokenService;
    }

    @POST
    public Response getCredentialOffer(
            @HeaderParam("Authorization") @NotEmpty String authorizationHeader,
            Object requestBody) {
        Credential credential;
        try {
            BearerAccessToken bearerAccessToken =
                    tokenService.validateAccessToken(authorizationHeader);

            //            SignedJWT signedAccessToken =
            // SignedJWT.parse(bearerAccessToken.getValue());
            //
            //            JWTClaimsSet claimsSet = signedAccessToken.getJWTClaimsSet();
            //            System.out.println(claimsSet);

        } catch (Exception exception) {
            System.out.println(exception);
            // if exception is ParseException, then return UNAUTHORIZED
            return buildFailResponse().build();
        }

        //
        //        ObjectMapper mapper = new ObjectMapper();
        //        String credentialOfferString = mapper.writeValueAsString(credentialOffer);
        //        String credentialOfferStringEncoded =
        //                URLEncoder.encode(credentialOfferString, StandardCharsets.UTF_8);
        //
        //        CredentialOfferUri credentialOfferUri =
        //                new CredentialOfferUri(
        //                        configurationService.getWalletUrl(),
        //                        "/add?credential_offer=",
        //                        credentialOfferStringEncoded);

        return buildSuccessResponse().entity(requestBody).build();
    }

    private Response.ResponseBuilder buildSuccessResponse() {
        return Response.status(Response.Status.OK).type(MediaType.APPLICATION_JSON_TYPE);
    }

    private Response.ResponseBuilder buildFailResponse() {
        return Response.status(Response.Status.INTERNAL_SERVER_ERROR)
                .type(MediaType.APPLICATION_JSON_TYPE);
    }
}
