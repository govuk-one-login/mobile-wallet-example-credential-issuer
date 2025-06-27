package uk.gov.di.mobile.wallet.cri.credential.mobile_driving_licence.cbor;

import com.fasterxml.jackson.core.JsonGenerator;
import com.fasterxml.jackson.databind.SerializerProvider;
import com.fasterxml.jackson.dataformat.cbor.CBORGenerator;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InOrder;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import uk.gov.di.mobile.wallet.cri.credential.mobile_driving_licence.cose.COSESign1;
import uk.gov.di.mobile.wallet.cri.credential.mobile_driving_licence.mdoc.IssuerSigned;

import java.io.IOException;
import java.util.*;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.Mockito.inOrder;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class IssuerSignedCBORSerializerTest {

    // Mocked dependencies for IssuerSignedCBORSerializer
    @Mock private CBORGenerator cborGenerator;
    @Mock private JsonGenerator regularGenerator;
    @Mock private SerializerProvider serializerProvider;
    @Mock private IssuerSigned issuerSigned;

    /** Serializes an IssuerSigned object with a single namespace containing multiple items. */
    @Test
    void Should_SerializeIssuerSigned_SingleNameSpaceWithMultipleItems() throws IOException {
        // Arrange: Prepare a map with one namespace containing two items
        Map<String, List<byte[]>> nameSpaces = new LinkedHashMap<>();
        byte[] issuerSignedItemBytes1 = new byte[] {1, 2, 3};
        byte[] issuerSignedItemBytes2 = new byte[] {4, 5, 6};
        nameSpaces.put("namespace1", Arrays.asList(issuerSignedItemBytes1, issuerSignedItemBytes2));
        when(issuerSigned.nameSpaces()).thenReturn(nameSpaces);

        // Arrange: Prepare an CoseSign1 object
        byte[] protectedHeaderBytes = {1, 2, 3, 4};
        Map<Integer, Object> unprotectedHeader = new HashMap<>();
        byte[] payloadBytes = {5, 6, 7, 8};
        byte[] signatureBytes = {9, 10, 11, 12};
        COSESign1 issuerAuth =
                new COSESign1(
                        protectedHeaderBytes, unprotectedHeader, payloadBytes, signatureBytes);
        when(issuerSigned.issuerAuth()).thenReturn(issuerAuth);

        // Act: Serialize the IssuerSigned object
        new IssuerSignedCBORSerializer().serialize(issuerSigned, cborGenerator, serializerProvider);

        // Assert: Verify the correct sequence of CBOR generator calls
        InOrder inOrder = inOrder(cborGenerator);

        // Start the outer object and nameSpaces object
        inOrder.verify(cborGenerator).writeStartObject();
        inOrder.verify(cborGenerator).writeFieldName("nameSpaces");
        inOrder.verify(cborGenerator).writeStartObject();

        // Write the namespace field and start the array for its items
        inOrder.verify(cborGenerator).writeFieldName("namespace1");
        inOrder.verify(cborGenerator).writeStartArray();

        // For each item: write tag 24 then the item value
        inOrder.verify(cborGenerator).writeTag(24);
        inOrder.verify(cborGenerator).writeObject(issuerSignedItemBytes1);
        inOrder.verify(cborGenerator).writeTag(24);
        inOrder.verify(cborGenerator).writeObject(issuerSignedItemBytes2);

        // Close the array and objects
        inOrder.verify(cborGenerator).writeEndArray();
        inOrder.verify(cborGenerator).writeEndObject();

        // Write the issuerAuth field and start the array for its items
        inOrder.verify(cborGenerator).writeFieldName("issuerAuth");
        inOrder.verify(cborGenerator).writeStartArray();
        // Write issuerAuth (COSE_Sign1) items
        inOrder.verify(cborGenerator).writeBinary(protectedHeaderBytes);
        inOrder.verify(cborGenerator).writeObject(unprotectedHeader);
        inOrder.verify(cborGenerator).writeBinary(payloadBytes);
        inOrder.verify(cborGenerator).writeBinary(signatureBytes);
        inOrder.verify(cborGenerator).writeEndArray();

        // End the outer object
        inOrder.verify(cborGenerator).writeEndObject();
    }

    /** Serializes an IssuerSigned object with multiple namespaces. */
    @Test
    void Should_SerializeIssuerSignedWithCBORGenerator_MultipleNameSpaces() throws IOException {
        // Arrange: Prepare a map with two namespaces, each containing one item
        Map<String, List<byte[]>> nameSpaces = new LinkedHashMap<>();
        byte[] issuerSignedItemBytes1 = new byte[] {1, 2, 3};
        byte[] issuerSignedItemBytes2 = new byte[] {4, 5, 6};
        nameSpaces.put("namespace1", List.of(issuerSignedItemBytes1));
        nameSpaces.put("namespace2", List.of(issuerSignedItemBytes2));
        when(issuerSigned.nameSpaces()).thenReturn(nameSpaces);

        // Arrange: Prepare an CoseSign1 object
        byte[] protectedHeaderBytes = {1, 2, 3, 4};
        Map<Integer, Object> unprotectedHeader = new HashMap<>();
        byte[] payloadBytes = {5, 6, 7, 8};
        byte[] signatureBytes = {9, 10, 11, 12};
        COSESign1 issuerAuth =
                new COSESign1(
                        protectedHeaderBytes, unprotectedHeader, payloadBytes, signatureBytes);
        when(issuerSigned.issuerAuth()).thenReturn(issuerAuth);

        // Act: Serialize the IssuerSigned object
        new IssuerSignedCBORSerializer().serialize(issuerSigned, cborGenerator, serializerProvider);

        // Assert: Verify the correct sequence of CBOR generator calls
        InOrder inOrder = inOrder(cborGenerator);

        // Start the outer object and nameSpaces object
        inOrder.verify(cborGenerator).writeStartObject();
        inOrder.verify(cborGenerator).writeFieldName("nameSpaces");
        inOrder.verify(cborGenerator).writeStartObject();

        // Write the first namespace and its item
        inOrder.verify(cborGenerator).writeFieldName("namespace1");
        inOrder.verify(cborGenerator).writeStartArray();
        inOrder.verify(cborGenerator).writeTag(24);
        inOrder.verify(cborGenerator).writeObject(issuerSignedItemBytes1);
        inOrder.verify(cborGenerator).writeEndArray();

        // Write the second namespace and its item
        inOrder.verify(cborGenerator).writeFieldName("namespace2");
        inOrder.verify(cborGenerator).writeStartArray();
        inOrder.verify(cborGenerator).writeTag(24);
        inOrder.verify(cborGenerator).writeObject(issuerSignedItemBytes2);
        inOrder.verify(cborGenerator).writeEndArray();

        // Close nameSpaces object
        inOrder.verify(cborGenerator).writeEndObject();

        // Write the issuerAuth field and start the array for its items
        inOrder.verify(cborGenerator).writeFieldName("issuerAuth");
        inOrder.verify(cborGenerator).writeStartArray();
        // Write issuerAuth (COSE_Sign1) items
        inOrder.verify(cborGenerator).writeBinary(protectedHeaderBytes);
        inOrder.verify(cborGenerator).writeObject(unprotectedHeader);
        inOrder.verify(cborGenerator).writeBinary(payloadBytes);
        inOrder.verify(cborGenerator).writeBinary(signatureBytes);
        inOrder.verify(cborGenerator).writeEndArray();

        // End the outer object
        inOrder.verify(cborGenerator).writeEndObject();
    }

    /** Throws an exception if a non-CBOR generator is used. */
    @Test
    void Should_ThrowIllegalArgumentException_When_SerializerIsNonCBORGenerator() {
        // Act & Assert: Attempting to serialize with a regular (non-CBOR) generator should fail
        IssuerSignedCBORSerializer issuerSignedCBORSerializer = new IssuerSignedCBORSerializer();
        IllegalArgumentException exception =
                assertThrows(
                        IllegalArgumentException.class,
                        () ->
                                issuerSignedCBORSerializer.serialize(
                                        issuerSigned, regularGenerator, serializerProvider));
        assertEquals("This serializer only supports CBORGenerator", exception.getMessage());
    }
}
