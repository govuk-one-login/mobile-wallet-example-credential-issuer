package uk.gov.di.mobile.wallet.cri.did_document;

import com.nimbusds.jose.jwk.Curve;
import com.nimbusds.jose.jwk.ECKey;
import org.bouncycastle.openssl.PEMException;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ValueSource;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import uk.gov.di.mobile.wallet.cri.services.ConfigurationService;
import uk.gov.di.mobile.wallet.cri.services.signing.KeyNotActiveException;
import uk.gov.di.mobile.wallet.cri.services.signing.KmsService;

import java.net.URI;
import java.security.InvalidAlgorithmParameterException;
import java.security.KeyPair;
import java.security.KeyPairGenerator;
import java.security.NoSuchAlgorithmException;
import java.security.interfaces.ECPublicKey;
import java.util.List;

import static com.nimbusds.jose.JWSAlgorithm.ES256;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.containsString;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;

@ExtendWith({MockitoExtension.class})
class DidDocumentServiceTest {

    @InjectMocks private DidDocumentService didDocumentService;
    @Mock private KmsService kmsService;
    @Mock private ConfigurationService configurationService;
    private static final String TEST_KEY_ID =
            "0ee49f6f7aa27ef1924a735ed9542a85d8be3fb916632adbae584a1c24de91f2";
    private static final String TEST_CONTROLLER = "did:web:test-example-credential-issuer.gov.uk";
    private static final String TEST_DID_ID = TEST_CONTROLLER + "#" + TEST_KEY_ID;
    private static final List<String> TEST_CONTEXT =
            List.of("https://www.w3.org/ns/did/v1", "https://w3id.org/security/suites/jws-2020/v1");
    private static final String TEST_DID_TYPE = "JsonWebKey2020";
    private static final String TEST_PUBLIC_KEY_TYPE = "EC";

    @Test
    void shouldReturnDidDocument()
            throws PEMException,
                    NoSuchAlgorithmException,
                    InvalidAlgorithmParameterException,
                    KeyNotActiveException {
        when(configurationService.getSigningKeyAlias()).thenReturn("test-signing-key-alias");
        when(configurationService.getSelfUrl())
                .thenReturn(URI.create("https://test-example-credential-issuer.gov.uk"));
        ECKey mockJwk = getMockJwk();
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

    @ParameterizedTest
    @ValueSource(strings = {"Key is Inactive", "Key is not Enabled", "Key is due for deletion"})
    @DisplayName("Should Throw Key Not Active Exception if")
    void should_ThrowKeyNotActiveException(String scenario) {
        when(configurationService.getSigningKeyAlias()).thenReturn("test-signing-key-alias");
        when(configurationService.getSelfUrl())
                .thenReturn(URI.create("https://test-example-credential-issuer.gov.uk"));

        KeyNotActiveException exception =
                assertThrows(
                        KeyNotActiveException.class,
                        () -> didDocumentService.generateDidDocument());
        assertThat(exception.getMessage(), containsString("Public key is not active"));
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
