package uk.gov.di.mobile.wallet.cri.credential.mobile_driving_licence.cbor;

import com.fasterxml.jackson.core.JsonGenerator;
import com.fasterxml.jackson.databind.JsonSerializer;
import com.fasterxml.jackson.databind.SerializerProvider;
import com.fasterxml.jackson.dataformat.cbor.CBORGenerator;
import uk.gov.di.mobile.wallet.cri.credential.mobile_driving_licence.mdoc.DeviceKeyInfo;

import java.io.IOException;

public class DeviceKeyInfoCBORSerializer extends JsonSerializer<DeviceKeyInfo> {
    @Override
    public void serialize(
            DeviceKeyInfo value, JsonGenerator generator, SerializerProvider serializers)
            throws IOException {
        if (!(generator instanceof CBORGenerator cborGenerator)) {
            throw new IllegalArgumentException("Requires CBORGenerator");
        }

        cborGenerator.writeStartObject(2);
        cborGenerator.writeFieldName("deviceKey");
        cborGenerator.writeObject(value.deviceKey());
        cborGenerator.writeFieldName("keyAuthorizations");
        cborGenerator.writeStartObject(1);
        cborGenerator.writeFieldName("nameSpaces");
        cborGenerator.writeObject(value.keyAuthorizations().nameSpaces());
        cborGenerator.writeEndObject();
        cborGenerator.writeEndObject();
    }
}
