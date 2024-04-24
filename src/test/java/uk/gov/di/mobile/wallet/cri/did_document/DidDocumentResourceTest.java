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
        List<String> testContext =
                List.of(
                        "https://www.w3.org/ns/did/v1/test",
                        "https://www.w3.org/ns/security/jwk/v1/test");
        String testController = "test_controller";
        List<String> testAssertionMethod = Collections.singletonList("test_assertion_method");
        ECKey testJwk = getTestJwk();
        List<Did> testVerificationMethod =
                Collections.singletonList(
                        new DidBuilder()
                                .setType("test_did_type")
                                .setController("test_controller")
                                .setId("test_did_id")
                                .setPublicKeyJwk(testJwk)
                                .build());

        return new DidDocumentBuilder()
                .setContext(testContext)
                .setId(testController)
                .setVerificationMethod(testVerificationMethod)
                .setAssertionMethod(testAssertionMethod)
                .build();
    }

    private ECKey getTestJwk() throws InvalidAlgorithmParameterException, NoSuchAlgorithmException {
        KeyPairGenerator gen = KeyPairGenerator.getInstance("EC");
        gen.initialize(Curve.P_256.toECParameterSpec());
        KeyPair keyPair = gen.generateKeyPair();

        return new ECKey.Builder(Curve.P_256, (ECPublicKey) keyPair.getPublic())
                .keyID("test_key_id")
                .algorithm(ES256)
                .build();
    }
}
