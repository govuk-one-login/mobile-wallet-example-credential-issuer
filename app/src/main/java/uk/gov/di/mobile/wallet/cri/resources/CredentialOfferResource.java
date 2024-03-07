package uk.gov.di.mobile.wallet.cri.resources;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import jakarta.inject.Singleton;
import jakarta.validation.constraints.NotEmpty;
import jakarta.ws.rs.GET;
import jakarta.ws.rs.Path;
import jakarta.ws.rs.QueryParam;
import uk.gov.di.mobile.wallet.cri.helpers.CredentialOffer;
import uk.gov.di.mobile.wallet.cri.services.CredentialOfferService;

import java.util.UUID;

@Singleton
@Path("/credential_offer")
public class CredentialOfferResource {

    private final CredentialOfferService credentialOfferService;

    public CredentialOfferResource(CredentialOfferService credentialOfferService) {
        this.credentialOfferService = credentialOfferService;
    }

    @GET
    public Object getCredentialOffer(
            @QueryParam("walletSubjectId") @NotEmpty String walletSubjectId,
            @QueryParam("documentId") @NotEmpty String documentId)
            throws JsonProcessingException {

        UUID uuid = UUID.randomUUID();
        String credentialIdentifier = uuid.toString();

        CredentialOffer credentialOffer =
                credentialOfferService.buildCredentialOffer(credentialIdentifier);

        credentialOfferService.saveCredentialOffer(
                documentId, credentialIdentifier, walletSubjectId);

        ObjectMapper mapper = new ObjectMapper();

        String jsonString = mapper.writeValueAsString(credentialOffer);
        System.out.println(jsonString);

        return null;
    }
}
