package uk.gov.di.mobile.wallet.cri.credential.mobile_driving_licence.cose;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertArrayEquals;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

class COSEUnprotectedHeaderTest {

    @Test
    void shouldCreateRecordWithX5Chain() {
        byte[] x5chain = {1, 2, 3};
        COSEUnprotectedHeader result = new COSEUnprotectedHeader(x5chain);

        assertArrayEquals(x5chain, result.x5chain());
    }

    @Test
    void shouldBeEqualWhenArraysAreEqual() {
        byte[] x5chain1 = {1, 2, 3};
        byte[] x5chain2 = {1, 2, 3};

        COSEUnprotectedHeader result1 = new COSEUnprotectedHeader(x5chain1);
        COSEUnprotectedHeader result2 = new COSEUnprotectedHeader(x5chain2);

        assertEquals(result1, result2);
    }

    @Test
    void shouldNotBeEqualWhenArraysAreDifferent() {
        byte[] x5chain1 = {1, 2, 3};
        byte[] x5chain2 = {1, 2, 4};

        COSEUnprotectedHeader result1 = new COSEUnprotectedHeader(x5chain1);
        COSEUnprotectedHeader result2 = new COSEUnprotectedHeader(x5chain2);

        assertNotEquals(result1, result2);
    }

    @Test
    void shouldHaveSameHashCodeWhenArraysAreEqual() {
        byte[] x5chain1 = {1, 2, 3};
        byte[] x5chain2 = {1, 2, 3};

        COSEUnprotectedHeader result1 = new COSEUnprotectedHeader(x5chain1);
        COSEUnprotectedHeader result2 = new COSEUnprotectedHeader(x5chain2);

        assertEquals(result1.hashCode(), result2.hashCode());
    }

    @Test
    void shouldIncludeX5ChainInToString() {
        byte[] x5chain = {1, 2, 3};

        COSEUnprotectedHeader result = new COSEUnprotectedHeader(x5chain);
        String toString = result.toString();

        assertTrue(toString.contains("x5chain="));
        assertTrue(toString.contains("[1, 2, 3]"));
    }
}
