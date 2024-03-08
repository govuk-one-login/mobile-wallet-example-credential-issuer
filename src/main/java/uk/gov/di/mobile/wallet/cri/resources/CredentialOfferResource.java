package uk.gov.di.mobile.wallet.cri.resources;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.nimbusds.jose.JOSEException;
import jakarta.inject.Singleton;
import jakarta.validation.constraints.NotEmpty;
import jakarta.ws.rs.GET;
import jakarta.ws.rs.Path;
import jakarta.ws.rs.QueryParam;
import jakarta.ws.rs.core.MediaType;
import jakarta.ws.rs.core.Response;
import uk.gov.di.mobile.wallet.cri.helpers.CredentialOffer;
import uk.gov.di.mobile.wallet.cri.services.ConfigurationService;
import uk.gov.di.mobile.wallet.cri.services.CredentialOfferService;

import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;
import java.text.ParseException;
import java.util.UUID;

@Singleton
@Path("/credential_offer")
public class CredentialOfferResource {

    private final CredentialOfferService credentialOfferService;
    private final ConfigurationService configurationService;

    public CredentialOfferResource(
            CredentialOfferService credentialOfferService,
            ConfigurationService configurationService) {
        this.credentialOfferService = credentialOfferService;
        this.configurationService = configurationService;
    }

    @GET
    public Response getCredentialOffer(
            @QueryParam("walletSubjectId") @NotEmpty String walletSubjectId,
            @QueryParam("documentId") @NotEmpty String documentId)
            throws JsonProcessingException, ParseException, JOSEException {

        UUID uuid = UUID.randomUUID();
        String credentialIdentifier = uuid.toString();

        CredentialOffer credentialOffer =
                credentialOfferService.buildCredentialOffer(credentialIdentifier);

        credentialOfferService.saveCredentialOffer(
                credentialIdentifier, documentId, walletSubjectId);

        ObjectMapper mapper = new ObjectMapper();
        String credentialOfferString = mapper.writeValueAsString(credentialOffer);
        System.out.println(credentialOfferString);

        String credentialOfferUrl =
                configurationService.getWalletUrl()
                        + "/add?credential_offer="
                        + URLEncoder.encode(credentialOfferString, StandardCharsets.UTF_8);

        return buildResponse().entity(credentialOfferUrl).build();
    }

    private Response.ResponseBuilder buildResponse() {
        return Response.status(Response.Status.OK).type(MediaType.APPLICATION_JSON_TYPE);
    }
}
