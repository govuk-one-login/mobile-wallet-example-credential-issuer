package uk.gov.di.mobile.wallet.cri.credential.mobile_driving_licence.cbor.mobile_driving_licence;

import com.fasterxml.jackson.core.JsonGenerator;
import com.fasterxml.jackson.databind.SerializerProvider;
import com.fasterxml.jackson.dataformat.cbor.CBORGenerator;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InOrder;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import uk.gov.di.mobile.wallet.cri.credential.mobile_driving_licence.Code;
import uk.gov.di.mobile.wallet.cri.credential.mobile_driving_licence.DrivingPrivilege;

import java.io.IOException;
import java.time.LocalDate;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.Mockito.inOrder;
import static org.mockito.Mockito.mock;

@ExtendWith(MockitoExtension.class)
class DrivingPrivilegeSerializerTest {

    private final DrivingPrivilegeSerializer serializer = new DrivingPrivilegeSerializer();
    @Mock private CBORGenerator cborGenerator;
    @Mock private SerializerProvider serializerProvider;

    @Test
    void Should_SerializeDrivingPrivilege_OneProperty() throws IOException {
        DrivingPrivilege valueToSerialize = new DrivingPrivilege("B1", null, null, null);

        serializer.serialize(valueToSerialize, cborGenerator, serializerProvider);

        InOrder inOrder = inOrder(cborGenerator);
        inOrder.verify(cborGenerator).writeStartObject(1);
        inOrder.verify(cborGenerator).writeStringField("vehicle_category_code", "B1");
        inOrder.verify(cborGenerator).writeEndObject();
    }

    @Test
    void Should_SerializeDrivingPrivilege_TwoProperties() throws IOException {
        DrivingPrivilege valueToSerialize = new DrivingPrivilege("B1", "01-01-2024", null, null);

        serializer.serialize(valueToSerialize, cborGenerator, serializerProvider);

        InOrder inOrder = inOrder(cborGenerator);
        inOrder.verify(cborGenerator).writeStartObject(2);
        inOrder.verify(cborGenerator).writeStringField("vehicle_category_code", "B1");
        inOrder.verify(cborGenerator).writeFieldName("issue_date");
        inOrder.verify(cborGenerator).writeObject(LocalDate.of(2024, 1, 1));
        inOrder.verify(cborGenerator).writeEndObject();
    }

    @Test
    void Should_SerializeDrivingPrivilege_ThreeProperties() throws IOException {
        DrivingPrivilege valueToSerialize =
                new DrivingPrivilege("B1", "01-01-2024", "31-12-2025", null);

        serializer.serialize(valueToSerialize, cborGenerator, serializerProvider);

        InOrder inOrder = inOrder(cborGenerator);
        inOrder.verify(cborGenerator).writeStartObject(3);
        inOrder.verify(cborGenerator).writeStringField("vehicle_category_code", "B1");
        inOrder.verify(cborGenerator).writeFieldName("issue_date");
        inOrder.verify(cborGenerator).writeObject(LocalDate.of(2024, 1, 1));
        inOrder.verify(cborGenerator).writeFieldName("expiry_date");
        inOrder.verify(cborGenerator).writeObject(LocalDate.of(2025, 12, 31));
        inOrder.verify(cborGenerator).writeEndObject();
    }

    @Test
    void Should_SerializeDrivingPrivilege_FourProperties() throws IOException {
        DrivingPrivilege valueToSerialize =
                new DrivingPrivilege(
                        "B1", "01-01-2024", "31-12-2025", List.of(new Code("A"), new Code("B")));

        serializer.serialize(valueToSerialize, cborGenerator, serializerProvider);

        InOrder inOrder = inOrder(cborGenerator);
        inOrder.verify(cborGenerator).writeStartObject(4);
        inOrder.verify(cborGenerator).writeStringField("vehicle_category_code", "B1");
        inOrder.verify(cborGenerator).writeFieldName("issue_date");
        inOrder.verify(cborGenerator).writeObject(LocalDate.of(2024, 1, 1));
        inOrder.verify(cborGenerator).writeFieldName("expiry_date");
        inOrder.verify(cborGenerator).writeObject(LocalDate.of(2025, 12, 31));
        inOrder.verify(cborGenerator).writeFieldName("codes");
        inOrder.verify(cborGenerator).writeStartArray(List.of(new Code("A"), new Code("B")), 2);
        inOrder.verify(cborGenerator).writeStartObject(1);
        inOrder.verify(cborGenerator).writeFieldName("code");
        inOrder.verify(cborGenerator).writeString("A");
        inOrder.verify(cborGenerator).writeEndObject();
        inOrder.verify(cborGenerator).writeStartObject(1);
        inOrder.verify(cborGenerator).writeFieldName("code");
        inOrder.verify(cborGenerator).writeString("B");
        inOrder.verify(cborGenerator).writeEndObject();
        inOrder.verify(cborGenerator).writeEndArray();
        inOrder.verify(cborGenerator).writeEndObject();
    }

    @Test
    void Should_ThrowIllegalArgumentException_When_SerializerIsNonCBORGenerator() {
        JsonGenerator invalidGenerator = mock(JsonGenerator.class);

        IllegalArgumentException exception =
                assertThrows(
                        IllegalArgumentException.class,
                        () -> {
                            serializer.serialize(
                                    mock(DrivingPrivilege.class),
                                    invalidGenerator,
                                    serializerProvider);
                        });
        assertEquals("Requires CBORGenerator", exception.getMessage());
    }
}
