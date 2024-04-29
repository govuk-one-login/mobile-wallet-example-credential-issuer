package uk.gov.di.mobile.wallet.cri.did_document;

import com.nimbusds.jose.jwk.Curve;
import com.nimbusds.jose.jwk.ECKey;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

import java.security.InvalidAlgorithmParameterException;
import java.security.KeyPair;
import java.security.KeyPairGenerator;
import java.security.NoSuchAlgorithmException;
import java.security.interfaces.ECPublicKey;
import java.util.Collections;
import java.util.List;

import static com.nimbusds.jose.JWSAlgorithm.ES256;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.instanceOf;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;

public class DidDocumentBuilderTest {
    private static final String TEST_PUBLIC_KEY_TYPE = "EC";
    private static final String TEST_KEY_ID = "1234abcd-12ab-34cd-56ef-1234567890ab";
    private static final String TEST_HASHED_KEY_ID =
            "0ee49f6f7aa27ef1924a735ed9542a85d8be3fb916632adbae584a1c24de91f2";
    private static final String TEST_DID_TYPE = "JsonWebKey2020";
    private static final List<String> TEST_CONTEXT =
            List.of("https://www.w3.org/ns/did/v1", "https://www.w3.org/ns/security/jwk/v1");
    private static final String TEST_CONTROLLER = "did:web:localhost:8080";
    private static final String TEST_DID_ID = TEST_CONTROLLER + "#" + TEST_HASHED_KEY_ID;

    @Test
    void shouldReturnDidDocument()
            throws InvalidAlgorithmParameterException, NoSuchAlgorithmException {
        List<String> testAssertionMethod = Collections.singletonList("test_assertion_method");
        ECKey testJwk = getTestJwk();
        List<Did> testVerificationMethod =
                Collections.singletonList(
                        new DidBuilder()
                                .setType(TEST_DID_TYPE)
                                .setController(TEST_CONTROLLER)
                                .setId(TEST_DID_ID)
                                .setPublicKeyJwk(testJwk, TEST_HASHED_KEY_ID)
                                .build());

        DidDocument response =
                new DidDocumentBuilder()
                        .setContext(TEST_CONTEXT)
                        .setId(TEST_CONTROLLER)
                        .setVerificationMethod(testVerificationMethod)
                        .setAssertionMethod(testAssertionMethod)
                        .build();

        assertEquals(TEST_CONTEXT, response.context);
        assertEquals(TEST_CONTROLLER, response.id);
        assertEquals(testAssertionMethod, response.assertionMethod);
        assertThat(response.verificationMethod.get(0), instanceOf(Did.class));
    }

    @Test
    void shouldThrowIllegalArgumentExceptionOnNullVerificationMethod() {
        DidDocumentBuilder didDocumentBuilder = new DidDocumentBuilder();
        IllegalArgumentException thrown =
                assertThrows(
                        IllegalArgumentException.class,
                        () -> didDocumentBuilder.setVerificationMethod(null));
        Assertions.assertEquals("verificationMethod must not be null", thrown.getMessage());
    }

    @Test
    void shouldThrowIllegalArgumentExceptionOnNullId() {
        DidDocumentBuilder didDocumentBuilder = new DidDocumentBuilder();
        IllegalArgumentException thrown =
                assertThrows(IllegalArgumentException.class, () -> didDocumentBuilder.setId(null));
        Assertions.assertEquals("id must not be null", thrown.getMessage());
    }

    @Test
    void shouldThrowIllegalArgumentExceptionOnNullAssertionMethod() {
        DidDocumentBuilder didDocumentBuilder = new DidDocumentBuilder();
        IllegalArgumentException thrown =
                assertThrows(
                        IllegalArgumentException.class,
                        () -> didDocumentBuilder.setAssertionMethod(null));
        Assertions.assertEquals("assertionMethod must not be null", thrown.getMessage());
    }

    @Test
    void shouldThrowIllegalArgumentExceptionOnNullContext() {
        DidDocumentBuilder didDocumentBuilder = new DidDocumentBuilder();
        IllegalArgumentException thrown =
                assertThrows(
                        IllegalArgumentException.class, () -> didDocumentBuilder.setContext(null));
        Assertions.assertEquals("context must not be null", thrown.getMessage());
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
