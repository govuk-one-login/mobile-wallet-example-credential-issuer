package uk.gov.di.mobile.wallet.cri.credential.mobile_driving_licence.cbor;

import com.fasterxml.jackson.core.JsonGenerator;
import com.fasterxml.jackson.databind.SerializerProvider;
import com.fasterxml.jackson.dataformat.cbor.CBORGenerator;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InOrder;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import uk.gov.di.mobile.wallet.cri.credential.mobile_driving_licence.cose.COSEUnprotectedHeader;
import uk.gov.di.mobile.wallet.cri.credential.mobile_driving_licence.cose.COSEUnprotectedHeaderBuilder;

import java.io.IOException;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.Mockito.inOrder;
import static org.mockito.Mockito.mock;

@ExtendWith(MockitoExtension.class)
class COSEUnprotectedHeaderSerializerTest {

    @Mock private CBORGenerator cborGenerator;
    @Mock private SerializerProvider serializerProvider;

    @Test
    void Should_SerializeCOSEUnprotectedHeader() throws IOException {
        COSEUnprotectedHeader valueToSerialize =
                new COSEUnprotectedHeaderBuilder().x5chain(new byte[] {1, 2, 3}).build();

        new COSEUnprotectedHeaderSerializer()
                .serialize(valueToSerialize, cborGenerator, serializerProvider);

        InOrder inOrder = inOrder(cborGenerator);
        inOrder.verify(cborGenerator).writeStartObject(1);
        inOrder.verify(cborGenerator).writeFieldId(33); // 33="x5chain"
        inOrder.verify(cborGenerator).writeBinary(new byte[] {1, 2, 3});
        inOrder.verify(cborGenerator).writeEndObject();
    }

    @Test
    void Should_ThrowIllegalArgumentException_When_SerializerIsNonCBORGenerator() {
        JsonGenerator invalidGenerator = mock(JsonGenerator.class);

        IllegalArgumentException exception =
                org.junit.jupiter.api.Assertions.assertThrows(
                        IllegalArgumentException.class,
                        () -> {
                            new COSEUnprotectedHeaderSerializer()
                                    .serialize(
                                            mock(COSEUnprotectedHeader.class), invalidGenerator, serializerProvider);
                        });
        assertEquals("Requires CBORGenerator", exception.getMessage());
    }
}
