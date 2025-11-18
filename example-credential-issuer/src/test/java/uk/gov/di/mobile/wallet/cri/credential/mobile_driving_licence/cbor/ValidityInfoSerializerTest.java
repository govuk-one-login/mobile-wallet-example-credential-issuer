package uk.gov.di.mobile.wallet.cri.credential.mobile_driving_licence.cbor;

import com.fasterxml.jackson.databind.JsonMappingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.module.SimpleModule;
import com.fasterxml.jackson.dataformat.cbor.CBORGenerator;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InOrder;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import uk.gov.di.mobile.wallet.cri.credential.mobile_driving_licence.mdoc.ValidityInfo;

import java.io.IOException;
import java.time.Instant;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.Mockito.inOrder;

@ExtendWith(MockitoExtension.class)
class ValidityInfoSerializerTest {

    @Mock private CBORGenerator cborGenerator;

    @Test
    void Should_SerializeValidityInfo_WithCBORGenerator() throws IOException {
        ValidityInfo value =
                new ValidityInfo(
                        Instant.parse("2025-01-01T00:00:00Z"),
                        Instant.parse("2025-01-01T00:00:00Z"),
                        Instant.parse("2025-12-31T23:59:59Z"));

        new ValidityInfoSerializer().serialize(value, cborGenerator, null);

        InOrder inOrder = inOrder(cborGenerator);
        inOrder.verify(cborGenerator).writeStartObject(3);
        inOrder.verify(cborGenerator).writeFieldName("signed");
        inOrder.verify(cborGenerator).writeObject(value.signed());
        inOrder.verify(cborGenerator).writeFieldName("validFrom");
        inOrder.verify(cborGenerator).writeObject(value.validFrom());
        inOrder.verify(cborGenerator).writeFieldName("validUntil");
        inOrder.verify(cborGenerator).writeObject(value.validUntil());
        inOrder.verify(cborGenerator).writeEndObject();
    }

    @Test
    void Should_ThrowIllegalArgumentException_When_SerializerIsNonCBORGenerator() {
        ObjectMapper jsonMapper = new ObjectMapper();
        SimpleModule module = new SimpleModule();
        module.addSerializer(new ValidityInfoSerializer());
        jsonMapper.registerModule(module);

        ValidityInfo value =
                new ValidityInfo(
                        Instant.parse("2025-01-01T00:00:00Z"),
                        Instant.parse("2025-01-01T00:00:00Z"),
                        Instant.parse("2025-12-31T23:59:59Z"));

        JsonMappingException ex =
                assertThrows(JsonMappingException.class, () -> jsonMapper.writeValueAsBytes(value));
        assertEquals(IllegalArgumentException.class, ex.getCause().getClass());
        assertEquals("Requires CBORGenerator", ex.getCause().getMessage());
    }
}
