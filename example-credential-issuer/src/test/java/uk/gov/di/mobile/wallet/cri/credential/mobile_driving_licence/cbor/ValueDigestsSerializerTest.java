package uk.gov.di.mobile.wallet.cri.credential.mobile_driving_licence.cbor;

import com.fasterxml.jackson.core.JsonGenerator;
import com.fasterxml.jackson.databind.SerializerProvider;
import com.fasterxml.jackson.dataformat.cbor.CBORGenerator;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InOrder;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import uk.gov.di.mobile.wallet.cri.credential.mobile_driving_licence.mdoc.ValueDigests;

import java.io.IOException;
import java.util.HashMap;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.Mockito.inOrder;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.times;

@ExtendWith(MockitoExtension.class)
class ValueDigestsSerializerTest {

    @Mock private CBORGenerator cborGenerator;
    @Mock private SerializerProvider serializerProvider;

    @Test
    void Should_SerializeValueDigests_WithSortedDigests() throws IOException {
        Map<Integer, byte[]> elements = new HashMap<>();
        elements.put(5, new byte[] {0x05});
        elements.put(1, new byte[] {0x01});
        elements.put(3, new byte[] {0x03});
        Map<String, Map<Integer, byte[]>> namespaces = new HashMap<>();
        namespaces.put("org.iso.18013.5.1", elements);
        ValueDigests valueToSerialize = new ValueDigests(namespaces);

        new ValueDigestsSerializer().serialize(valueToSerialize, cborGenerator, serializerProvider);

        InOrder inOrder = inOrder(cborGenerator);
        inOrder.verify(cborGenerator).writeStartObject(1);
        inOrder.verify(cborGenerator).writeFieldName("org.iso.18013.5.1");
        inOrder.verify(cborGenerator).writeStartObject(3);
        inOrder.verify(cborGenerator).writeFieldId(1);
        inOrder.verify(cborGenerator).writeBinary(new byte[] {0x01});
        inOrder.verify(cborGenerator).writeFieldId(3);
        inOrder.verify(cborGenerator).writeBinary(new byte[] {0x03});
        inOrder.verify(cborGenerator).writeFieldId(5);
        inOrder.verify(cborGenerator).writeBinary(new byte[] {0x05});
        inOrder.verify(cborGenerator, times(2)).writeEndObject();
    }

    @Test
    void Should_ThrowIllegalArgumentException_When_SerializerIsNonCBORGenerator() {
        JsonGenerator invalidGenerator = mock(JsonGenerator.class);
        ValueDigests value =
                new ValueDigests(Map.of("org.iso.18013.5.1", Map.of(1, new byte[] {0x01})));

        IllegalArgumentException exception =
                assertThrows(
                        IllegalArgumentException.class,
                        () ->
                                new ValueDigestsSerializer()
                                        .serialize(value, invalidGenerator, serializerProvider));
        assertEquals("Requires CBORGenerator", exception.getMessage());
    }
}
