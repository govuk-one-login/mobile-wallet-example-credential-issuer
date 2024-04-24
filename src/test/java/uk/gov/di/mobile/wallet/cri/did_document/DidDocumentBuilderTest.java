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
    @Test
    void shouldReturnDidDocument()
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

        DidDocument response =
                new DidDocumentBuilder()
                        .setContext(testContext)
                        .setId(testController)
                        .setVerificationMethod(testVerificationMethod)
                        .setAssertionMethod(testAssertionMethod)
                        .build();

        assertEquals(testContext, response.context);
        assertEquals(testController, response.id);
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
        KeyPairGenerator gen = KeyPairGenerator.getInstance("EC");
        gen.initialize(Curve.P_256.toECParameterSpec());
        KeyPair keyPair = gen.generateKeyPair();

        return new ECKey.Builder(Curve.P_256, (ECPublicKey) keyPair.getPublic())
                .keyID("test_key_id")
                .algorithm(ES256)
                .build();
    }
}
