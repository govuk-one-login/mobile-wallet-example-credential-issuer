package uk.gov.di.mobile.wallet.cri.services;

import com.nimbusds.jwt.SignedJWT;
import uk.gov.di.mobile.wallet.cri.helpers.CredentialOfferBuilder;
import uk.gov.di.mobile.wallet.cri.helpers.PreAuthorizedCodeBuilder;


public class CredentialOfferService {
    String credential_issuer;
    String[] credentials;
    Object grants;

    private final ConfigurationService configurationService;
    private final KmsService kmsService;

    public CredentialOfferService(ConfigurationService configurationService, KmsService kmsService) {
        this.configurationService = configurationService;
        this.kmsService = kmsService;
    }

    public Object getCredentialOffer() {
        SignedJWT signedJwt = new PreAuthorizedCodeBuilder(configurationService, kmsService).buildPreAuthorizedCode();

        CredentialOfferBuilder credentialOffer = new CredentialOfferBuilder(
                configurationService.getIssuer(),
                configurationService.getCredentialTypes(),
                signedJwt
        );

        return null;
    }


}


