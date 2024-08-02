package uk.gov.di.mobile.wallet.cri.did_document;

import com.nimbusds.jose.jwk.Curve;
import com.nimbusds.jose.jwk.ECKey;
import org.bouncycastle.openssl.PEMException;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.junit.jupiter.MockitoExtension;
import software.amazon.awssdk.services.kms.model.DescribeKeyRequest;
import software.amazon.awssdk.services.kms.model.DescribeKeyResponse;
import software.amazon.awssdk.services.kms.model.KeyMetadata;
import software.amazon.awssdk.services.kms.model.NotFoundException;
import uk.gov.di.mobile.wallet.cri.services.ConfigurationService;
import uk.gov.di.mobile.wallet.cri.services.signing.KeyNotActiveException;
import uk.gov.di.mobile.wallet.cri.services.signing.KmsService;

import java.security.InvalidAlgorithmParameterException;
import java.security.KeyPair;
import java.security.KeyPairGenerator;
import java.security.NoSuchAlgorithmException;
import java.security.interfaces.ECPublicKey;
import java.time.Instant;
import java.util.List;

import static com.nimbusds.jose.JWSAlgorithm.ES256;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.containsString;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

@ExtendWith({MockitoExtension.class})
class DidDocumentServiceTest {

    private DidDocumentService didDocumentService;
    private final KmsService kmsService = mock(KmsService.class);
    private final ConfigurationService configurationService = mock(ConfigurationService.class);
    private static final String TEST_ARN =
            "arn:aws:kms:eu-west-2:00000000000:key/1234abcd-12ab-34cd-56ef-1234567890ab";
    private static final String TEST_KEY_ID =
            "0ee49f6f7aa27ef1924a735ed9542a85d8be3fb916632adbae584a1c24de91f2";
    private static final String TEST_CONTROLLER = "did:web:test-example-credential-issuer.gov.uk";
    private static final String TEST_DID_ID = TEST_CONTROLLER + "#" + TEST_KEY_ID;
    private static final List<String> TEST_CONTEXT =
            List.of("https://www.w3.org/ns/did/v1", "https://www.w3.org/ns/security/jwk/v1");
    private static final String TEST_DID_TYPE = "JsonWebKey2020";
    private static final String TEST_PUBLIC_KEY_TYPE = "EC";

    @BeforeEach
    void setUp() {
        didDocumentService = new DidDocumentService(configurationService, kmsService);
        when(configurationService.getSigningKeyAlias())
                .thenReturn("test-signing-key-alias");
        when(configurationService.getDidController())
                .thenReturn("test-example-credential-issuer.gov.uk");    }

    @Test
    void shouldReturnDidDocument()
            throws PEMException,
                    NoSuchAlgorithmException,
                    InvalidAlgorithmParameterException,
                    KeyNotActiveException {
        ECKey mockJwk = getMockJwk();
        when(kmsService.describeKey(any(DescribeKeyRequest.class)))
                .thenReturn(getMockDescribeKeyResponse(TEST_ARN, true, null));
        when(kmsService.isKeyActive(any(String.class))).thenReturn(true);
        when(kmsService.getPublicKey(any(String.class))).thenReturn(mockJwk);

        DidDocument didDocument = didDocumentService.generateDidDocument();
        assertEquals(TEST_CONTROLLER, didDocument.getId());
        assertEquals(TEST_CONTEXT, didDocument.getContext());
        assertEquals(1, didDocument.getVerificationMethod().size());
        assertEquals(1, didDocument.getAssertionMethod().size());
        assertEquals(TEST_DID_ID, didDocument.getAssertionMethod().get(0));

        Did did = didDocument.getVerificationMethod().get(0);
        assertEquals(TEST_DID_ID, did.getId());
        assertEquals(TEST_CONTROLLER, did.getController());
        assertEquals(TEST_DID_TYPE, did.getType());

        PublicKeyJwk jwk = did.getPublicKeyJwk();
        assertEquals(TEST_KEY_ID, jwk.getKid());
        assertEquals(mockJwk.getKeyType().toString(), jwk.getKty());
        assertEquals(mockJwk.getCurve().toString(), jwk.getCrv());
        assertEquals(mockJwk.getX().toString(), jwk.getX());
        assertEquals(mockJwk.getY().toString(), jwk.getY());
        assertEquals(mockJwk.getAlgorithm().toString(), jwk.getAlg());
    }

    @Test
    void shouldThrowKeyNotActiveExceptionIfKeyIsInactive() {
        when(kmsService.describeKey(any(DescribeKeyRequest.class)))
                .thenThrow(NotFoundException.class);

        KeyNotActiveException exception =
                assertThrows(
                        KeyNotActiveException.class,
                        () -> didDocumentService.generateDidDocument());
        assertThat(exception.getMessage(), containsString("Public key is not active"));
    }

    @Test
    void shouldThrowKeyNotActiveExceptionIfKeyIsNotEnabled() {
        when(kmsService.describeKey(any(DescribeKeyRequest.class)))
                .thenReturn(getMockDescribeKeyResponse(TEST_ARN, false, null));

        KeyNotActiveException exception =
                assertThrows(
                        KeyNotActiveException.class,
                        () -> didDocumentService.generateDidDocument());
        assertThat(exception.getMessage(), containsString("Public key is not active"));
    }

    @Test
    void shouldThrowKeyNotActiveExceptionIfKeyIsDueForDeletion() {
        when(kmsService.describeKey(any(DescribeKeyRequest.class)))
                .thenReturn(
                        getMockDescribeKeyResponse(TEST_ARN, true, Instant.now().plusSeconds(60)));

        KeyNotActiveException exception =
                assertThrows(
                        KeyNotActiveException.class,
                        () -> didDocumentService.generateDidDocument());
        assertThat(exception.getMessage(), containsString("Public key is not active"));
    }

    public static DescribeKeyResponse getMockDescribeKeyResponse(
            String keyId, boolean enabled, Instant deletionDate) {
        return DescribeKeyResponse.builder()
                .keyMetadata(
                        KeyMetadata.builder()
                                .keyId(keyId)
                                .enabled(enabled)
                                .deletionDate(deletionDate)
                                .build())
                .build();
    }

    private ECKey getMockJwk() throws InvalidAlgorithmParameterException, NoSuchAlgorithmException {
        KeyPairGenerator gen = KeyPairGenerator.getInstance(TEST_PUBLIC_KEY_TYPE);
        gen.initialize(Curve.P_256.toECParameterSpec());
        KeyPair keyPair = gen.generateKeyPair();

        return new ECKey.Builder(Curve.P_256, (ECPublicKey) keyPair.getPublic())
                .keyID(TEST_KEY_ID)
                .algorithm(ES256)
                .build();
    }
}
