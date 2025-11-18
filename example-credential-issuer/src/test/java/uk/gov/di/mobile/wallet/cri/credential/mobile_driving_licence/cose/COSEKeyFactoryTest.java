package uk.gov.di.mobile.wallet.cri.credential.mobile_driving_licence.cose;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import testUtils.EcKeyHelper;
import uk.gov.di.mobile.wallet.cri.credential.mobile_driving_licence.cose.constants.COSEEllipticCurves;
import uk.gov.di.mobile.wallet.cri.credential.mobile_driving_licence.cose.constants.COSEKeyTypes;

import java.security.InvalidAlgorithmParameterException;
import java.security.KeyPairGenerator;
import java.security.NoSuchAlgorithmException;
import java.security.interfaces.ECPublicKey;
import java.security.spec.ECGenParameterSpec;

import static org.junit.jupiter.api.Assertions.assertAll;
import static org.junit.jupiter.api.Assertions.assertArrayEquals;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;

class COSEKeyFactoryTest {

    private COSEKeyFactory coseKeyFactory;

    @BeforeEach
    void setUp() {
        coseKeyFactory = new COSEKeyFactory();
    }

    @DisplayName("Should convert a P-256 ECPublicKey to a COSEKey with 32-byte coordinates")
    @Test
    void shouldConvertRealP256KeyToCOSEKey() throws Exception {
        byte[] expectedY = {
            -101, -83, 16, -38, 66, -12, 45, -72, 71, 74, 91, 92, 41, -118, -53, -93, 75, 53, -25,
            117, -24, -57, -7, -109, -26, -12, 84, -50, 12, 124, 50, 65
        };
        byte[] expectedX = {
            37, -110, 113, 59, -70, 27, 71, -62, 44, -65, -97, 57, 18, -56, -96, -47, -77, -12, 0,
            111, -24, 75, 61, 60, 71, 95, -82, 82, -106, -37, -4, -127
        };
        ECPublicKey ecPublicKey = EcKeyHelper.getEcKey().toECPublicKey();

        COSEKey coseKey = coseKeyFactory.fromECPublicKey(ecPublicKey);

        assertAll(
                () -> assertEquals(COSEKeyTypes.EC2, coseKey.keyType(), "Key type should be EC2"),
                () ->
                        assertEquals(
                                COSEEllipticCurves.P256, coseKey.curve(), "Curve should be P-256"),
                () -> assertEquals(32, coseKey.x().length, "x must be 32 bytes for P-256"),
                () -> assertEquals(32, coseKey.y().length, "y must be 32 bytes for P-256"),
                () -> assertArrayEquals(expectedX, coseKey.x(), "x must match expected value"),
                () -> assertArrayEquals(expectedY, coseKey.y(), "y must match expected value"));
    }

    @DisplayName("Should throw exception when EC public key is not P-256")
    @Test
    void shouldThrowWhenNonP256Curve()
            throws NoSuchAlgorithmException, InvalidAlgorithmParameterException {
        KeyPairGenerator kpg = KeyPairGenerator.getInstance("EC");
        kpg.initialize(new ECGenParameterSpec("secp384r1")); // P-384 curve
        ECPublicKey ecPublicKey = (ECPublicKey) kpg.generateKeyPair().getPublic();

        IllegalArgumentException ex =
                assertThrows(
                        IllegalArgumentException.class,
                        () -> new COSEKeyFactory().fromECPublicKey(ecPublicKey));
        assertEquals(
                "Invalid EC key curve: expected P-256 (secp256r1), got field size 384 bits",
                ex.getMessage());
    }
}
