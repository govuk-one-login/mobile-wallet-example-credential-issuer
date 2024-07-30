package uk.gov.di.mobile.wallet.cri.credential_offer;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import jakarta.inject.Singleton;
import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.Pattern;
import jakarta.ws.rs.GET;
import jakarta.ws.rs.Path;
import jakarta.ws.rs.Produces;
import jakarta.ws.rs.QueryParam;
import jakarta.ws.rs.core.MediaType;
import jakarta.ws.rs.core.Response;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import uk.gov.di.mobile.wallet.cri.models.CredentialOfferCacheItem;
import uk.gov.di.mobile.wallet.cri.services.ConfigurationService;
import uk.gov.di.mobile.wallet.cri.services.data_storage.DataStore;
import uk.gov.di.mobile.wallet.cri.services.data_storage.DataStoreException;
import uk.gov.di.mobile.wallet.cri.services.signing.SigningException;

import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;
import java.security.NoSuchAlgorithmException;
import java.time.Instant;
import java.util.UUID;

@Singleton
@Path("/credential_offer")
public class CredentialOfferResource {

    private final CredentialOfferService credentialOfferService;
    private final ConfigurationService configurationService;
    private final DataStore dataStore;
    private static final Logger LOGGER = LoggerFactory.getLogger(CredentialOfferResource.class);

    private static final String WALLET_SUBJECT_ID_PATTERN =
            "^urn:fdc:wallet\\.account\\.gov\\.uk:2024:[a-zA-Z0-9_-]{43}$";
    private static final String DOCUMENT_ID_PATTERN = "^[a-zA-Z0-9_-]{10,50}$";
    private static final String CREDENTIAL_TYPE_PATTERN = "^[a-zA-Z]{10,100}$";

    public CredentialOfferResource(
            CredentialOfferService credentialOfferService,
            ConfigurationService configurationService,
            DataStore dataStore) {
        this.credentialOfferService = credentialOfferService;
        this.configurationService = configurationService;
        this.dataStore = dataStore;
    }

    @GET
    @Produces(MediaType.APPLICATION_JSON)
    public Response getCredentialOffer(
            @QueryParam("walletSubjectId") @NotEmpty @Pattern(regexp = WALLET_SUBJECT_ID_PATTERN)
                    String walletSubjectId,
            @QueryParam("documentId") @NotEmpty @Pattern(regexp = DOCUMENT_ID_PATTERN)
                    String documentId,
            @QueryParam("credentialType") @NotEmpty @Pattern(regexp = CREDENTIAL_TYPE_PATTERN)
                    String credentialType)
            throws JsonProcessingException {

        UUID uuid = UUID.randomUUID();
        String credentialOfferId = uuid.toString();

        CredentialOffer credentialOffer;
        try {
            credentialOffer =
                    credentialOfferService.buildCredentialOffer(credentialOfferId, credentialType);
        } catch (SigningException | NoSuchAlgorithmException exception) {
            LOGGER.error(
                    "Failed to create credential offer for walletSubjectId {} and documentId {}",
                    walletSubjectId,
                    documentId,
                    exception);
            return buildInternalErrorResponse().build();
        }

        LOGGER.info(
                "Credential offer created for walletSubjectId {} and credentialOfferId {}",
                walletSubjectId,
                credentialOfferId);

        Long credentialOfferTtl =
                Instant.now()
                        .plusSeconds(
                                Long.parseLong(configurationService.getCredentialOfferTtlInSecs()))
                        .getEpochSecond();

        try {
            dataStore.saveCredentialOffer(
                    new CredentialOfferCacheItem(
                            credentialOfferId, documentId, walletSubjectId, credentialOfferTtl));
        } catch (DataStoreException exception) {
            LOGGER.error(
                    "Failed to save credential offer for walletSubjectId {} and credentialOfferId {}",
                    walletSubjectId,
                    documentId,
                    exception);
            return buildInternalErrorResponse().build();
        }

        LOGGER.info(
                "Credential offer saved for walletSubjectId {} and credentialOfferId {}",
                walletSubjectId,
                credentialOfferId);

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

    private Response.ResponseBuilder buildInternalErrorResponse() {
        return Response.status(Response.Status.INTERNAL_SERVER_ERROR)
                .type(MediaType.APPLICATION_JSON_TYPE);
    }
}
