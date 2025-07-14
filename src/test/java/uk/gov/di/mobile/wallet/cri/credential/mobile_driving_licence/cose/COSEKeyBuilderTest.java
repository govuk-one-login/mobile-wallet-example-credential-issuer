package uk.gov.di.mobile.wallet.cri.credential.mobile_driving_licence.cose;

import org.junit.jupiter.api.Test;

import java.util.Map;

import static org.junit.jupiter.api.Assertions.assertArrayEquals;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertThrows;

class COSEKeyBuilderTest {

    @Test
    void Should_BuildCOSEKeyWithAllRequiredParameters() {
        int kty = 2; // Example key type
        int crv = 1; // Example curve
        byte[] x = new byte[32]; // Example x coordinate
        byte[] y = new byte[32]; // Example y coordinate

        COSEKey result =
                new COSEKeyBuilder().keyType(kty).curve(crv).xCoordinate(x).yCoordinate(y).build();

        assertNotNull(result, "COSEKey should not be null");

        Map<Integer, Object> map = result.parameters();
        assertEquals(4, map.size(), "COSEKey map should have 4 parameters");
        assertEquals(kty, map.get(1), "Key type (kty) should match input");
        assertEquals(crv, map.get(-1), "Curve (crv) should match input");
        assertArrayEquals(x, (byte[]) map.get(-2), "x-coordinate should match input");
        assertArrayEquals(y, (byte[]) map.get(-3), "y-coordinate should match input");
    }

    @Test
    void Should_ThrowException_When_KeyTypeIsMissing() {
        COSEKeyBuilder builder =
                new COSEKeyBuilder().curve(1).xCoordinate(new byte[32]).yCoordinate(new byte[32]);

        IllegalStateException exception = assertThrows(IllegalStateException.class, builder::build);
        assertEquals("Key type must be set", exception.getMessage());
    }

    @Test
    void Should_ThrowException_When_CurveIsMissing() {
        COSEKeyBuilder builder =
                new COSEKeyBuilder().keyType(2).xCoordinate(new byte[32]).yCoordinate(new byte[32]);

        IllegalStateException exception = assertThrows(IllegalStateException.class, builder::build);
        assertEquals("EC curve must be set", exception.getMessage());
    }

    @Test
    void Should_ThrowException_When_XCoordinateIsMissing() {
        COSEKeyBuilder builder = new COSEKeyBuilder().keyType(2).curve(1).yCoordinate(new byte[32]);

        IllegalStateException exception = assertThrows(IllegalStateException.class, builder::build);
        assertEquals("y-coordinate must be set", exception.getMessage());
    }

    @Test
    void Should_ThrowException_When_YCoordinateIsMissing() {
        COSEKeyBuilder builder = new COSEKeyBuilder().keyType(2).curve(1).xCoordinate(new byte[32]);

        IllegalStateException exception = assertThrows(IllegalStateException.class, builder::build);
        assertEquals("y-coordinate must be set", exception.getMessage());
    }
}
