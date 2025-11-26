package uk.gov.di.mobile.wallet.cri.credential.mobile_driving_licence.cose;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import testUtils.EcKeyHelper;
import uk.gov.di.mobile.wallet.cri.credential.mobile_driving_licence.cose.constants.COSEEllipticCurves;
import uk.gov.di.mobile.wallet.cri.credential.mobile_driving_licence.cose.constants.COSEKeyTypes;

import java.security.InvalidAlgorithmParameterException;
import java.security.KeyPairGenerator;
import java.security.NoSuchAlgorithmException;
import java.security.interfaces.ECPublicKey;
import java.security.spec.ECGenParameterSpec;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;

class COSEKeyFactoryTest {

    private COSEKeyFactory coseKeyFactory;

    @BeforeEach
    void setUp() {
        coseKeyFactory = new COSEKeyFactory();
    }

    @Test
    void Should_ConvertP256KeyToCOSEKey() throws Exception {
        ECPublicKey ecPublicKey = EcKeyHelper.getEcKey().toECPublicKey();

        COSEKey coseKey = coseKeyFactory.fromECPublicKey(ecPublicKey);

        assertEquals(COSEKeyTypes.EC2, coseKey.keyType(), "Key type should be EC2");
        assertEquals(COSEEllipticCurves.P256, coseKey.curve(), "Curve should be P-256");
        assertEquals(32, coseKey.x().length, "x must be 32 bytes for P-256");
        assertEquals(32, coseKey.y().length, "y must be 32 bytes for P-256");
    }

    @Test
    void Should_ThrowIllegalArgumentException_When_NonP256Curve()
            throws NoSuchAlgorithmException, InvalidAlgorithmParameterException {
        KeyPairGenerator kpg = KeyPairGenerator.getInstance("EC");
        kpg.initialize(new ECGenParameterSpec("secp384r1")); // P-384 curve
        ECPublicKey ecPublicKey = (ECPublicKey) kpg.generateKeyPair().getPublic();

        IllegalArgumentException exception =
                assertThrows(
                        IllegalArgumentException.class,
                        () -> coseKeyFactory.fromECPublicKey(ecPublicKey));
        assertEquals("Invalid key curve - expected P-256", exception.getMessage());
    }
}
