package uk.gov.di.mobile.wallet.cri.credential.mdoc;

import org.apache.hc.client5.http.utils.Hex;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class IssuerSignedItemFactoryTest {

    @Mock private DigestIDGenerator mockDigestIDGenerator;
    private IssuerSignedItemFactory issuerSignedItemFactory;

    @BeforeEach
    void setUp() {
        issuerSignedItemFactory = new IssuerSignedItemFactory(mockDigestIDGenerator);
    }

    @Test
    void Should_CreateItemWithProvidedElementIdentifierAndValue() {
        String identifier = "identifier";
        String value = "value";
        when(mockDigestIDGenerator.next()).thenReturn(12345);

        IssuerSignedItem issuerSignedItem = issuerSignedItemFactory.build(identifier, value);

        assertEquals(
                identifier,
                issuerSignedItem.elementIdentifier(),
                "Item should have the provided element identifier");
        assertEquals(
                value,
                issuerSignedItem.elementValue(),
                "Item should have the provided element value");
    }

    @Test
    void Should_CreateItemWithNextDigestID() {
        int expectedDigestID = 54321;
        when(mockDigestIDGenerator.next()).thenReturn(expectedDigestID);

        IssuerSignedItem issuerSignedItem = issuerSignedItemFactory.build("identifier", "value");

        assertEquals(
                expectedDigestID,
                issuerSignedItem.digestId(),
                "Item should have the digest ID from the generator");
        verify(mockDigestIDGenerator, times(1)).next();
    }

    @Test
    void Should_CreateItemWithRandomBytes() {
        IssuerSignedItem issuerSignedItem1 = issuerSignedItemFactory.build("identifier1", "value1");
        IssuerSignedItem issuerSignedItem2 = issuerSignedItemFactory.build("identifier2", "value2");

        assertNotNull(issuerSignedItem1.random(), "Random bytes should not be null");
        assertNotNull(issuerSignedItem2.random(), "Random bytes should not be null");
        assertEquals(
                16,
                issuerSignedItem1.random().length,
                "Random bytes array should have length of 16");
        assertEquals(
                16,
                issuerSignedItem2.random().length,
                "Random bytes array should have length of 16");
        assertNotEquals(
                Hex.encodeHexString(issuerSignedItem1.random()),
                Hex.encodeHexString(issuerSignedItem2.random()),
                "Random bytes should be different for different items");
    }

    @Test
    void Should_BuildMultipleItemsWithConsecutiveDigestIDs() {
        when(mockDigestIDGenerator.next()).thenReturn(100).thenReturn(101).thenReturn(102);

        IssuerSignedItem issuerSignedItem1 = issuerSignedItemFactory.build("identifier1", "value1");
        IssuerSignedItem issuerSignedItem2 = issuerSignedItemFactory.build("identifier2", "value2");
        IssuerSignedItem issuerSignedItem3 = issuerSignedItemFactory.build("identifier3", "value3");

        assertEquals(100, issuerSignedItem1.digestId(), "First item should have digestID 100");
        assertEquals(101, issuerSignedItem2.digestId(), "Second item should have digestID 101");
        assertEquals(102, issuerSignedItem3.digestId(), "Third item should have digestID 102");
        verify(mockDigestIDGenerator, times(3)).next();
    }

    @Test
    void Should_BuildItemsWithElementValueOfDifferentTypes() {
        String stringValue = "string value";
        Integer integerValue = 42;
        Boolean booleanValue = true;

        IssuerSignedItem stringItem =
                issuerSignedItemFactory.build("stringIdentifier", stringValue);
        IssuerSignedItem intItem = issuerSignedItemFactory.build("integerIdentifier", integerValue);
        IssuerSignedItem boolItem =
                issuerSignedItemFactory.build("booleanIdentifier", booleanValue);

        assertEquals(stringValue, stringItem.elementValue(), "String value should be preserved");
        assertEquals(integerValue, intItem.elementValue(), "Integer value should be preserved");
        assertEquals(booleanValue, boolItem.elementValue(), "Boolean value should be preserved");
    }
}
