package uk.gov.di.mobile.wallet.cri.credential.mobile_driving_licence.cbor;

import com.fasterxml.jackson.core.JsonGenerator;
import com.fasterxml.jackson.databind.SerializerProvider;
import com.fasterxml.jackson.dataformat.cbor.CBORGenerator;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InOrder;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import uk.gov.di.mobile.wallet.cri.credential.mobile_driving_licence.mdoc.IssuerSignedItem;

import java.io.IOException;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.Mockito.inOrder;
import static org.mockito.Mockito.mock;

@ExtendWith(MockitoExtension.class)
class IssuerSignedItemCBORSerializerTest {

    @Mock private CBORGenerator cborGenerator;
    @Mock private SerializerProvider serializerProvider;

    @Test
    void Should_SerializeIssuerSignedItem_AsTaggedEncodedCBORDataItem() throws IOException {
        IssuerSignedItem valueToSerialize =
                new IssuerSignedItem(
                        1,
                        new byte[] {0x01, 0x02, 0x03},
                        "test_element_identifier",
                        "Test Element Value");

        new IssuerSignedItemCBORSerializer()
                .serialize(valueToSerialize, cborGenerator, serializerProvider);

        InOrder inOrder = inOrder(cborGenerator);
        inOrder.verify(cborGenerator).writeTag(24);
        var bytesCaptor = ArgumentCaptor.forClass(byte[].class);
        inOrder.verify(cborGenerator).writeBinary(bytesCaptor.capture());
    }

    @Test
    void Should_ThrowIllegalArgumentException_When_SerializerIsNonCBORGenerator() {
        JsonGenerator invalidGenerator = mock(JsonGenerator.class);

        IllegalArgumentException exception =
                assertThrows(
                        IllegalArgumentException.class,
                        () ->
                                new IssuerSignedItemCBORSerializer()
                                        .serialize(
                                                mock(IssuerSignedItem.class),
                                                invalidGenerator,
                                                serializerProvider));
        assertEquals("Requires CBORGenerator", exception.getMessage());
    }
}
