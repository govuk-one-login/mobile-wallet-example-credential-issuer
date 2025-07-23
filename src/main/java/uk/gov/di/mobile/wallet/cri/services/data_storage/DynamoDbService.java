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
    private final DynamoDbEnhancedClient dynamoDbEnhancedClient;
    private final DynamoDbTable<CachedCredentialOffer> cachedCredentialOfferTable;
    private final DynamoDbTable<StoredCredential> storedCredentialTable;

    public DynamoDbService(DynamoDbEnhancedClient dynamoDbEnhancedClient,
                           String cachedCredentialOfferTable,
                           String storedCredentialTable) {
        this.dynamoDbEnhancedClient = dynamoDbEnhancedClient;
        this.cachedCredentialOfferTable = getTable(CachedCredentialOffer.class, cachedCredentialOfferTable);
        this.storedCredentialTable = getTable(StoredCredential.class, storedCredentialTable);
    }

    private <T> DynamoDbTable<T> getTable(Class<T> beanClass, String tableName) {
        return dynamoDbEnhancedClient.table(tableName, TableSchema.fromBean(beanClass));
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
            cachedCredentialOfferTable.putItem(cachedCredentialOffer);
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
            cachedCredentialOfferTable.updateItem(cachedCredentialOffer);
        } catch (Exception exception) {
            throw new DataStoreException("Error updating credential offer", exception);
        }
    }

    @Override
    public void saveSoredCredential(StoredCredential storedCredential) throws DataStoreException {
        try {
            storedCredentialTable.putItem(storedCredential);
        } catch (Exception exception) {
            throw new DataStoreException("Failed to store credential in DynamoDB", exception);
        }
    }

    @Override
    public StoredCredential getStoredCredential(String partitionValue) throws DataStoreException {
        try {
            return getStoredCredentialByKey(Key.builder().partitionValue(partitionValue).build());
        } catch (Exception exception) {
            throw new DataStoreException("Error fetching credential", exception);
        }
    }

    @Override
    public void deleteSoredCredential(String partitionValue) throws DataStoreException {
        try {
            deleteStoredCredentialByKey(Key.builder().partitionValue(partitionValue).build());
        } catch (Exception exception) {
            throw new DataStoreException("Error deleting credential", exception);
        }
    }

    private CachedCredentialOffer getItemByKey(Key key) {
        return cachedCredentialOfferTable.getItem(key);
    }

    private StoredCredential getStoredCredentialByKey(Key key) {
        return storedCredentialTable.getItem(key);
    }

    private StoredCredential deleteStoredCredentialByKey(Key key) {
        return storedCredentialTable.deleteItem(key);
    }
}
