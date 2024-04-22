package uk.gov.di.mobile.wallet.cri.services.signing;

import software.amazon.awssdk.auth.credentials.DefaultCredentialsProvider;
import software.amazon.awssdk.regions.Region;
import software.amazon.awssdk.services.kms.KmsClient;
import software.amazon.awssdk.services.kms.model.SignRequest;
import software.amazon.awssdk.services.kms.model.SignResponse;
import uk.gov.di.mobile.wallet.cri.services.ConfigurationService;

import java.net.URI;

public class KmsService implements SigningService {

    private final KmsClient kmsClient;

    public KmsService(ConfigurationService configurationService) {
        this(
                configurationService.getLocalstackEndpoint(),
                configurationService.getAwsRegion(),
                configurationService.getEnvironment());
    }

    public KmsService(String localstackEndpoint, String awsRegion, String environment) {
        if (environment.equals("local")) {
            this.kmsClient =
                    KmsClient.builder()
                            .endpointOverride(URI.create(localstackEndpoint))
                            .credentialsProvider(DefaultCredentialsProvider.create())
                            .region(Region.of(awsRegion))
                            .build();
        } else {
            this.kmsClient =
                    KmsClient.builder()
                            .region(Region.of(awsRegion))
                            .credentialsProvider(DefaultCredentialsProvider.create())
                            .build();
        }
    }

    public SignResponse sign(SignRequest signRequest) {
        return kmsClient.sign(signRequest);
    }
}
