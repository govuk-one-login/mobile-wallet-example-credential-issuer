package uk.gov.di.mobile.wallet.cri.credential.mobile_driving_licence.cose;

import org.junit.jupiter.api.Test;

import java.util.Map;

import static org.junit.jupiter.api.Assertions.assertArrayEquals;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

class COSESign1Test {

    @Test
    void Should_CreateRecordWithAllFields() {
        byte[] protectedHeader = {1, 2, 3};
        Map<Integer, Object> unprotectedHeader = Map.of(1, "test");
        byte[] payload = {4, 5, 6};
        byte[] signature = {7, 8, 9};

        COSESign1 record = new COSESign1(protectedHeader, unprotectedHeader, payload, signature);

        assertArrayEquals(protectedHeader, record.protectedHeader());
        assertEquals(unprotectedHeader, record.unprotectedHeader());
        assertArrayEquals(payload, record.payload());
        assertArrayEquals(signature, record.signature());
    }

    @Test
    void Should_BeEqual_When_ArrayContentIsSame() {
        byte[] protectedHeader1 = {1, 2, 3};
        byte[] protectedHeader2 = {1, 2, 3};
        Map<Integer, Object> unprotectedHeader = Map.of(1, "test");
        byte[] payload1 = {4, 5, 6};
        byte[] payload2 = {4, 5, 6};
        byte[] signature1 = {7, 8, 9};
        byte[] signature2 = {7, 8, 9};

        COSESign1 record1 =
                new COSESign1(protectedHeader1, unprotectedHeader, payload1, signature1);
        COSESign1 record2 =
                new COSESign1(protectedHeader2, unprotectedHeader, payload2, signature2);

        assertEquals(record1, record2);
    }

    @Test
    void Should_NotBeEqual_When_ProtectedHeaderDiffers() {
        byte[] protectedHeader1 = {1, 2, 3};
        byte[] protectedHeader2 = {1, 2, 4};
        Map<Integer, Object> unprotectedHeader = Map.of(1, "test");
        byte[] payload = {4, 5, 6};
        byte[] signature = {7, 8, 9};

        COSESign1 record1 = new COSESign1(protectedHeader1, unprotectedHeader, payload, signature);
        COSESign1 record2 = new COSESign1(protectedHeader2, unprotectedHeader, payload, signature);

        assertNotEquals(record1, record2);
    }

    @Test
    void Should_NotBeEqual_When_PayloadDiffers() {
        byte[] protectedHeader = {1, 2, 3};
        Map<Integer, Object> unprotectedHeader = Map.of(1, "test");
        byte[] payload1 = {4, 5, 6};
        byte[] payload2 = {4, 5, 7};
        byte[] signature = {7, 8, 9};

        COSESign1 record1 = new COSESign1(protectedHeader, unprotectedHeader, payload1, signature);
        COSESign1 record2 = new COSESign1(protectedHeader, unprotectedHeader, payload2, signature);

        assertNotEquals(record1, record2);
    }

    @Test
    void Should_NotBeEqual_When_SignatureDiffers() {
        byte[] protectedHeader = {1, 2, 3};
        Map<Integer, Object> unprotectedHeader = Map.of(1, "test");
        byte[] payload = {4, 5, 6};
        byte[] signature1 = {7, 8, 9};
        byte[] signature2 = {7, 8, 10};

        COSESign1 record1 = new COSESign1(protectedHeader, unprotectedHeader, payload, signature1);
        COSESign1 record2 = new COSESign1(protectedHeader, unprotectedHeader, payload, signature2);

        assertNotEquals(record1, record2);
    }

    @Test
    void Should_NotBeEqual_When_UnprotectedHeaderDiffers() {
        byte[] protectedHeader = {1, 2, 3};
        Map<Integer, Object> unprotectedHeader1 = Map.of(1, "test");
        Map<Integer, Object> unprotectedHeader2 = Map.of(1, "different");
        byte[] payload = {4, 5, 6};
        byte[] signature = {7, 8, 9};

        COSESign1 record1 = new COSESign1(protectedHeader, unprotectedHeader1, payload, signature);
        COSESign1 record2 = new COSESign1(protectedHeader, unprotectedHeader2, payload, signature);

        assertNotEquals(record1, record2);
    }

    @Test
    void Should_HaveSameHashCodeForEqualObjects() {
        byte[] protectedHeader1 = {1, 2, 3};
        byte[] protectedHeader2 = {1, 2, 3};
        Map<Integer, Object> unprotectedHeader = Map.of(1, "test");
        byte[] payload1 = {4, 5, 6};
        byte[] payload2 = {4, 5, 6};
        byte[] signature1 = {7, 8, 9};
        byte[] signature2 = {7, 8, 9};

        COSESign1 record1 =
                new COSESign1(protectedHeader1, unprotectedHeader, payload1, signature1);
        COSESign1 record2 =
                new COSESign1(protectedHeader2, unprotectedHeader, payload2, signature2);

        assertEquals(record1.hashCode(), record2.hashCode());
    }

    @Test
    void Should_IncludeArrayContentInToString() {
        byte[] protectedHeader = {1, 2, 3};
        Map<Integer, Object> unprotectedHeader = Map.of(1, "test");
        byte[] payload = {4, 5, 6};
        byte[] signature = {7, 8, 9};

        COSESign1 record = new COSESign1(protectedHeader, unprotectedHeader, payload, signature);
        String toString = record.toString();

        assertTrue(toString.contains("[1, 2, 3]"));
        assertTrue(toString.contains("[4, 5, 6]"));
        assertTrue(toString.contains("[7, 8, 9]"));
        assertTrue(toString.contains("test"));
    }
}
