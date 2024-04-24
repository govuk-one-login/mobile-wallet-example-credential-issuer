package uk.gov.di.mobile.wallet.cri.did_document;

import com.nimbusds.jose.jwk.ECKey;
import com.nimbusds.jose.jwk.JWK;
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
import uk.gov.di.mobile.wallet.cri.services.signing.KmsService;

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
    private static final String DID_DIGEST_ALGORITHM = "SHA-256";
    private static final String JSON_WEB_KEY = "JsonWebKey2020";
    private static final String CONTROLLER_PREFIX = "did:web:";
    public static final List<String> CONTEXT =
            List.of("https://www.w3.org/ns/did/v1", "https://www.w3.org/ns/security/jwk/v1");
    private static final Logger logger = LoggerFactory.getLogger(DidDocumentService.class);
    private final ConfigurationService configurationService;
    private final KmsService kmsService;

    public DidDocumentService(ConfigurationService configurationService, KmsService kmsService) {
        this.configurationService = configurationService;
        this.kmsService = kmsService;
    }

    public DidDocument generateDIDDocument()
            throws DidDocumentException, PEMException, NoSuchAlgorithmException {
        String signingKeyAlias = configurationService.getSigningKeyAlias();

        String controller = CONTROLLER_PREFIX + configurationService.getDidController();
        Did did = generateDid(signingKeyAlias, controller);

        return new DidDocumentBuilder()
                .setContext(CONTEXT)
                .setId(controller)
                .setVerificationMethod(Collections.singletonList(did))
                .setAssertionMethod(Collections.singletonList(did.getId()))
                .build();
    }

    private Did generateDid(String signingKeyAlias, String controller)
            throws PEMException, NoSuchAlgorithmException {
        if (!isKeyActive(signingKeyAlias)) {
            throw new DidDocumentException("Public key is not active");
        }

        var publicKeyResponse =
                kmsService.getPublicKey(
                        GetPublicKeyRequest.builder().keyId(signingKeyAlias).build());

        String keyId = Arn.fromString(publicKeyResponse.keyId()).resource().resource();
        MessageDigest digest = MessageDigest.getInstance(DID_DIGEST_ALGORITHM);

        String keyIdEncoded =
                Hex.encodeHexString(digest.digest(keyId.getBytes(StandardCharsets.UTF_8)));

        JWK jwk = createJwk(publicKeyResponse, keyIdEncoded);
        String didId = controller + "#" + keyIdEncoded;

        return new DidBuilder()
                .setId(didId)
                .setController(controller)
                .setType(JSON_WEB_KEY)
                .setPublicKeyJwk(jwk)
                .build();
    }

    private JWK createJwk(GetPublicKeyResponse publicKeyResponse, String keyId)
            throws PEMException {
        var publicKey = createPublicKey(publicKeyResponse);
        return new ECKey.Builder(P_256, (ECPublicKey) publicKey)
                .keyID(keyId)
                .algorithm(ES256)
                .build();
    }

    private PublicKey createPublicKey(GetPublicKeyResponse publicKeyResponse) throws PEMException {
        var subjectKeyInfo =
                SubjectPublicKeyInfo.getInstance(publicKeyResponse.publicKey().asByteArray());

        return new JcaPEMKeyConverter().getPublicKey(subjectKeyInfo);
    }

    private boolean isKeyActive(String keyAlias) {
        var describeKeyRequest = DescribeKeyRequest.builder().keyId(keyAlias).build();
        DescribeKeyResponse describeKeyResponse;

        try {
            describeKeyResponse = kmsService.describeKey(describeKeyRequest);
        } catch (NotFoundException e) {
            logger.info("Key with alias {} was not found", keyAlias);
            return false;
        }

        if (Boolean.FALSE.equals(describeKeyResponse.keyMetadata().enabled())) {
            logger.info("Key with alias {} was is disabled", keyAlias);
            return false;
        }

        if (describeKeyResponse.keyMetadata().deletionDate() != null) {
            logger.info("Key with alias {} was is due for deletion", keyAlias);
            return false;
        }

        return true;
    }
}
