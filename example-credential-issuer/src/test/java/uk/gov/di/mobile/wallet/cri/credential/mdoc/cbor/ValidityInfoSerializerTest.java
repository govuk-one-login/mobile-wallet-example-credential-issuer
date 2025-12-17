package uk.gov.di.mobile.wallet.cri.credential.mdoc.cbor;

import com.fasterxml.jackson.core.JsonGenerator;
import com.fasterxml.jackson.databind.SerializerProvider;
import com.fasterxml.jackson.dataformat.cbor.CBORGenerator;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InOrder;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import uk.gov.di.mobile.wallet.cri.credential.mdoc.ValidityInfo;

import java.io.IOException;
import java.time.Instant;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.Mockito.inOrder;
import static org.mockito.Mockito.mock;

@ExtendWith(MockitoExtension.class)
class ValidityInfoSerializerTest {

    private final ValidityInfoSerializer serializer = new ValidityInfoSerializer();
    @Mock private CBORGenerator cborGenerator;
    @Mock private SerializerProvider serializerProvider;

    @Test
    void Should_SerializeValidityInfo() throws IOException {
        Instant signed = Instant.parse("2025-01-01T00:00:00Z");
        Instant validFrom = Instant.parse("2025-01-01T00:00:00Z");
        Instant validUntil = Instant.parse("2025-01-01T00:00:00Z");
        ValidityInfo valueToSerialize = new ValidityInfo(signed, validFrom, validUntil);

        serializer.serialize(valueToSerialize, cborGenerator, serializerProvider);

        InOrder inOrder = inOrder(cborGenerator);
        inOrder.verify(cborGenerator).writeStartObject(3);
        inOrder.verify(cborGenerator).writeFieldName("signed");
        inOrder.verify(cborGenerator).writeObject(signed);
        inOrder.verify(cborGenerator).writeFieldName("validFrom");
        inOrder.verify(cborGenerator).writeObject(validFrom);
        inOrder.verify(cborGenerator).writeFieldName("validUntil");
        inOrder.verify(cborGenerator).writeObject(validUntil);
        cborGenerator.writeEndObject();
    }

    @Test
    void Should_ThrowIllegalArgumentException_When_SerializerIsNonCBORGenerator() {
        JsonGenerator invalidGenerator = mock(JsonGenerator.class);

        IllegalArgumentException exception =
                assertThrows(
                        IllegalArgumentException.class,
                        () -> {
                            serializer.serialize(
                                    mock(ValidityInfo.class), invalidGenerator, serializerProvider);
                        });
        assertEquals("Requires CBORGenerator", exception.getMessage());
    }
}
