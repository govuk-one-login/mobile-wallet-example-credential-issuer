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

import static com.nimbusds.jose.JWSAlgorithm.ES256;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.instanceOf;
import static org.junit.jupiter.api.Assertions.*;

public class DidBuilderTest {

    @Test
    void shouldReturnDid() throws InvalidAlgorithmParameterException, NoSuchAlgorithmException {
        ECKey testJwk = getTestJwk();

        Did response =
                new DidBuilder()
                        .setType("test_did_type")
                        .setController("test_controller")
                        .setId("test_did_id")
                        .setPublicKeyJwk(testJwk)
                        .build();

        assertEquals("test_did_type", response.type);
        assertEquals("test_controller", response.controller);
        assertEquals("test_did_id", response.id);
        assertThat(response.publicKeyJwk, instanceOf(PublicKeyJwk.class));
        assertEquals(testJwk.getKeyID(), response.publicKeyJwk.kid);
        assertEquals(testJwk.getX().toString(), response.publicKeyJwk.x);
        assertEquals(testJwk.getY().toString(), response.publicKeyJwk.y);
        assertEquals(testJwk.getKeyType().getValue(), response.publicKeyJwk.kty);
        assertEquals(testJwk.getCurve().toString(), response.publicKeyJwk.crv);
    }

    @Test
    void shouldThrowIllegalArgumentExceptionOnNullJwk() {
        DidBuilder didBuilder = new DidBuilder();
        IllegalArgumentException thrown =
                assertThrows(
                        IllegalArgumentException.class, () -> didBuilder.setPublicKeyJwk(null));
        Assertions.assertEquals("jwk must not be null", thrown.getMessage());
    }

    @Test
    void shouldThrowIllegalArgumentExceptionOnNullId() {
        DidBuilder didBuilder = new DidBuilder();
        IllegalArgumentException thrown =
                assertThrows(IllegalArgumentException.class, () -> didBuilder.setId(null));
        Assertions.assertEquals("id must not be null", thrown.getMessage());
    }

    @Test
    void shouldThrowIllegalArgumentExceptionOnNullType() {
        DidBuilder didBuilder = new DidBuilder();
        IllegalArgumentException thrown =
                assertThrows(IllegalArgumentException.class, () -> didBuilder.setType(null));
        Assertions.assertEquals("type must not be null", thrown.getMessage());
    }

    @Test
    void shouldThrowIllegalArgumentExceptionOnNullController() {
        DidBuilder didBuilder = new DidBuilder();
        IllegalArgumentException thrown =
                assertThrows(IllegalArgumentException.class, () -> didBuilder.setController(null));
        Assertions.assertEquals("controller must not be null", thrown.getMessage());
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
