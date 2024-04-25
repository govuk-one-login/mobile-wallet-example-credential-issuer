package uk.gov.di.mobile.wallet.cri.did_document;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.nimbusds.jose.jwk.Curve;
import com.nimbusds.jose.jwk.ECKey;
import io.dropwizard.testing.junit5.DropwizardExtensionsSupport;
import io.dropwizard.testing.junit5.ResourceExtension;
import jakarta.ws.rs.core.Response;
import org.bouncycastle.openssl.PEMException;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mockito;
import org.mockito.junit.jupiter.MockitoExtension;

import java.security.InvalidAlgorithmParameterException;
import java.security.KeyPair;
import java.security.KeyPairGenerator;
import java.security.NoSuchAlgorithmException;
import java.security.interfaces.ECPublicKey;
import java.util.Collections;
import java.util.List;

import static com.nimbusds.jose.JWSAlgorithm.ES256;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.core.Is.is;
import static org.mockito.Mockito.*;

@ExtendWith(DropwizardExtensionsSupport.class)
@ExtendWith(MockitoExtension.class)
public class DidDocumentResourceTest {

    private static final String TEST_KEY_ID = "1234abcd-12ab-34cd-56ef-1234567890ab";
    private static final String TEST_HASHED_KEY_ID =
            "0ee49f6f7aa27ef1924a735ed9542a85d8be3fb916632adbae584a1c24de91f2";
    private static final String TEST_CONTROLLER = "did:web:localhost:8080";
    private static final String TEST_DID_ID = TEST_CONTROLLER + "#" + TEST_HASHED_KEY_ID;
    private static final List<String> TEST_ASSERTION_METHOD = List.of(TEST_DID_ID);
    private static final List<String> TEST_CONTEXT =
            List.of("https://www.w3.org/ns/did/v1", "https://www.w3.org/ns/security/jwk/v1");
    private static final String TEST_DID_TYPE = "JsonWebKey2020";
    private static final String TEST_PUBLIC_KEY_TYPE = "EC";
    private static final DidDocumentService didDocumentService = mock(DidDocumentService.class);
    private final ResourceExtension EXT =
            ResourceExtension.builder()
                    .addResource(new DidDocumentResource(didDocumentService))
                    .build();

    @BeforeEach
    void setUp() {
        Mockito.reset(didDocumentService);
    }

    @Test
    void shouldReturn500WhenDidDocumentServiceThrowsAnException()
            throws PEMException, NoSuchAlgorithmException {

        doThrow(new NoSuchAlgorithmException("Mock error message", new Exception()))
                .when(didDocumentService)
                .generateDidDocument();

        final Response response = EXT.target("/.well-known/did.json").request().get();

        verify(didDocumentService, Mockito.times(1)).generateDidDocument();
        assertThat(response.getStatus(), is(500));
        reset(didDocumentService);
    }

    @Test
    void shouldReturn200AndDidDocument()
            throws JsonProcessingException,
                    InvalidAlgorithmParameterException,
                    NoSuchAlgorithmException,
                    PEMException {

        DidDocument didDocument = getMockDidDocument();
        when(didDocumentService.generateDidDocument()).thenReturn(didDocument);

        final Response response = EXT.target("/.well-known/did.json").request().get();

        verify(didDocumentService, Mockito.times(1)).generateDidDocument();
        assertThat(response.getStatus(), is(200));
        assertThat(
                response.readEntity(String.class),
                is(new ObjectMapper().writeValueAsString(didDocument)));
    }

    private DidDocument getMockDidDocument()
            throws InvalidAlgorithmParameterException, NoSuchAlgorithmException {
        ECKey testJwk = getTestJwk();
        List<Did> testDid =
                Collections.singletonList(
                        new DidBuilder()
                                .setType(TEST_DID_TYPE)
                                .setController(TEST_CONTROLLER)
                                .setId(TEST_DID_ID)
                                .setPublicKeyJwk(testJwk)
                                .build());

        return new DidDocumentBuilder()
                .setContext(TEST_CONTEXT)
                .setId(TEST_CONTROLLER)
                .setVerificationMethod(testDid)
                .setAssertionMethod(TEST_ASSERTION_METHOD)
                .build();
    }

    private ECKey getTestJwk() throws InvalidAlgorithmParameterException, NoSuchAlgorithmException {
        KeyPairGenerator gen = KeyPairGenerator.getInstance(TEST_PUBLIC_KEY_TYPE);
        gen.initialize(Curve.P_256.toECParameterSpec());
        KeyPair keyPair = gen.generateKeyPair();

        return new ECKey.Builder(Curve.P_256, (ECPublicKey) keyPair.getPublic())
                .keyID(TEST_KEY_ID)
                .algorithm(ES256)
                .build();
    }
}
