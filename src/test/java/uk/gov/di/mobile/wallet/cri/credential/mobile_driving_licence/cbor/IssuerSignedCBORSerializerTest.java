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
import uk.gov.di.mobile.wallet.cri.credential.mobile_driving_licence.mdoc.IssuerAuth;
import uk.gov.di.mobile.wallet.cri.credential.mobile_driving_licence.mdoc.IssuerSigned;

import java.io.IOException;
import java.util.Arrays;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.inOrder;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class IssuerSignedCBORSerializerTest {

    private IssuerSignedCBORSerializer serializer;

    @Mock private CBORGenerator cborGenerator;
    @Mock private JsonGenerator regularGenerator;
    @Mock private SerializerProvider serializerProvider;
    @Mock private IssuerSigned issuerSigned;

    private final byte[] issuerSignedItemBytes1 = new byte[] {1, 2, 3};
    private final byte[] issuerSignedItemBytes2 = new byte[] {4, 5, 6};

    @BeforeEach
    void setUp() {
        serializer = new IssuerSignedCBORSerializer();
    }

    @Test
    void Should_SerializeIssuerSignedWithCBORGenerator_SingleNameSpaceWithMultipleItems()
            throws IOException {
        // Create a map with one namespace containing two items
        Map<String, List<byte[]>> nameSpaces = new LinkedHashMap<>();
        nameSpaces.put("namespace1", Arrays.asList(issuerSignedItemBytes1, issuerSignedItemBytes2));
        when(issuerSigned.nameSpaces()).thenReturn(nameSpaces);

        byte[] mobileSecurityObjectBytes = new byte[] {9, 9, 9};
        IssuerAuth issuerAuth = new IssuerAuth(mobileSecurityObjectBytes);
        when(issuerSigned.issuerAuth()).thenReturn(issuerAuth);

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
        inOrder.verify(cborGenerator).writeObject(issuerSignedItemBytes1);
        inOrder.verify(cborGenerator).writeTag(24);
        inOrder.verify(cborGenerator).writeObject(issuerSignedItemBytes2);

        // Close the array and objects
        inOrder.verify(cborGenerator).writeEndArray();
        inOrder.verify(cborGenerator).writeEndObject();

        // IssuerAuth serialization
        inOrder.verify(cborGenerator).writeFieldName("issuerAuth");
        inOrder.verify(cborGenerator).writeStartArray();
        inOrder.verify(cborGenerator).writeTag(24);
        inOrder.verify(cborGenerator).writeObject(mobileSecurityObjectBytes);
        inOrder.verify(cborGenerator).writeEndArray();

        // End the outer object
        inOrder.verify(cborGenerator).writeEndObject();
    }

    @Test
    void Should_SerializeIssuerSignedWithCBORGenerator_MultipleNameSpaces() throws IOException {
        // Create a map with two namespaces, each with one item
        Map<String, List<byte[]>> nameSpaces = new LinkedHashMap<>();
        nameSpaces.put("namespace1", List.of(issuerSignedItemBytes1));
        nameSpaces.put("namespace2", List.of(issuerSignedItemBytes2));
        when(issuerSigned.nameSpaces()).thenReturn(nameSpaces);

        byte[] mobileSecurityObjectBytes = new byte[] {9, 9, 9};
        IssuerAuth issuerAuth = new IssuerAuth(mobileSecurityObjectBytes);
        when(issuerSigned.issuerAuth()).thenReturn(issuerAuth);

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
        inOrder.verify(cborGenerator).writeObject(issuerSignedItemBytes1);
        inOrder.verify(cborGenerator).writeEndArray();

        // Write the second namespace
        inOrder.verify(cborGenerator).writeFieldName("namespace2");
        inOrder.verify(cborGenerator).writeStartArray();
        inOrder.verify(cborGenerator).writeTag(24);
        inOrder.verify(cborGenerator).writeObject(issuerSignedItemBytes2);
        inOrder.verify(cborGenerator).writeEndArray();

        // Close nameSpaces object
        inOrder.verify(cborGenerator).writeEndObject();

        // IssuerAuth serialization
        inOrder.verify(cborGenerator).writeFieldName("issuerAuth");
        inOrder.verify(cborGenerator).writeStartArray();
        inOrder.verify(cborGenerator).writeTag(24);
        inOrder.verify(cborGenerator).writeObject(mobileSecurityObjectBytes);
        inOrder.verify(cborGenerator).writeEndArray();

        // End the outer object
        inOrder.verify(cborGenerator).writeEndObject();
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
