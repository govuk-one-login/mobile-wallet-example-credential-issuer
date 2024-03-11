package uk.gov.di.mobile.wallet.cri.credentialOffer;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import jakarta.inject.Singleton;
import jakarta.validation.constraints.NotEmpty;
import jakarta.ws.rs.GET;
import jakarta.ws.rs.Path;
import jakarta.ws.rs.QueryParam;
import jakarta.ws.rs.core.MediaType;
import jakarta.ws.rs.core.Response;
import uk.gov.di.mobile.wallet.cri.models.CredentialOfferCacheItem;
import uk.gov.di.mobile.wallet.cri.services.ConfigurationService;
import uk.gov.di.mobile.wallet.cri.services.dataStorage.DataStore;
import uk.gov.di.mobile.wallet.cri.services.dataStorage.DynamoDbService;
import uk.gov.di.mobile.wallet.cri.services.signing.SigningException;

import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;
import java.util.UUID;

@Singleton
@Path("/credential_offer")
public class CredentialOfferResource {

    private final CredentialOfferService credentialOfferService;
    private final ConfigurationService configurationService;
    private final DataStore dataStore;

    public CredentialOfferResource(
            CredentialOfferService credentialOfferService,
            ConfigurationService configurationService) {
        this.credentialOfferService = credentialOfferService;
        this.configurationService = configurationService;
        this.dataStore =
                new DynamoDbService(
                        DynamoDbService.getClient(configurationService),
                        configurationService.getCriCacheTableName());
    }

    @GET
    public Response getCredentialOffer(
            @QueryParam("walletSubjectId") @NotEmpty String walletSubjectId,
            @QueryParam("documentId") @NotEmpty String documentId)
            throws JsonProcessingException {

        UUID uuid = UUID.randomUUID();
        String credentialIdentifier = uuid.toString();

        CredentialOffer credentialOffer;

        try {
            credentialOffer = credentialOfferService.buildCredentialOffer(credentialIdentifier);
        } catch (SigningException exception) {
            System.out.println("Error when building credential offer: " + exception);
            return buildSuccessResponse().build();
        }

        dataStore.saveCredentialOffer(
                new CredentialOfferCacheItem(credentialIdentifier, documentId, walletSubjectId));

        ObjectMapper mapper = new ObjectMapper();
        String credentialOfferString = mapper.writeValueAsString(credentialOffer);
        System.out.println(credentialOfferString);

        String credentialOfferUrl =
                configurationService.getWalletUrl()
                        + "/add?credential_offer="
                        + URLEncoder.encode(credentialOfferString, StandardCharsets.UTF_8);

        return buildSuccessResponse().entity(credentialOfferUrl).build();
    }

    private Response.ResponseBuilder buildSuccessResponse() {
        return Response.status(Response.Status.OK).type(MediaType.APPLICATION_JSON_TYPE);
    }

    private Response.ResponseBuilder buildFailResponse() {
        return Response.status(Response.Status.INTERNAL_SERVER_ERROR)
                .type(MediaType.APPLICATION_JSON_TYPE);
    }
}
