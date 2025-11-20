package uk.gov.di.mobile.wallet.cri.credential.mobile_driving_licence.cbor;

import com.fasterxml.jackson.core.JsonGenerator;
import com.fasterxml.jackson.databind.SerializerProvider;
import com.fasterxml.jackson.dataformat.cbor.CBORGenerator;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InOrder;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import uk.gov.di.mobile.wallet.cri.credential.mobile_driving_licence.cose.COSEKey;
import uk.gov.di.mobile.wallet.cri.credential.mobile_driving_licence.mdoc.DeviceKeyInfo;
import uk.gov.di.mobile.wallet.cri.credential.mobile_driving_licence.mdoc.KeyAuthorizations;

import java.io.IOException;
import java.util.LinkedHashSet;
import java.util.Set;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.inOrder;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.times;

@ExtendWith(MockitoExtension.class)
class DeviceKeyInfoSerializerTest {

    private final DeviceKeyInfoSerializer serializer = new DeviceKeyInfoSerializer();
    @Mock private CBORGenerator cborGenerator;
    @Mock private SerializerProvider serializerProvider;

    @Test
    void Should_SerializeDeviceKeyInfo() throws IOException {
        COSEKey coseKey = new COSEKey(2, 1, new byte[] {0x01}, new byte[] {0x02});
        Set<String> namespaces = new LinkedHashSet<>();
        namespaces.add("org.iso.18013.5.1");
        namespaces.add("org.iso.18013.5.1.GB");
        DeviceKeyInfo valueToSerialize =
                new DeviceKeyInfo(coseKey, new KeyAuthorizations(namespaces));

        serializer.serialize(valueToSerialize, cborGenerator, serializerProvider);

        InOrder inOrder = inOrder(cborGenerator);
        inOrder.verify(cborGenerator).writeStartObject(2);
        inOrder.verify(cborGenerator).writeFieldName("deviceKey");
        inOrder.verify(cborGenerator).writeStartObject(4);
        inOrder.verify(cborGenerator).writeFieldId(1);
        inOrder.verify(cborGenerator).writeNumber(2);
        inOrder.verify(cborGenerator).writeFieldId(-1);
        inOrder.verify(cborGenerator).writeNumber(1);
        inOrder.verify(cborGenerator).writeFieldId(-2);
        inOrder.verify(cborGenerator).writeBinary(new byte[] {0x01});
        inOrder.verify(cborGenerator).writeFieldId(-3);
        inOrder.verify(cborGenerator).writeBinary(new byte[] {0x02});
        inOrder.verify(cborGenerator).writeEndObject();
        inOrder.verify(cborGenerator).writeFieldName("keyAuthorizations");
        inOrder.verify(cborGenerator).writeStartObject(1);
        inOrder.verify(cborGenerator).writeFieldName("nameSpaces");
        inOrder.verify(cborGenerator).writeStartArray(any(), eq(2));
        inOrder.verify(cborGenerator).writeString("org.iso.18013.5.1");
        inOrder.verify(cborGenerator).writeString("org.iso.18013.5.1.GB");
        inOrder.verify(cborGenerator).writeEndArray();
        inOrder.verify(cborGenerator, times(2)).writeEndObject();
    }

    @Test
    void Should_ThrowIllegalArgumentException_When_SerializerIsNonCBORGenerator() {
        JsonGenerator invalidGenerator = mock(JsonGenerator.class);

        IllegalArgumentException exception =
                assertThrows(
                        IllegalArgumentException.class,
                        () ->
                                serializer.serialize(
                                        mock(DeviceKeyInfo.class),
                                        invalidGenerator,
                                        serializerProvider));
        assertEquals("Requires CBORGenerator", exception.getMessage());
    }
}
