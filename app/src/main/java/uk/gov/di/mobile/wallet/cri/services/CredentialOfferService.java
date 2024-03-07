package uk.gov.di.mobile.wallet.cri.services;

import com.nimbusds.jose.JOSEException;
import com.nimbusds.jwt.SignedJWT;
import uk.gov.di.mobile.wallet.cri.helpers.CredentialOffer;
import uk.gov.di.mobile.wallet.cri.helpers.CredentialOfferCacheItem;
import uk.gov.di.mobile.wallet.cri.helpers.PreAuthorizedCodeBuilder;

import java.text.ParseException;
import java.util.HashMap;
import java.util.Map;

public class CredentialOfferService {
    private final ConfigurationService configurationService;
    private final KmsService kmsService;
    private final DynamoDbService<CredentialOfferCacheItem> dynamoDbService;

    public CredentialOfferService(
            ConfigurationService configurationService, KmsService kmsService) {
        this.configurationService = configurationService;
        this.kmsService = kmsService;
        this.dynamoDbService =
                new DynamoDbService<>(
                        DynamoDbService.getClient(configurationService),
                        CredentialOfferCacheItem.class,
                        configurationService.getCriCacheTableName());
    }

    public CredentialOffer buildCredentialOffer(String credentialIdentifier)
            throws ParseException, JOSEException {
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

    public void saveCredentialOffer(
            String credentialIdentifier, String documentId, String walletSubjectId) {
        dynamoDbService.putItem(
                new CredentialOfferCacheItem(credentialIdentifier, documentId, walletSubjectId));
    }
}
