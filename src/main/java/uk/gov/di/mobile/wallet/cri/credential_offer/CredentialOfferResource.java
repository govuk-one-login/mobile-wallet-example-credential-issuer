package uk.gov.di.mobile.wallet.cri.credential_offer;

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
import uk.gov.di.mobile.wallet.cri.services.data_storage.DataStore;
import uk.gov.di.mobile.wallet.cri.services.data_storage.DataStoreException;
import uk.gov.di.mobile.wallet.cri.services.signing.SigningException;

import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;
import java.util.UUID;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

@Singleton
@Path("/credential_offer")
public class CredentialOfferResource {

    private final CredentialOfferService credentialOfferService;
    private final ConfigurationService configurationService;
    private final DataStore dataStore;

    private static Logger logger = LoggerFactory.getLogger(CredentialOfferResource.class);

    public CredentialOfferResource(
            CredentialOfferService credentialOfferService,
            ConfigurationService configurationService,
            DataStore dataStore) {
        this.credentialOfferService = credentialOfferService;
        this.configurationService = configurationService;
        this.dataStore = dataStore;
    }

    @GET
    public Response getCredentialOffer(
            @QueryParam("walletSubjectId") @NotEmpty String walletSubjectId,
            @QueryParam("documentId") @NotEmpty String documentId)
            throws JsonProcessingException {
        logger.info(
                "Credential Offer request recieved with wsID: {} and docID: {}",
                walletSubjectId,
                documentId);
        UUID uuid = UUID.randomUUID();
        String credentialIdentifier = uuid.toString();

        CredentialOffer credentialOffer;
        try {
            credentialOffer = credentialOfferService.buildCredentialOffer(credentialIdentifier);
            logger.info(
                    "Credential Offer built wsID: {} and docID: {}", walletSubjectId, documentId);
        } catch (SigningException exception) {
            return buildFailResponse().build();
        }

        try {
            dataStore.saveCredentialOffer(
                    new CredentialOfferCacheItem(
                            credentialIdentifier, documentId, walletSubjectId));
            logger.info("Saved into DB wsID: {} and docID: {}", walletSubjectId, documentId);
        } catch (DataStoreException exception) {
            return buildFailResponse().build();
        }

        ObjectMapper mapper = new ObjectMapper();
        String credentialOfferString = mapper.writeValueAsString(credentialOffer);
        String credentialOfferStringEncoded =
                URLEncoder.encode(credentialOfferString, StandardCharsets.UTF_8);

        CredentialOfferUri credentialOfferUri =
                new CredentialOfferUri(
                        configurationService.getWalletUrl(),
                        "/add?credential_offer=",
                        credentialOfferStringEncoded);

        return buildSuccessResponse().entity(credentialOfferUri).build();
    }

    private Response.ResponseBuilder buildSuccessResponse() {
        return Response.status(Response.Status.OK).type(MediaType.APPLICATION_JSON_TYPE);
    }

    private Response.ResponseBuilder buildFailResponse() {
        return Response.status(Response.Status.INTERNAL_SERVER_ERROR)
                .type(MediaType.APPLICATION_JSON_TYPE);
    }
}
