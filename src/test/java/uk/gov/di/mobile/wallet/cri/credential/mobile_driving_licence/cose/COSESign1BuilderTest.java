package uk.gov.di.mobile.wallet.cri.credential.mobile_driving_licence.cose;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.util.HashMap;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.*;

class COSESign1BuilderTest {

    private COSESign1Builder builder;
    private byte[] testProtectedHeader;
    private Map<Integer, Object> testUnprotectedHeader;
    private byte[] testPayload;
    private byte[] testSignature;

    @BeforeEach
    void setUp() {
        builder = new COSESign1Builder();
        testProtectedHeader = new byte[] {1, 2, 3};
        testUnprotectedHeader = new HashMap<>();
        testUnprotectedHeader.put(4, "test");
        testPayload = new byte[] {4, 5, 6};
        testSignature = new byte[] {7, 8, 9};
    }

    @Test
    void Should_BuildCOSESign1() {
        COSESign1 sign1 =
                builder.protectedHeader(testProtectedHeader)
                        .unprotectedHeader(testUnprotectedHeader)
                        .payload(testPayload)
                        .signature(testSignature)
                        .build();

        assertNotNull(sign1);
        assertEquals(testProtectedHeader, sign1.protectedHeader());
        assertEquals(testUnprotectedHeader, sign1.unprotectedHeader());
        assertEquals(testPayload, sign1.payload());
        assertEquals(testSignature, sign1.signature());
    }

    @Test
    void Should_ThrowIllegalArgumentException_When_ProtectedHeaderIsNull() {
        assertThrows(IllegalArgumentException.class, () -> builder.protectedHeader(null));
    }

    @Test
    void Should_ThrowIllegalArgumentException_When_UnprotectedHeaderIsNull() {
        assertThrows(IllegalArgumentException.class, () -> builder.unprotectedHeader(null));
    }

    @Test
    void Should_ThrowIllegalArgumentException_When_PayloadIsNull() {
        assertThrows(IllegalArgumentException.class, () -> builder.payload(null));
    }

    @Test
    void Should_ThrowIllegalArgumentException_When_SignatureIsNull() {
        assertThrows(IllegalArgumentException.class, () -> builder.signature(null));
    }

    @Test
    void Should_ThrowIllegalArgumentException_When_ProtectedHeaderIsNotSet() {
        builder.unprotectedHeader(testUnprotectedHeader)
                .payload(testPayload)
                .signature(testSignature);

        IllegalStateException exception =
                assertThrows(IllegalStateException.class, () -> builder.build());

        assertEquals("All fields must be set and non-null", exception.getMessage());
    }

    @Test
    void Should_ThrowIllegalArgumentException_When_UnprotectedHeaderIsNotSet() {
        builder.protectedHeader(testProtectedHeader).payload(testPayload).signature(testSignature);

        IllegalStateException exception =
                assertThrows(IllegalStateException.class, () -> builder.build());

        assertEquals("All fields must be set and non-null", exception.getMessage());
    }

    @Test
    void Should_ThrowIllegalArgumentException_When_PayloadIsNotSet() {
        builder.protectedHeader(testProtectedHeader)
                .unprotectedHeader(testUnprotectedHeader)
                .signature(testSignature);

        IllegalStateException exception =
                assertThrows(IllegalStateException.class, () -> builder.build());

        assertEquals("All fields must be set and non-null", exception.getMessage());
    }

    @Test
    void Should_ThrowIllegalArgumentException_When_SignatureIsNotSet() {
        builder.protectedHeader(testProtectedHeader)
                .unprotectedHeader(testUnprotectedHeader)
                .payload(testPayload);

        IllegalStateException exception =
                assertThrows(IllegalStateException.class, () -> builder.build());

        assertEquals("All fields must be set and non-null", exception.getMessage());
    }

    @Test
    void Should_ThrowIllegalArgumentException_When_NoFieldsAreSet() {
        IllegalStateException exception =
                assertThrows(IllegalStateException.class, () -> builder.build());
        assertEquals("All fields must be set and non-null", exception.getMessage());
    }
}
