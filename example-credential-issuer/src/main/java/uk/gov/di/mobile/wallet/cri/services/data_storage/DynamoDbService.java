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
    private final DynamoDbEnhancedClient dynamoDbEnhancedClient;
    private final DynamoDbTable<CachedCredentialOffer> cachedCredentialOfferTable;
    private final DynamoDbTable<StoredCredential> storedCredentialTable;

    public DynamoDbService(
            DynamoDbEnhancedClient dynamoDbEnhancedClient,
            String cachedCredentialOfferTable,
            String storedCredentialTable) {
        this.dynamoDbEnhancedClient = dynamoDbEnhancedClient;
        this.cachedCredentialOfferTable =
                getTable(CachedCredentialOffer.class, cachedCredentialOfferTable);
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
    public void deleteCredentialOffer(String partitionValue) throws DataStoreException {
        try {
            Key key = Key.builder().partitionValue(partitionValue).build();
            cachedCredentialOfferTable.deleteItem(key);
        } catch (Exception exception) {
            throw new DataStoreException("Error deleting credential offer", exception);
        }
    }

    @Override
    public void saveStoredCredential(StoredCredential storedCredential) throws DataStoreException {
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

    private CachedCredentialOffer getItemByKey(Key key) {
        return cachedCredentialOfferTable.getItem(key);
    }

    private StoredCredential getStoredCredentialByKey(Key key) {
        return storedCredentialTable.getItem(key);
    }
}
