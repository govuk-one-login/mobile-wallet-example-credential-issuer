package uk.gov.di.mobile.wallet.cri.services.data_storage;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
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

public class DynamoDbService implements DataStore {

    private final String tableName;
    private final DynamoDbEnhancedClient dynamoDbEnhancedClient;
    private static final Logger logger = LoggerFactory.getLogger(DynamoDbService.class);

    public DynamoDbService(DynamoDbEnhancedClient dynamoDbEnhancedClient, String tableName) {
        this.tableName = tableName;
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
        try {
            getTable().putItem(credentialOfferCacheItem);
        } catch (Exception exception) {
            logger.error("Failed to save credential offer", exception);
            throw new DataStoreException("Error saving credential offer", exception);
        }
    }

    @Override
    public CredentialOfferCacheItem getCredentialOffer(String partitionValue)
            throws DataStoreException {
        try {
            return getItemByKey(Key.builder().partitionValue(partitionValue).build());
        } catch (Exception exception) {
            logger.error("Failed to get credential offer", exception);
            throw new DataStoreException("Error fetching credential offer", exception);
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
