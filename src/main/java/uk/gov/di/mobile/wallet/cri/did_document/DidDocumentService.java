package uk.gov.di.mobile.wallet.cri.did_document;

import com.nimbusds.jose.jwk.ECKey;
import org.apache.hc.client5.http.utils.Hex;
import org.bouncycastle.asn1.x509.SubjectPublicKeyInfo;
import org.bouncycastle.openssl.PEMException;
import org.bouncycastle.openssl.jcajce.JcaPEMKeyConverter;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import software.amazon.awssdk.arns.Arn;
import software.amazon.awssdk.services.kms.model.DescribeKeyRequest;
import software.amazon.awssdk.services.kms.model.DescribeKeyResponse;
import software.amazon.awssdk.services.kms.model.GetPublicKeyRequest;
import software.amazon.awssdk.services.kms.model.GetPublicKeyResponse;
import software.amazon.awssdk.services.kms.model.NotFoundException;
import uk.gov.di.mobile.wallet.cri.services.ConfigurationService;
import uk.gov.di.mobile.wallet.cri.services.signing.SigningService;

import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.security.PublicKey;
import java.security.interfaces.ECPublicKey;
import java.util.Collections;
import java.util.List;

import static com.nimbusds.jose.JWSAlgorithm.ES256;
import static com.nimbusds.jose.jwk.Curve.P_256;

public class DidDocumentService {

    private static final String DID_HASHING_ALGORITHM = "SHA-256";
    private static final String VERIFICATION_METHOD_TYPE = "JsonWebKey2020";
    private static final String CONTROLLER_PREFIX = "did:web:";
    private static final List<String> CONTEXT =
            List.of("https://www.w3.org/ns/did/v1", "https://www.w3.org/ns/security/jwk/v1");
    private static final Logger logger = LoggerFactory.getLogger(DidDocumentService.class);
    private final ConfigurationService configurationService;
    private final SigningService signingService;

    public DidDocumentService(
            ConfigurationService configurationService, SigningService signingService) {
        this.configurationService = configurationService;
        this.signingService = signingService;
    }

    public DidDocument generateDidDocument() throws PEMException, NoSuchAlgorithmException {

        String keyAlias = configurationService.getSigningKeyAlias();
        String controller = CONTROLLER_PREFIX + configurationService.getDidController();
        Did did = generateDid(keyAlias, controller);
        List<Did> verificationMethod = Collections.singletonList(did);
        List<String> assertionMethod = Collections.singletonList(did.getId());

        return new DidDocumentBuilder()
                .setContext(CONTEXT)
                .setId(controller)
                .setVerificationMethod(verificationMethod)
                .setAssertionMethod(assertionMethod)
                .build();
    }

    private Did generateDid(String keyAlias, String controller)
            throws PEMException, NoSuchAlgorithmException {

        if (!isKeyActive(keyAlias)) {
            throw new RuntimeException("Public key is not active");
        }

        GetPublicKeyResponse getPublicKeyResponse =
                signingService.getPublicKey(GetPublicKeyRequest.builder().keyId(keyAlias).build());

        String keyId = Arn.fromString(getPublicKeyResponse.keyId()).resource().resource();
        MessageDigest messageDigest = MessageDigest.getInstance(DID_HASHING_ALGORITHM);
        String keyIdHashed =
                Hex.encodeHexString(messageDigest.digest(keyId.getBytes(StandardCharsets.UTF_8)));

        ECKey jwk = createJwk(getPublicKeyResponse, keyIdHashed);
        String id = controller + "#" + keyIdHashed;

        return new DidBuilder()
                .setId(id)
                .setController(controller)
                .setType(VERIFICATION_METHOD_TYPE)
                .setPublicKeyJwk(jwk)
                .build();
    }

    private ECKey createJwk(GetPublicKeyResponse publicKeyResponse, String keyId)
            throws PEMException {
        PublicKey publicKey = createPublicKey(publicKeyResponse);

        return new ECKey.Builder(P_256, (ECPublicKey) publicKey)
                .keyID(keyId)
                .algorithm(ES256)
                .build();
    }

    private PublicKey createPublicKey(GetPublicKeyResponse publicKeyResponse) throws PEMException {
        SubjectPublicKeyInfo subjectKeyInfo =
                SubjectPublicKeyInfo.getInstance(publicKeyResponse.publicKey().asByteArray());

        return new JcaPEMKeyConverter().getPublicKey(subjectKeyInfo);
    }

    private boolean isKeyActive(String keyAlias) {
        DescribeKeyRequest describeKeyRequest =
                DescribeKeyRequest.builder().keyId(keyAlias).build();
        DescribeKeyResponse describeKeyResponse;

        try {
            describeKeyResponse = signingService.describeKey(describeKeyRequest);
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
