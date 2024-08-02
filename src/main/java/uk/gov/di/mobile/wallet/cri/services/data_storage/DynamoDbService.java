package uk.gov.di.mobile.wallet.cri.services.data_storage;

import software.amazon.awssdk.enhanced.dynamodb.DynamoDbEnhancedClient;
import software.amazon.awssdk.enhanced.dynamodb.DynamoDbTable;
import software.amazon.awssdk.enhanced.dynamodb.Key;
import software.amazon.awssdk.enhanced.dynamodb.TableSchema;
import software.amazon.awssdk.http.urlconnection.UrlConnectionHttpClient;
import software.amazon.awssdk.regions.Region;
import software.amazon.awssdk.services.dynamodb.DynamoDbClient;
import uk.gov.di.mobile.wallet.cri.models.CredentialOfferCacheItem;
import uk.gov.di.mobile.wallet.cri.services.ConfigurationService;

import java.net.URI;
import java.time.Instant;

public class DynamoDbService implements DataStore {

    private final String tableName;
    private final int credentialOfferTtl;
    private final DynamoDbEnhancedClient dynamoDbEnhancedClient;

    public DynamoDbService(
            DynamoDbEnhancedClient dynamoDbEnhancedClient,
            String tableName,
            int credentialOfferTtl) {
        this.tableName = tableName;
        this.credentialOfferTtl = credentialOfferTtl;
        this.dynamoDbEnhancedClient = dynamoDbEnhancedClient;
    }

    public static DynamoDbEnhancedClient getClient(ConfigurationService configurationService) {
        DynamoDbClient client;
        if (configurationService.getEnvironment().equals("local")) {
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
        credentialOfferCacheItem.setTimeToLive(
                Instant.now().plusSeconds(credentialOfferTtl).getEpochSecond());
        try {
            getTable().putItem(credentialOfferCacheItem);
        } catch (Exception exception) {
            throw new DataStoreException("Error saving credential offer", exception);
        }
    }

    @Override
    public CredentialOfferCacheItem getCredentialOffer(String partitionValue)
            throws DataStoreException {
        try {
            return getItemByKey(Key.builder().partitionValue(partitionValue).build());
        } catch (Exception exception) {
            throw new DataStoreException("Error fetching credential offer", exception);
        }
    }

    @Override
    public void deleteCredentialOffer(String partitionValue) throws DataStoreException {
        try {
            getTable().deleteItem(Key.builder().partitionValue(partitionValue).build());
        } catch (Exception exception) {
            throw new DataStoreException("Error deleting credential offer", exception);
        }
    }

    private CredentialOfferCacheItem getItemByKey(Key key) {
        return getTable().getItem(key);
    }

    private DynamoDbTable<CredentialOfferCacheItem> getTable() {
        return dynamoDbEnhancedClient.table(
                tableName, TableSchema.fromBean(CredentialOfferCacheItem.class));
    }
}
