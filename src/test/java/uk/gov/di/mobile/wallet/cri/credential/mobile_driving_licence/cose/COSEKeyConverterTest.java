package uk.gov.di.mobile.wallet.cri.credential.mobile_driving_licence.cose;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.math.BigInteger;
import java.security.interfaces.ECPublicKey;
import java.security.spec.ECFieldFp;
import java.security.spec.ECParameterSpec;
import java.security.spec.ECPoint;
import java.security.spec.EllipticCurve;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class COSEKeyConverterTest {

    @Mock private ECPublicKey mockPublicKey;

    @Mock private ECParameterSpec mockParams;

    @Mock private EllipticCurve mockCurve;

    @Mock private ECFieldFp mockField;

    @Mock private ECPoint mockPoint;

    @Test
    void Should_ConvertP256KeyToCOSEKey() {
        BigInteger x =
                new BigInteger(
                        "12345678901234567890123456789012345678901234567890123456789012345678", 16);
        BigInteger y =
                new BigInteger(
                        "98765432109876543210987654321098765432109876543210987654321098765432", 16);
        when(mockPublicKey.getParams()).thenReturn(mockParams);
        when(mockParams.getCurve()).thenReturn(mockCurve);
        when(mockCurve.getField()).thenReturn(mockField);
        when(mockField.getFieldSize()).thenReturn(256); // P-256 curve
        when(mockPublicKey.getW()).thenReturn(mockPoint);
        when(mockPoint.getAffineX()).thenReturn(x);
        when(mockPoint.getAffineY()).thenReturn(y);

        COSEKey result = COSEKeyConverter.fromECPublicKey(mockPublicKey);

        assertNotNull(result, "COSEKey should not be null");
    }

    @Test
    void Should_ThrowIllegalArgumentException_When_NonP256Curve() {
        when(mockPublicKey.getParams()).thenReturn(mockParams);
        when(mockParams.getCurve()).thenReturn(mockCurve);
        when(mockCurve.getField()).thenReturn(mockField);
        when(mockField.getFieldSize()).thenReturn(384); // P-384 curve

        IllegalArgumentException exception =
                assertThrows(
                        IllegalArgumentException.class,
                        () -> COSEKeyConverter.fromECPublicKey(mockPublicKey));
        assertEquals("Invalid key curve - expected P-256", exception.getMessage());
    }
}
