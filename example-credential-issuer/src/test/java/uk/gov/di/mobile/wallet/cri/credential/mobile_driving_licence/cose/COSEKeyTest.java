package uk.gov.di.mobile.wallet.cri.credential.mobile_driving_licence.cose;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertArrayEquals;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

class COSEKeyTest {

    @Test
    void Should_CreateRecordWithAllFields() {
        int keyType = 1;
        int curve = 2;
        byte[] x = {1, 2, 3};
        byte[] y = {7, 8, 9};

        COSEKey result = new COSEKey(keyType, curve, x, y);

        assertEquals(keyType, result.keyType());
        assertEquals(curve, result.curve());
        assertArrayEquals(x, result.x());
        assertArrayEquals(y, result.y());
    }

    @Test
    void Should_BeEqual_When_ArrayContentIsSame() {
        COSEKey result1 = new COSEKey(1, 2, new byte[] {1, 2}, new byte[] {3, 4});
        COSEKey result2 = new COSEKey(1, 2, new byte[] {1, 2}, new byte[] {3, 4});
        assertEquals(result1, result2);
    }

    @Test
    void Should_NotBeEqual_When_KeyTypeDiffers() {
        COSEKey result1 = new COSEKey(1, 2, new byte[] {1, 2}, new byte[] {3, 4});
        COSEKey result2 = new COSEKey(9, 2, new byte[] {1, 2}, new byte[] {3, 4});
        assertNotEquals(result1, result2);
    }

    @Test
    void Should_NotBeEqual_When_CurveDiffers() {
        COSEKey result1 = new COSEKey(1, 2, new byte[] {1, 2}, new byte[] {3, 4});
        COSEKey result2 = new COSEKey(1, 9, new byte[] {1, 2}, new byte[] {3, 4});
        assertNotEquals(result1, result2);
    }

    @Test
    void Should_NotBeEqual_When_XDiffers() {
        COSEKey result1 = new COSEKey(1, 2, new byte[] {1, 2}, new byte[] {3, 4});
        COSEKey result2 = new COSEKey(1, 2, new byte[] {9, 9}, new byte[] {3, 4});
        assertNotEquals(result1, result2);
    }

    @Test
    void Should_NotBeEqual_When_YDiffers() {
        COSEKey result1 = new COSEKey(1, 2, new byte[] {1, 2}, new byte[] {3, 4});
        COSEKey result2 = new COSEKey(1, 2, new byte[] {1, 2}, new byte[] {9, 9});
        assertNotEquals(result1, result2);
    }

    @Test
    void Should_HaveSameHashCodeForEqualObjects() {
        COSEKey result1 = new COSEKey(1, 2, new byte[] {1, 2}, new byte[] {3, 4});
        COSEKey result2 = new COSEKey(1, 2, new byte[] {1, 2}, new byte[] {3, 4});
        assertEquals(result1.hashCode(), result2.hashCode());
    }

    @Test
    void Should_IncludeArrayContentInToString() {
        COSEKey result = new COSEKey(1, 2, new byte[] {1, 2}, new byte[] {3, 4});
        String toString = result.toString();

        assertTrue(toString.contains("[1, 2]"));
        assertTrue(toString.contains("[3, 4]"));
    }
}
