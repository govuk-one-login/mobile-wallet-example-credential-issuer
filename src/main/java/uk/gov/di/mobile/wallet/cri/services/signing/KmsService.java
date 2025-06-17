package uk.gov.di.mobile.wallet.cri.services.signing;

import com.nimbusds.jose.jwk.ECKey;
import com.nimbusds.jose.jwk.KeyUse;
import org.bouncycastle.asn1.x509.SubjectPublicKeyInfo;
import org.bouncycastle.openssl.PEMException;
import org.bouncycastle.openssl.jcajce.JcaPEMKeyConverter;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import software.amazon.awssdk.arns.Arn;
import software.amazon.awssdk.auth.credentials.DefaultCredentialsProvider;
import software.amazon.awssdk.regions.Region;
import software.amazon.awssdk.services.kms.KmsClient;
import software.amazon.awssdk.services.kms.model.DescribeKeyRequest;
import software.amazon.awssdk.services.kms.model.DescribeKeyResponse;
import software.amazon.awssdk.services.kms.model.GetPublicKeyRequest;
import software.amazon.awssdk.services.kms.model.GetPublicKeyResponse;
import software.amazon.awssdk.services.kms.model.NotFoundException;
import software.amazon.awssdk.services.kms.model.SignRequest;
import software.amazon.awssdk.services.kms.model.SignResponse;
import uk.gov.di.mobile.wallet.cri.annotations.ExcludeFromGeneratedCoverageReport;
import uk.gov.di.mobile.wallet.cri.services.ConfigurationService;

import java.net.URI;
import java.security.NoSuchAlgorithmException;
import java.security.PublicKey;
import java.security.interfaces.ECPublicKey;

import static com.nimbusds.jose.JWSAlgorithm.ES256;
import static com.nimbusds.jose.jwk.Curve.P_256;
import static uk.gov.di.mobile.wallet.cri.util.ArnUtil.extractKeyId;

public class KmsService implements KeyProvider {

    private final KmsClient kmsClient;
    private static final Logger LOGGER = LoggerFactory.getLogger(KmsService.class);

    @ExcludeFromGeneratedCoverageReport
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

    public DescribeKeyResponse describeKey(DescribeKeyRequest describeKeyRequest) {
        return kmsClient.describeKey(describeKeyRequest);
    }

    public String getKeyId(String keyAlias) {
        DescribeKeyRequest describeKeyRequest =
                DescribeKeyRequest.builder().keyId(keyAlias).build();
        DescribeKeyResponse describeKeyResponse = describeKey(describeKeyRequest);
        return describeKeyResponse.keyMetadata().keyId();
    }

    public boolean isKeyActive(String keyAlias) {
        DescribeKeyRequest describeKeyRequest =
                DescribeKeyRequest.builder().keyId(keyAlias).build();
        DescribeKeyResponse describeKeyResponse;

        try {
            describeKeyResponse = describeKey(describeKeyRequest);
        } catch (NotFoundException exception) {
            LOGGER.info("Key with alias {} was not found", keyAlias);
            return false;
        }

        if (Boolean.FALSE.equals(describeKeyResponse.keyMetadata().enabled())) {
            LOGGER.info("Key with alias {} is disabled", keyAlias);
            return false;
        }

        if (describeKeyResponse.keyMetadata().deletionDate() != null) {
            LOGGER.info("Key with alias {} is due for deletion", keyAlias);
            return false;
        }

        return true;
    }

    public ECKey getPublicKey(String keyAlias) throws PEMException, NoSuchAlgorithmException {
        GetPublicKeyResponse publicKeyResponse = getKmsPublicKey(keyAlias);
        return createJwk(publicKeyResponse);
    }

    public GetPublicKeyResponse getKmsPublicKey(String keyAlias) {
        return kmsClient.getPublicKey(GetPublicKeyRequest.builder().keyId(keyAlias).build());
    }

    private ECKey createJwk(GetPublicKeyResponse publicKeyResponse)
            throws PEMException, NoSuchAlgorithmException {
        PublicKey publicKey = createPublicKey(publicKeyResponse);
        String keyId = extractKeyId(publicKeyResponse.keyId());
        String hashedKeyId = KeyHelper.hashKeyId(keyId);
        return new ECKey.Builder(P_256, (ECPublicKey) publicKey)
                .keyID(hashedKeyId)
                .algorithm(ES256)
                .keyUse(KeyUse.SIGNATURE)
                .build();
    }

    private PublicKey createPublicKey(GetPublicKeyResponse publicKeyResponse) throws PEMException {
        SubjectPublicKeyInfo subjectKeyInfo =
                SubjectPublicKeyInfo.getInstance(publicKeyResponse.publicKey().asByteArray());
        return new JcaPEMKeyConverter().getPublicKey(subjectKeyInfo);
    }
}
