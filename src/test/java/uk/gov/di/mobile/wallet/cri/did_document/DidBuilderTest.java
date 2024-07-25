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
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;

class DidBuilderTest {

    private static final String TEST_PUBLIC_KEY_TYPE = "EC";
    private static final String TEST_DID_TYPE = "JsonWebKey2020";
    private static final String TEST_CONTROLLER = "did:web:localhost:8080";
    private static final String TEST_HASHED_KEY_ID =
            "0ee49f6f7aa27ef1924a735ed9542a85d8be3fb916632adbae584a1c24de91f2";
    private static final String TEST_DID_ID = TEST_CONTROLLER + "#" + TEST_HASHED_KEY_ID;

    @Test
    void shouldReturnDid() throws InvalidAlgorithmParameterException, NoSuchAlgorithmException {
        ECKey testJwk = getTestJwk();

        Did response =
                new DidBuilder()
                        .setType(TEST_DID_TYPE)
                        .setController(TEST_CONTROLLER)
                        .setId(TEST_DID_ID)
                        .setPublicKeyJwk(testJwk)
                        .build();

        assertEquals(TEST_DID_TYPE, response.type);
        assertEquals(TEST_CONTROLLER, response.controller);
        assertEquals(TEST_DID_ID, response.id);
        assertThat(response.publicKeyJwk, instanceOf(PublicKeyJwk.class));
        assertEquals(TEST_HASHED_KEY_ID, response.publicKeyJwk.kid);
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
        KeyPairGenerator gen = KeyPairGenerator.getInstance(TEST_PUBLIC_KEY_TYPE);
        gen.initialize(Curve.P_256.toECParameterSpec());
        KeyPair keyPair = gen.generateKeyPair();

        return new ECKey.Builder(Curve.P_256, (ECPublicKey) keyPair.getPublic())
                .keyID(TEST_HASHED_KEY_ID)
                .algorithm(ES256)
                .build();
    }
}
