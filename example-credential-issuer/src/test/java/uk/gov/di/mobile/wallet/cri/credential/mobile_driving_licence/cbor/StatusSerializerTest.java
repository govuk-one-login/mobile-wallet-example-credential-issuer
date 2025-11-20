package uk.gov.di.mobile.wallet.cri.credential.mobile_driving_licence.cbor;

import com.fasterxml.jackson.core.JsonGenerator;
import com.fasterxml.jackson.databind.SerializerProvider;
import com.fasterxml.jackson.dataformat.cbor.CBORGenerator;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InOrder;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import uk.gov.di.mobile.wallet.cri.credential.mobile_driving_licence.mdoc.Status;
import uk.gov.di.mobile.wallet.cri.credential.mobile_driving_licence.mdoc.StatusList;

import java.io.IOException;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.Mockito.inOrder;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.times;

@ExtendWith(MockitoExtension.class)
class StatusSerializerTest {

    @Mock private CBORGenerator cborGenerator;
    @Mock private SerializerProvider serializerProvider;

    @Test
    void Should_SerializeStatus() throws IOException {
        Status valueToSerialize = new Status(new StatusList(5, "https://test-status-list/123"));

        new StatusSerializer().serialize(valueToSerialize, cborGenerator, serializerProvider);

        InOrder inOrder = inOrder(cborGenerator);
        inOrder.verify(cborGenerator).writeStartObject(1);
        inOrder.verify(cborGenerator).writeFieldName("status_list");
        inOrder.verify(cborGenerator).writeStartObject(2);
        inOrder.verify(cborGenerator).writeFieldName("idx");
        inOrder.verify(cborGenerator).writeNumber(5);
        inOrder.verify(cborGenerator).writeFieldName("uri");
        inOrder.verify(cborGenerator).writeString("https://test-status-list/123");
        inOrder.verify(cborGenerator, times(2)).writeEndObject();
    }

    @Test
    void Should_ThrowIllegalArgumentException_When_SerializerIsNonCBORGenerator() {
        JsonGenerator invalidGenerator = mock(JsonGenerator.class);

        IllegalArgumentException exception =
                org.junit.jupiter.api.Assertions.assertThrows(
                        IllegalArgumentException.class,
                        () -> {
                            new StatusSerializer()
                                    .serialize(
                                            mock(Status.class), invalidGenerator, serializerProvider);
                        });
        assertEquals("Requires CBORGenerator", exception.getMessage());
    }
}
