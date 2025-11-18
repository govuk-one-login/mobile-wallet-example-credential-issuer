package uk.gov.di.mobile.wallet.cri.credential.mobile_driving_licence.cbor;

import com.fasterxml.jackson.core.JsonGenerator;
import com.fasterxml.jackson.databind.SerializerProvider;
import com.fasterxml.jackson.databind.ser.std.StdSerializer;
import com.fasterxml.jackson.dataformat.cbor.CBORGenerator;
import uk.gov.di.mobile.wallet.cri.credential.mobile_driving_licence.mdoc.DeviceKeyInfo;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

public class DeviceKeyInfoSerializer extends StdSerializer<DeviceKeyInfo> {
    public DeviceKeyInfoSerializer() {
        super(DeviceKeyInfo.class);
    }

    @Override
    public void serialize(
            final DeviceKeyInfo value,
            final JsonGenerator generator,
            final SerializerProvider serializer)
            throws IOException {

        if (!(generator instanceof CBORGenerator cborGenerator)) {
            throw new IllegalArgumentException("Requires CBORGenerator");
        }

        cborGenerator.writeStartObject(2);

        cborGenerator.writeFieldName("deviceKey");
        cborGenerator.writeStartObject(4);
        cborGenerator.writeFieldId(1); // from RFC8152 13.1
        cborGenerator.writeNumber(value.deviceKey().keyType());
        cborGenerator.writeFieldId(-1); // from RFC8152 13.1
        cborGenerator.writeNumber(value.deviceKey().curve());
        cborGenerator.writeFieldId(-2); // from RFC8152 13.1
        cborGenerator.writeBinary(value.deviceKey().x());
        cborGenerator.writeFieldId(-3); // from RFC8152 13.1
        cborGenerator.writeBinary(value.deviceKey().y());
        cborGenerator.writeEndObject();

        cborGenerator.writeFieldName("keyAuthorizations");
        cborGenerator.writeStartObject(1);
        cborGenerator.writeFieldName("nameSpaces");
        List<String> namespaces = new ArrayList<>(value.keyAuthorizations().nameSpaces());
        cborGenerator.writeStartArray(namespaces, namespaces.size());
        for (String namespace : namespaces) {
            cborGenerator.writeString(namespace);
        }
        cborGenerator.writeEndArray();
        cborGenerator.writeEndObject();

        cborGenerator.writeEndObject();
    }
}
