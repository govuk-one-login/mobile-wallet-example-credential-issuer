package uk.gov.di.mobile.wallet.cri.credentialOffer;

import com.nimbusds.jwt.SignedJWT;
import uk.gov.di.mobile.wallet.cri.services.ConfigurationService;
import uk.gov.di.mobile.wallet.cri.services.signing.KmsService;
import uk.gov.di.mobile.wallet.cri.services.signing.SigningException;

import java.util.HashMap;
import java.util.Map;

public class CredentialOfferService {

    private final ConfigurationService configurationService;
    private final KmsService kmsService;

    public CredentialOfferService(
            ConfigurationService configurationService, KmsService kmsService) {
        this.configurationService = configurationService;
        this.kmsService = kmsService;
    }

    public CredentialOffer buildCredentialOffer(String credentialIdentifier)
            throws SigningException {
        System.out.println("getCredentialOffer");
        SignedJWT preAuthorizedCode =
                new PreAuthorizedCodeBuilder(configurationService, kmsService)
                        .buildPreAuthorizedCode(credentialIdentifier);

        System.out.println("preAuthorizedCode created");

        String signedJwtString = preAuthorizedCode.serialize();

        Map<String, Map<String, String>> grantsMap = new HashMap<>();
        Map<String, String> preAuthorizedCodeMap = new HashMap<>();

        preAuthorizedCodeMap.put("pre-authorized_code", signedJwtString);
        grantsMap.put("urn:ietf:params:oauth:grant-type:pre-authorized_code", preAuthorizedCodeMap);

        System.out.println("Trying to build Credential Offer");
        return new CredentialOffer(
                configurationService.getMockCriUrl(),
                configurationService.getCredentialTypes(),
                grantsMap);
    }
}
