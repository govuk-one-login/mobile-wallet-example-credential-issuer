package uk.gov.di.mobile.wallet.cri.credential.mobile_driving_licence.cose;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.MockedStatic;
import org.mockito.junit.jupiter.MockitoExtension;
import uk.gov.di.mobile.wallet.cri.credential.mdoc.mobile_driving_licence.cose.BigIntegerToFixedBytes;
import uk.gov.di.mobile.wallet.cri.credential.mdoc.mobile_driving_licence.cose.COSEKey;
import uk.gov.di.mobile.wallet.cri.credential.mdoc.mobile_driving_licence.cose.COSEKeyFactory;
import uk.gov.di.mobile.wallet.cri.credential.mdoc.mobile_driving_licence.cose.constants.COSEEllipticCurves;
import uk.gov.di.mobile.wallet.cri.credential.mdoc.mobile_driving_licence.cose.constants.COSEKeyTypes;

import java.math.BigInteger;
import java.security.interfaces.ECPublicKey;
import java.security.spec.ECFieldFp;
import java.security.spec.ECParameterSpec;
import java.security.spec.ECPoint;
import java.security.spec.EllipticCurve;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.assertArrayEquals;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.Answers.CALLS_REAL_METHODS;
import static org.mockito.Mockito.mockStatic;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class COSEKeyFactoryTest {

    @Mock private ECPublicKey mockEcPublicKey;
    @Mock private ECParameterSpec mockParams;
    @Mock private EllipticCurve mockCurve;
    @Mock private ECFieldFp mockField;
    @Mock private ECPoint mockPoint;

    private COSEKeyFactory coseKeyFactory;

    @BeforeEach
    void setUp() {
        coseKeyFactory = new COSEKeyFactory();
    }

    @Test
    void Should_ConvertP256KeyToCOSEKey() {
        // Arrange: Mock EC key
        when(mockEcPublicKey.getParams()).thenReturn(mockParams);
        when(mockParams.getCurve()).thenReturn(mockCurve);
        when(mockCurve.getField()).thenReturn(mockField);
        when(mockField.getFieldSize()).thenReturn(256); // P-256 curve
        when(mockEcPublicKey.getW()).thenReturn(mockPoint);
        final BigInteger x = new BigInteger("123456789");
        final BigInteger y = new BigInteger("987654321");
        when(mockPoint.getAffineX()).thenReturn(x);
        when(mockPoint.getAffineY()).thenReturn(y);

        // Arrange: Mock static method for converting BigInteger to fixed-length bytes
        final byte[] xBytes = new byte[] {1, 2, 3, 4};
        final byte[] yBytes = new byte[] {5, 6, 7, 8};
        try (MockedStatic<BigIntegerToFixedBytes> mocked =
                mockStatic(BigIntegerToFixedBytes.class, CALLS_REAL_METHODS)) {
            mocked.when(() -> BigIntegerToFixedBytes.bigIntegerToFixedBytes(x, 256))
                    .thenReturn(xBytes);
            mocked.when(() -> BigIntegerToFixedBytes.bigIntegerToFixedBytes(y, 256))
                    .thenReturn(yBytes);

            // Act: Convert EC key to COSEKey
            COSEKey coseKey = coseKeyFactory.fromECPublicKey(mockEcPublicKey);

            // Assert: COSEKey parameters match expectations
            Map<Integer, Object> parameters = coseKey.parameters();
            assertEquals(4, parameters.size(), "COSEKey map should have 4 items");
            assertEquals(COSEKeyTypes.EC2, parameters.get(1), "Key type should be EC2");
            assertEquals(COSEEllipticCurves.P256, parameters.get(-1), "Curve should be P-256");
            assertArrayEquals(xBytes, (byte[]) parameters.get(-2), "x coordinate bytes match");
            assertArrayEquals(yBytes, (byte[]) parameters.get(-3), "y coordinate bytes match");
        }
    }

    @Test
    void Should_ThrowIllegalArgumentException_When_NonP256Curve() {
        // Arrange: Mock EC key
        when(mockEcPublicKey.getParams()).thenReturn(mockParams);
        when(mockParams.getCurve()).thenReturn(mockCurve);
        when(mockCurve.getField()).thenReturn(mockField);
        when(mockField.getFieldSize()).thenReturn(384); // P-384 curve

        // Act & Assert
        IllegalArgumentException exception =
                assertThrows(
                        IllegalArgumentException.class,
                        () -> coseKeyFactory.fromECPublicKey(mockEcPublicKey));
        assertEquals("Invalid key curve - expected P-256", exception.getMessage());
    }
}
