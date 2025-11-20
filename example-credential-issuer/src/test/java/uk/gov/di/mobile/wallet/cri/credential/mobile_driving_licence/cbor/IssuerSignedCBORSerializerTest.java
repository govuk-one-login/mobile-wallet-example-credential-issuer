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
import uk.gov.di.mobile.wallet.cri.credential.mobile_driving_licence.cose.COSEUnprotectedHeader;
import uk.gov.di.mobile.wallet.cri.credential.mobile_driving_licence.cose.COSEUnprotectedHeaderBuilder;
import uk.gov.di.mobile.wallet.cri.credential.mobile_driving_licence.mdoc.IssuerSigned;
import uk.gov.di.mobile.wallet.cri.credential.mobile_driving_licence.mdoc.IssuerSignedItem;

import java.io.IOException;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.Mockito.inOrder;
import static org.mockito.Mockito.mock;

@ExtendWith(MockitoExtension.class)
class IssuerSignedCBORSerializerTest {

    @Mock private CBORGenerator cborGenerator;
    @Mock private SerializerProvider serializerProvider;

    @Test
    void Should_SerializeIssuerSigned_SingleNameSpaceWithMultipleItems() throws IOException {
        Map<String, List<IssuerSignedItem>> namespaces = new LinkedHashMap<>();
        namespaces.put("namespace1", List.of(buildTestItem(1), buildTestItem(2)));
        COSESign1 issuerAuth = buildTestIssuerAuth();
        IssuerSigned valueToSerialize = new IssuerSigned(namespaces, issuerAuth);

        new IssuerSignedCBORSerializer()
                .serialize(valueToSerialize, cborGenerator, serializerProvider);

        InOrder inOrder = inOrder(cborGenerator);
        inOrder.verify(cborGenerator).writeStartObject();
        inOrder.verify(cborGenerator).writeFieldName("nameSpaces");
        inOrder.verify(cborGenerator).writeStartObject();
        inOrder.verify(cborGenerator).writeFieldName("namespace1");
        inOrder.verify(cborGenerator).writeStartArray();
        inOrder.verify(cborGenerator).writeObject(namespaces.get("namespace1").get(0));
        inOrder.verify(cborGenerator).writeObject(namespaces.get("namespace1").get(1));
        inOrder.verify(cborGenerator).writeEndArray();
        inOrder.verify(cborGenerator).writeEndObject();
        inOrder.verify(cborGenerator).writeFieldName("issuerAuth");
        inOrder.verify(cborGenerator).writeStartArray();
        inOrder.verify(cborGenerator).writeBinary(issuerAuth.protectedHeader());
        inOrder.verify(cborGenerator).writeObject(issuerAuth.unprotectedHeader());
        inOrder.verify(cborGenerator).writeBinary(issuerAuth.payload());
        inOrder.verify(cborGenerator).writeBinary(issuerAuth.signature());
        inOrder.verify(cborGenerator).writeEndArray();
        inOrder.verify(cborGenerator).writeEndObject();
    }

    @Test
    void Should_SerializeIssuerSignedWithCBORGenerator_MultipleNameSpaces() throws IOException {
        Map<String, List<IssuerSignedItem>> namespaces = new LinkedHashMap<>();
        namespaces.put("namespace1", List.of(buildTestItem(1)));
        namespaces.put("namespace2", List.of(buildTestItem(1)));
        COSESign1 issuerAuth = buildTestIssuerAuth();
        IssuerSigned valueToSerialize = new IssuerSigned(namespaces, issuerAuth);

        new IssuerSignedCBORSerializer()
                .serialize(valueToSerialize, cborGenerator, serializerProvider);

        InOrder inOrder = inOrder(cborGenerator);
        inOrder.verify(cborGenerator).writeStartObject();
        inOrder.verify(cborGenerator).writeFieldName("nameSpaces");
        inOrder.verify(cborGenerator).writeStartObject();
        inOrder.verify(cborGenerator).writeFieldName("namespace1");
        inOrder.verify(cborGenerator).writeStartArray();
        inOrder.verify(cborGenerator).writeObject(namespaces.get("namespace1").get(0));
        inOrder.verify(cborGenerator).writeEndArray();
        inOrder.verify(cborGenerator).writeFieldName("namespace2");
        inOrder.verify(cborGenerator).writeStartArray();
        inOrder.verify(cborGenerator).writeObject(namespaces.get("namespace2").get(0));
        inOrder.verify(cborGenerator).writeEndArray();
        inOrder.verify(cborGenerator).writeEndObject();
        inOrder.verify(cborGenerator).writeFieldName("issuerAuth");
        inOrder.verify(cborGenerator).writeStartArray();
        inOrder.verify(cborGenerator).writeBinary(issuerAuth.protectedHeader());
        inOrder.verify(cborGenerator).writeObject(issuerAuth.unprotectedHeader());
        inOrder.verify(cborGenerator).writeBinary(issuerAuth.payload());
        inOrder.verify(cborGenerator).writeBinary(issuerAuth.signature());
        inOrder.verify(cborGenerator).writeEndArray();
        inOrder.verify(cborGenerator).writeEndObject();
    }

    @Test
    void Should_ThrowIllegalArgumentException_When_SerializerIsNonCBORGenerator() {
        JsonGenerator invalidGenerator = mock(JsonGenerator.class);

        IllegalArgumentException exception =
                org.junit.jupiter.api.Assertions.assertThrows(
                        IllegalArgumentException.class,
                        () -> {
                            new IssuerSignedCBORSerializer()
                                    .serialize(
                                            mock(IssuerSigned.class), invalidGenerator, serializerProvider);
                        });
        assertEquals("Requires CBORGenerator", exception.getMessage());
    }

    private IssuerSignedItem buildTestItem(int id) {
        return new IssuerSignedItem(id, new byte[] {1, 2}, "elementId", "elementValue");
    }

    private COSESign1 buildTestIssuerAuth() {
        byte[] protectedHeader = {1, 2, 3, 4};
        byte[] x5chain = {5, 6, 7, 8};
        byte[] payload = {9, 10, 11, 12};
        byte[] signature = {13, 14, 15, 16};
        COSEUnprotectedHeader unprotectedHeader =
                new COSEUnprotectedHeaderBuilder().x5chain(x5chain).build();
        return new COSESign1(protectedHeader, unprotectedHeader, payload, signature);
    }
}
