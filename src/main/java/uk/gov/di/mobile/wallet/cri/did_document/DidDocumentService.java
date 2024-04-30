package uk.gov.di.mobile.wallet.cri.did_document;

import com.nimbusds.jose.jwk.ECKey;
import org.apache.hc.client5.http.utils.Hex;
import org.bouncycastle.openssl.PEMException;
import uk.gov.di.mobile.wallet.cri.services.ConfigurationService;
import uk.gov.di.mobile.wallet.cri.services.signing.KeyService;

import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.Collections;
import java.util.List;

public class DidDocumentService {

    private static final String VERIFICATION_METHOD_TYPE = "JsonWebKey2020";
    private static final String CONTROLLER_PREFIX = "did:web:";
    private static final List<String> CONTEXT =
            List.of("https://www.w3.org/ns/did/v1", "https://www.w3.org/ns/security/jwk/v1");
    private final ConfigurationService configurationService;
    private final KeyService keyService;

    public DidDocumentService(ConfigurationService configurationService, KeyService keyService) {
        this.configurationService = configurationService;
        this.keyService = keyService;
    }

    public DidDocument generateDidDocument()
            throws PEMException, NoSuchAlgorithmException, KeyNotActiveException {

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
            throws PEMException, NoSuchAlgorithmException, KeyNotActiveException {

        if (!keyService.isKeyActive(keyAlias)) {
            throw new KeyNotActiveException("Public key is not active");
        }

        ECKey jwk = keyService.getPublicKey(keyAlias);

        String keyId = jwk.getKeyID();
        String kidHashingAlgorithm = configurationService.getKeyIdHashingAlgorithm();
        MessageDigest messageDigest = MessageDigest.getInstance(kidHashingAlgorithm);
        String hashedKeyId =
                Hex.encodeHexString(messageDigest.digest(keyId.getBytes(StandardCharsets.UTF_8)));

        String id = controller + "#" + hashedKeyId;

        return new DidBuilder()
                .setId(id)
                .setController(controller)
                .setType(VERIFICATION_METHOD_TYPE)
                .setPublicKeyJwk(jwk, hashedKeyId)
                .build();
    }
}
