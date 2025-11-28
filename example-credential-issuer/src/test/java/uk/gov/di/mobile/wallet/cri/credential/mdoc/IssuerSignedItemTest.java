package uk.gov.di.mobile.wallet.cri.credential.mdoc;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertArrayEquals;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

class IssuerSignedItemTest {

    @Test
    void Should_CreateRecordWithAllFields() {
        int digestId = 1;
        byte[] random = new byte[] {1, 2};
        String elementIdentifier = "test_element_identifier";
        Object elementValue = "Test Element Value";

        IssuerSignedItem result =
                new IssuerSignedItem(digestId, random, elementIdentifier, elementValue);

        assertEquals(digestId, result.digestId());
        assertArrayEquals(random, result.random());
        assertEquals(elementIdentifier, result.elementIdentifier());
        assertEquals(elementValue, result.elementValue());
    }

    @Test
    void Should_BeEqual_When_ArrayContentIsSame() {
        IssuerSignedItem result1 =
                new IssuerSignedItem(
                        1, new byte[] {1, 2}, "test_element_identifier", "Test Element Value");
        IssuerSignedItem result2 =
                new IssuerSignedItem(
                        1, new byte[] {1, 2}, "test_element_identifier", "Test Element Value");
        assertEquals(result1, result2);
    }

    @Test
    void Should_NotBeEqual_When_DigestIdDiffers() {
        IssuerSignedItem result1 =
                new IssuerSignedItem(
                        1, new byte[] {1, 2}, "test_element_identifier", "Test Element Value");
        IssuerSignedItem result2 =
                new IssuerSignedItem(
                        2, new byte[] {1, 2}, "test_element_identifier", "Test Element Value");
        assertNotEquals(result1, result2);
    }

    @Test
    void Should_NotBeEqual_When_RandomDiffers() {
        IssuerSignedItem result1 =
                new IssuerSignedItem(
                        1, new byte[] {1, 2}, "test_element_identifier", "Test Element Value");
        IssuerSignedItem result2 =
                new IssuerSignedItem(
                        1, new byte[] {3, 4}, "test_element_identifier", "Test Element Value");
        assertNotEquals(result1, result2);
    }

    @Test
    void Should_NotBeEqual_When_ElementIdentifierDiffers() {
        IssuerSignedItem result1 =
                new IssuerSignedItem(
                        1, new byte[] {1, 2}, "test_element_identifier", "Test Element Value");
        IssuerSignedItem result2 =
                new IssuerSignedItem(1, new byte[] {1, 2}, "something_else", "Test Element Value");
        assertNotEquals(result1, result2);
    }

    @Test
    void Should_NotBeEqual_When_ElementValueDiffers() {
        IssuerSignedItem result1 =
                new IssuerSignedItem(
                        1, new byte[] {1, 2}, "test_element_identifier", "Test Element Value");
        IssuerSignedItem result2 =
                new IssuerSignedItem(
                        1, new byte[] {1, 2}, "test_element_identifier", "Something Else");
        assertNotEquals(result1, result2);
    }

    @Test
    void Should_HaveSameHashCodeForEqualObjects() {
        IssuerSignedItem result1 =
                new IssuerSignedItem(
                        1, new byte[] {1, 2}, "test_element_identifier", "Test Element Value");
        IssuerSignedItem result2 =
                new IssuerSignedItem(
                        1, new byte[] {1, 2}, "test_element_identifier", "Test Element Value");
        assertEquals(result1.hashCode(), result2.hashCode());
    }

    @Test
    void Should_IncludeArrayContentInToString() {
        IssuerSignedItem result =
                new IssuerSignedItem(
                        1, new byte[] {1, 2}, "test_element_identifier", "Test Element Value");
        String toString = result.toString();

        assertTrue(toString.contains("[1, 2]"));
    }
}
