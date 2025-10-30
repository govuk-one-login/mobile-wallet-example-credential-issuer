package uk.gov.di.mobile.wallet.cri.services.data_storage;

import software.amazon.awssdk.enhanced.dynamodb.DynamoDbEnhancedClient;
import software.amazon.awssdk.enhanced.dynamodb.DynamoDbIndex;
import software.amazon.awssdk.enhanced.dynamodb.DynamoDbTable;
import software.amazon.awssdk.enhanced.dynamodb.Key;
import software.amazon.awssdk.enhanced.dynamodb.TableSchema;
import software.amazon.awssdk.enhanced.dynamodb.model.QueryConditional;
import software.amazon.awssdk.enhanced.dynamodb.model.QueryEnhancedRequest;
import software.amazon.awssdk.http.urlconnection.UrlConnectionHttpClient;
import software.amazon.awssdk.regions.Region;
import software.amazon.awssdk.services.dynamodb.DynamoDbClient;
import uk.gov.di.mobile.wallet.cri.models.CachedCredentialOffer;
import uk.gov.di.mobile.wallet.cri.models.StoredCredential;
import uk.gov.di.mobile.wallet.cri.services.ConfigurationService;

import java.net.URI;
import java.util.List;

public class DynamoDbService implements DataStore {
    private final DynamoDbTable<CachedCredentialOffer> cachedCredentialOfferTable;
    private final DynamoDbTable<StoredCredential> storedCredentialTable;

    public DynamoDbService(
            DynamoDbEnhancedClient dynamoDbEnhancedClient,
            String cachedCredentialOfferTableName,
            String storedCredentialTableName) {
        this.cachedCredentialOfferTable =
                getTable(
                        dynamoDbEnhancedClient,
                        CachedCredentialOffer.class,
                        cachedCredentialOfferTableName);
        this.storedCredentialTable =
                getTable(dynamoDbEnhancedClient, StoredCredential.class, storedCredentialTableName);
    }

    private static <T> DynamoDbTable<T> getTable(
            DynamoDbEnhancedClient client, Class<T> beanClass, String tableName) {
        return client.table(tableName, TableSchema.fromBean(beanClass));
    }

    public static DynamoDbEnhancedClient getClient(ConfigurationService configurationService) {
        DynamoDbClient client =
                configurationService.getEnvironment().equals("local")
                        ? getLocalClient(configurationService)
                        : DynamoDbClient.builder()
                                .httpClient(UrlConnectionHttpClient.create())
                                .build();

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
            Key key = Key.builder().partitionValue(partitionValue).build();
            return cachedCredentialOfferTable.getItem(key);
        } catch (Exception exception) {
            throw new DataStoreException("Error fetching credential offer", exception);
        }
    }

    @Override
    public void deleteCredentialOffer(String partitionValue) throws DataStoreException {
        try {
            Key key = Key.builder().partitionValue(partitionValue).build();
            cachedCredentialOfferTable.deleteItem(key);
        } catch (Exception exception) {
            throw new DataStoreException("Error deleting credential offer", exception);
        }
    }

    @Override
    public void deleteCredential(String partitionValue) throws DataStoreException {
        try {
            Key key = Key.builder().partitionValue(partitionValue).build();
            storedCredentialTable.deleteItem(key);
        } catch (Exception exception) {
            throw new DataStoreException("Error deleting credential", exception);
        }
    }

    @Override
    public void saveStoredCredential(StoredCredential storedCredential) throws DataStoreException {
        try {
            storedCredentialTable.putItem(storedCredential);
        } catch (Exception exception) {
            throw new DataStoreException("Failed to store credential", exception);
        }
    }

    @Override
    public StoredCredential getStoredCredential(String partitionValue) throws DataStoreException {
        try {
            Key key = Key.builder().partitionValue(partitionValue).build();
            return storedCredentialTable.getItem(key);
        } catch (Exception exception) {
            throw new DataStoreException("Error fetching credential", exception);
        }
    }

    @Override
    public List<StoredCredential> getCredentialsByDocumentId(String documentId)
            throws DataStoreException {
        try {
            DynamoDbIndex<StoredCredential> index = storedCredentialTable.index("documentIdIndex");

            QueryEnhancedRequest request =
                    QueryEnhancedRequest.builder()
                            .queryConditional(
                                    QueryConditional.keyEqualTo(
                                            Key.builder().partitionValue(documentId).build()))
                            .build();

            return index.query(request).stream().flatMap(page -> page.items().stream()).toList();

        } catch (Exception exception) {
            throw new DataStoreException("Error fetching credentials by documentId", exception);
        }
    }
}
