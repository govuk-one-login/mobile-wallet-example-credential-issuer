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
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;

class PublicKeyJwkBuilderTest {

    private static final String TEST_KEY_ID =
            "0ee49f6f7aa27ef1924a735ed9542a85d8be3fb916632adbae584a1c24de91f2";
    private static final String TEST_PUBLIC_KEY_TYPE = "EC";

    @Test
    void shouldReturnPublicKeyJwk()
            throws InvalidAlgorithmParameterException, NoSuchAlgorithmException {
        ECKey testJwk = getTestJwk();

        PublicKeyJwk response =
                new PublicKeyJwkBuilder()
                        .setKid(testJwk.getKeyID())
                        .setKty(testJwk.getKeyType().toString())
                        .setCrv(testJwk.getCurve().toString())
                        .setX(testJwk.getX().toString())
                        .setY(testJwk.getY().toString())
                        .setAlg(testJwk.getAlgorithm().toString())
                        .build();

        assertEquals(testJwk.getKeyID(), response.kid);
        assertEquals(testJwk.getKeyType().toString(), response.kty);
        assertEquals(testJwk.getCurve().toString(), response.crv);
        assertEquals(testJwk.getX().toString(), response.x);
        assertEquals(testJwk.getY().toString(), response.y);
        assertEquals(testJwk.getAlgorithm().toString(), response.alg);
    }

    @Test
    void shouldThrowIllegalArgumentExceptionOnNullKid() {
        PublicKeyJwkBuilder publicKeyJwkBuilder = new PublicKeyJwkBuilder();
        IllegalArgumentException thrown =
                assertThrows(
                        IllegalArgumentException.class, () -> publicKeyJwkBuilder.setKid(null));
        Assertions.assertEquals("kid attribute must not be null", thrown.getMessage());
    }

    @Test
    void shouldThrowIllegalArgumentExceptionOnNullKty() {
        PublicKeyJwkBuilder publicKeyJwkBuilder = new PublicKeyJwkBuilder();
        IllegalArgumentException thrown =
                assertThrows(
                        IllegalArgumentException.class, () -> publicKeyJwkBuilder.setKty(null));
        Assertions.assertEquals("kty attribute must not be null", thrown.getMessage());
    }

    @Test
    void shouldThrowIllegalArgumentExceptionOnNullCrv() {
        PublicKeyJwkBuilder publicKeyJwkBuilder = new PublicKeyJwkBuilder();
        IllegalArgumentException thrown =
                assertThrows(
                        IllegalArgumentException.class, () -> publicKeyJwkBuilder.setCrv(null));
        Assertions.assertEquals("crv attribute must not be null", thrown.getMessage());
    }

    @Test
    void shouldThrowIllegalArgumentExceptionOnNullX() {
        PublicKeyJwkBuilder publicKeyJwkBuilder = new PublicKeyJwkBuilder();
        IllegalArgumentException thrown =
                assertThrows(IllegalArgumentException.class, () -> publicKeyJwkBuilder.setX(null));
        Assertions.assertEquals("x attribute must not be null", thrown.getMessage());
    }

    @Test
    void shouldThrowIllegalArgumentExceptionOnNullY() {
        PublicKeyJwkBuilder publicKeyJwkBuilder = new PublicKeyJwkBuilder();
        IllegalArgumentException thrown =
                assertThrows(IllegalArgumentException.class, () -> publicKeyJwkBuilder.setY(null));
        Assertions.assertEquals("y attribute must not be null", thrown.getMessage());
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
