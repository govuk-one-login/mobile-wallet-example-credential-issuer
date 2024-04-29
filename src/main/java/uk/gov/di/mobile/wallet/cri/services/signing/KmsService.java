package uk.gov.di.mobile.wallet.cri.services.signing;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import software.amazon.awssdk.auth.credentials.DefaultCredentialsProvider;
import software.amazon.awssdk.regions.Region;
import software.amazon.awssdk.services.kms.KmsClient;
import software.amazon.awssdk.services.kms.model.*;
import uk.gov.di.mobile.wallet.cri.did_document.DidDocumentService;
import uk.gov.di.mobile.wallet.cri.services.ConfigurationService;

import java.net.URI;

public class KmsService implements KeyService {

    private final KmsClient kmsClient;
    private static final Logger logger = LoggerFactory.getLogger(DidDocumentService.class);

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

    public GetPublicKeyResponse getPublicKey(GetPublicKeyRequest getPublicKeyRequest) {
        return kmsClient.getPublicKey(getPublicKeyRequest);
    }

    public DescribeKeyResponse describeKey(DescribeKeyRequest describeKeyRequest) {
        return kmsClient.describeKey(describeKeyRequest);
    }

    public boolean isKeyActive(String keyAlias) {
        DescribeKeyRequest describeKeyRequest =
                DescribeKeyRequest.builder().keyId(keyAlias).build();
        DescribeKeyResponse describeKeyResponse;

        try {
            describeKeyResponse = describeKey(describeKeyRequest);
        } catch (NotFoundException e) {
            logger.info("Key with alias {} was not found", keyAlias);
            return false;
        }

        if (Boolean.FALSE.equals(describeKeyResponse.keyMetadata().enabled())) {
            logger.info("Key with alias {} is disabled", keyAlias);
            return false;
        }

        if (describeKeyResponse.keyMetadata().deletionDate() != null) {
            logger.info("Key with alias {} is due for deletion", keyAlias);
            return false;
        }

        return true;
    }
}
