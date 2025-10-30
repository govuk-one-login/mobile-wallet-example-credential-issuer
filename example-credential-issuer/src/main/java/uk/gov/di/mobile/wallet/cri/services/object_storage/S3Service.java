package uk.gov.di.mobile.wallet.cri.services.object_storage;

import software.amazon.awssdk.auth.credentials.DefaultCredentialsProvider;
import software.amazon.awssdk.core.ResponseInputStream;
import software.amazon.awssdk.regions.Region;
import software.amazon.awssdk.services.s3.S3Client;
import software.amazon.awssdk.services.s3.model.GetObjectRequest;
import software.amazon.awssdk.services.s3.model.GetObjectResponse;
import uk.gov.di.mobile.wallet.cri.services.ConfigurationService;

public class S3Service implements ObjectStore {
    private final S3Client s3Client;

    public S3Service(S3Client s3Client) {
        this.s3Client = s3Client;
    }

    public static S3Client getClient(ConfigurationService configurationService) {
        S3Client client;
        if (configurationService.getEnvironment().equals("local")) {
            client = getLocalClient(configurationService);
        } else {
            client =
                    S3Client.builder()
                            .region(Region.of(configurationService.getAwsRegion()))
                            .credentialsProvider(DefaultCredentialsProvider.builder().build())
                            .build();
        }
        return client;
    }

    private static S3Client getLocalClient(ConfigurationService configurationService) {
        return S3Client.builder()
                .region(Region.of(configurationService.getAwsRegion()))
                .credentialsProvider(DefaultCredentialsProvider.builder().build())
                .forcePathStyle(true) // Required for running locally with localstack
                .build();
    }

    @Override
    public byte[] getObject(String bucketName, String key) throws ObjectStoreException {
        try {
            GetObjectRequest request =
                    GetObjectRequest.builder().bucket(bucketName).key(key).build();
            ResponseInputStream<GetObjectResponse> response = s3Client.getObject(request);
            return response.readAllBytes();
        } catch (Exception exception) {
            throw new ObjectStoreException("Error fetching object from S3", exception);
        }
    }
}
