package uk.gov.di.mobile.wallet.cri.services.data_storage;

import software.amazon.awssdk.enhanced.dynamodb.DynamoDbEnhancedClient;
import software.amazon.awssdk.enhanced.dynamodb.DynamoDbTable;
import software.amazon.awssdk.enhanced.dynamodb.TableSchema;
import software.amazon.awssdk.http.urlconnection.UrlConnectionHttpClient;
import software.amazon.awssdk.regions.Region;
import software.amazon.awssdk.services.dynamodb.DynamoDbClient;
import uk.gov.di.mobile.wallet.cri.models.CredentialOfferCacheItem;
import uk.gov.di.mobile.wallet.cri.services.ConfigurationService;

import java.net.URI;

public class DynamoDbService implements DataStore {

    private final DynamoDbTable<CredentialOfferCacheItem> table;

    public DynamoDbService(DynamoDbEnhancedClient dynamoDbEnhancedClient, String tableName) {
        this.table =
                dynamoDbEnhancedClient.table(
                        tableName, TableSchema.fromBean(CredentialOfferCacheItem.class));
    }

    public static DynamoDbEnhancedClient getClient(ConfigurationService configurationService) {
        DynamoDbClient client;

        if (configurationService.getEnvironment().equals("local")) {
            System.out.println("Running app in local environment");

            client = getLocalClient(configurationService);
        } else {
            client = DynamoDbClient.builder().httpClient(UrlConnectionHttpClient.create()).build();
        }
        return DynamoDbEnhancedClient.builder().dynamoDbClient(client).build();
    }

    private static DynamoDbClient getLocalClient(ConfigurationService configurationService) {
        return DynamoDbClient.builder()
                .endpointOverride(URI.create(configurationService.getLocalstackEndpoint()))
                .httpClient(UrlConnectionHttpClient.create())
                .region(Region.of(configurationService.getAwsRegion()))
                .build();
    }

    @Override
    public void saveCredentialOffer(CredentialOfferCacheItem credentialOfferCacheItem)
            throws DataStoreException {
        try {
            table.putItem(credentialOfferCacheItem);
        } catch (Exception exception) {
            throw new DataStoreException(exception);
        }
    }
}
