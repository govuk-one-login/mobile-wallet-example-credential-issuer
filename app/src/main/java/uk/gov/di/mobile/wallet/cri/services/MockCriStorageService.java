package uk.gov.di.mobile.wallet.cri.services;

import uk.gov.di.mobile.wallet.cri.helpers.CredentialOfferCacheItem;

public class MockCriStorageService {
    private final DynamoDbService<CredentialOfferCacheItem> dynamoDbService;

    public MockCriStorageService(ConfigurationService configurationService) {
        this.dynamoDbService =
                new DynamoDbService<>(
                        DynamoDbService.getClient(configurationService),
                        CredentialOfferCacheItem.class,
                        configurationService.getCriCacheTableName());
    }

    public void saveCredentialOffer(
            String documentId, String credentialIdentifier, String walletSubjectId) {
        dynamoDbService.putItem(
                new CredentialOfferCacheItem(documentId, credentialIdentifier, walletSubjectId));
    }
}
