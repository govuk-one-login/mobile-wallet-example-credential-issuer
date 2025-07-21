package uk.gov.di.mobile.wallet.cri.services.data_storage;

import software.amazon.awssdk.enhanced.dynamodb.DynamoDbEnhancedClient;
import software.amazon.awssdk.enhanced.dynamodb.DynamoDbTable;
import software.amazon.awssdk.enhanced.dynamodb.Key;
import software.amazon.awssdk.enhanced.dynamodb.TableSchema;
import software.amazon.awssdk.http.urlconnection.UrlConnectionHttpClient;
import software.amazon.awssdk.regions.Region;
import software.amazon.awssdk.services.dynamodb.DynamoDbClient;
import uk.gov.di.mobile.wallet.cri.models.CachedCredentialOffer;
import uk.gov.di.mobile.wallet.cri.models.StoredCredential;
import uk.gov.di.mobile.wallet.cri.services.ConfigurationService;

import java.net.URI;

public class DynamoDbService implements DataStore {
    private final String tableName;
    private final DynamoDbEnhancedClient dynamoDbEnhancedClient;

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
    public void saveCredentialOffer(CachedCredentialOffer cachedCredentialOffer)
            throws DataStoreException {
        try {
            getTable().putItem(cachedCredentialOffer);
        } catch (Exception exception) {
            throw new DataStoreException("Error saving credential offer", exception);
        }
    }

    @Override
    public CachedCredentialOffer getCredentialOffer(String partitionValue)
            throws DataStoreException {
        try {
            return getItemByKey(Key.builder().partitionValue(partitionValue).build());
        } catch (Exception exception) {
            throw new DataStoreException("Error fetching credential offer", exception);
        }
    }

    @Override
    public void updateCredentialOffer(CachedCredentialOffer cachedCredentialOffer)
            throws DataStoreException {
        try {
            getTable().updateItem(cachedCredentialOffer);
        } catch (Exception exception) {
            throw new DataStoreException("Error updating credential offer", exception);
        }
    }

    private CachedCredentialOffer getItemByKey(Key key) {
        return getTable().getItem(key);
    }

    private StoredCredential getStoredCredential(Key key) {
        return getStoredCredentialTable().getItem(key);
    }

    private DynamoDbTable<CachedCredentialOffer> getTable() {
        return dynamoDbEnhancedClient.table(
                tableName, TableSchema.fromBean(CachedCredentialOffer.class));
    }

    private DynamoDbTable<StoredCredential> getStoredCredentialTable() {
        return dynamoDbEnhancedClient.table(tableName, TableSchema.fromBean(StoredCredential.class));
    }

    @Override
    public void saveCredential(StoredCredential storedCredential) throws DataStoreException {
        try {
            getStoredCredentialTable().putItem(storedCredential);
        } catch (Exception exception) {
            throw new DataStoreException("Error saving credential", exception);
        }

    }

    @Override
    public StoredCredential getCredential(String partitionValue) throws DataStoreException {
        try {
            return getStoredCredential(Key.builder().partitionValue(partitionValue).build());
        } catch (Exception exception) {
            throw new DataStoreException("Error fetching credential", exception);
        }
    }

    @Override
    public void updateCredential(StoredCredential storedCredential) throws DataStoreException {

    }

}
