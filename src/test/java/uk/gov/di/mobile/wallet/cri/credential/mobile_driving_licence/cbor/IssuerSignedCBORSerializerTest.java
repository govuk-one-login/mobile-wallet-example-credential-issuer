package uk.gov.di.mobile.wallet.cri.credential.mobile_driving_licence.cbor;

import com.fasterxml.jackson.core.JsonGenerator;
import com.fasterxml.jackson.databind.SerializerProvider;
import com.fasterxml.jackson.dataformat.cbor.CBORGenerator;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InOrder;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import uk.gov.di.mobile.wallet.cri.credential.mobile_driving_licence.mdoc.IssuerSigned;
import uk.gov.di.mobile.wallet.cri.credential.mobile_driving_licence.mdoc.IssuerSignedItem;

import java.io.IOException;
import java.util.*;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class IssuerSignedCBORSerializerTest {

    private IssuerSignedCBORSerializer serializer;

    @Mock private CBORGenerator cborGenerator;
    @Mock private JsonGenerator regularGenerator;
    @Mock private SerializerProvider serializerProvider;
    @Mock private IssuerSigned issuerSigned;
    @Mock private IssuerSignedItem issuerSignedItem1;
    @Mock private IssuerSignedItem issuerSignedItem2;

    @BeforeEach
    void setUp() {
        serializer = new IssuerSignedCBORSerializer();
    }

    @Test
    void Should_SerializeIssuerSignedWithCBORGenerator_SingleNameSpaceWithMultipleItems()
            throws IOException {
        // Create a map with one namespace containing two items
        Map<String, List<IssuerSignedItem>> nameSpaces = new HashMap<>();
        nameSpaces.put("namespace1", Arrays.asList(issuerSignedItem1, issuerSignedItem2));
        when(issuerSigned.nameSpaces()).thenReturn(nameSpaces);

        serializer.serialize(issuerSigned, cborGenerator, serializerProvider);

        // Verify correct sequence of method calls
        InOrder inOrder = inOrder(cborGenerator);

        // Start the outer object
        inOrder.verify(cborGenerator).writeStartObject();
        inOrder.verify(cborGenerator).writeFieldName("nameSpaces");
        inOrder.verify(cborGenerator).writeStartObject();

        // Write the namespace field and start the array for its items
        inOrder.verify(cborGenerator).writeFieldName("namespace1");
        inOrder.verify(cborGenerator).writeStartArray();

        // For each item: write tag 24 then write the object
        inOrder.verify(cborGenerator).writeTag(24);
        inOrder.verify(cborGenerator).writeObject(issuerSignedItem1);
        inOrder.verify(cborGenerator).writeTag(24);
        inOrder.verify(cborGenerator).writeObject(issuerSignedItem2);

        // Close the array and objects
        inOrder.verify(cborGenerator).writeEndArray();
        inOrder.verify(cborGenerator, times(2)).writeEndObject();
    }

    @Test
    void Should_SerializeIssuerSignedWithCBORGenerator_MultipleNameSpaces() throws IOException {
        // Create a map with two namespaces, each with one item
        Map<String, List<IssuerSignedItem>> nameSpaces = new LinkedHashMap<>();
        nameSpaces.put("namespace1", Arrays.asList(issuerSignedItem1));
        nameSpaces.put("namespace2", Arrays.asList(issuerSignedItem2));
        when(issuerSigned.nameSpaces()).thenReturn(nameSpaces);

        serializer.serialize(issuerSigned, cborGenerator, serializerProvider);

        // Verify correct sequence of method calls
        InOrder inOrder = inOrder(cborGenerator);

        // Start the outer object
        inOrder.verify(cborGenerator).writeStartObject();
        inOrder.verify(cborGenerator).writeFieldName("nameSpaces");
        inOrder.verify(cborGenerator).writeStartObject();

        // Write the first namespace
        inOrder.verify(cborGenerator).writeFieldName("namespace1");
        inOrder.verify(cborGenerator).writeStartArray();
        inOrder.verify(cborGenerator).writeTag(24);
        inOrder.verify(cborGenerator).writeObject(issuerSignedItem1);
        inOrder.verify(cborGenerator).writeEndArray();

        // Write the second namespace
        inOrder.verify(cborGenerator).writeFieldName("namespace2");
        inOrder.verify(cborGenerator).writeStartArray();
        inOrder.verify(cborGenerator).writeTag(24);
        inOrder.verify(cborGenerator).writeObject(issuerSignedItem2);
        inOrder.verify(cborGenerator).writeEndArray();

        // Close objects
        inOrder.verify(cborGenerator, times(2)).writeEndObject();
    }

    @Test
    void Should_ThrowIllegalArgumentException_When_SerializerIsNonCBORGenerator() {
        IllegalArgumentException exception =
                assertThrows(
                        IllegalArgumentException.class,
                        () ->
                                serializer.serialize(
                                        issuerSigned, regularGenerator, serializerProvider));

        assertEquals("This serializer only supports CBORGenerator", exception.getMessage());
    }
}
