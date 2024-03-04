package uk.gov.di.mobile.wallet.cri.resources;

import jakarta.inject.Singleton;
import jakarta.ws.rs.GET;
import jakarta.ws.rs.Path;
import uk.gov.di.mobile.wallet.cri.services.CredentialOfferService;

@Singleton
@Path("/credential_offer")
public class CredentialOfferResource {

    private final CredentialOfferService credentialOfferService;

    public CredentialOfferResource(CredentialOfferService credentialOfferService) {
        this.credentialOfferService = credentialOfferService;
    }

    @GET
    public Object getCredentialOffer() {
        return credentialOfferService.getCredentialOffer();
    }
}

