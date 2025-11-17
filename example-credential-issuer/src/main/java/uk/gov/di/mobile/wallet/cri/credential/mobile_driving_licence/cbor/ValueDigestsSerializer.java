package uk.gov.di.mobile.wallet.cri.credential.mobile_driving_licence.cbor;

import com.fasterxml.jackson.core.JsonGenerator;
import com.fasterxml.jackson.databind.SerializerProvider;
import com.fasterxml.jackson.databind.ser.std.StdSerializer;
import com.fasterxml.jackson.dataformat.cbor.CBORGenerator;
import uk.gov.di.mobile.wallet.cri.credential.mobile_driving_licence.mdoc.ValueDigests;

import java.io.IOException;
import java.util.Map;
import java.util.TreeMap;

public class ValueDigestsSerializer extends StdSerializer<ValueDigests> {
    public ValueDigestsSerializer() {
        super(ValueDigests.class);
    }

    @Override
    public void serialize(
            ValueDigests value, JsonGenerator generator, SerializerProvider serializer)
            throws IOException {
        if (!(generator instanceof CBORGenerator cborGenerator)) {
            throw new IllegalArgumentException("Requires CBORGenerator");
        }

        Map<String, Map<Integer, byte[]>> namespaces = value.valueDigests();

        cborGenerator.writeStartObject(namespaces.size());
        for (var namespace : namespaces.entrySet()) {
            cborGenerator.writeFieldName(namespace.getKey());
            Map<Integer, byte[]> elements = namespace.getValue();
            if (elements == null) {
                cborGenerator.writeStartObject(0);
                cborGenerator.writeEndObject();
                continue;
            }
            var sortedElements = new TreeMap<>(elements);
            cborGenerator.writeStartObject(sortedElements.size());
            for (var element : sortedElements.entrySet()) {
                cborGenerator.writeFieldId(element.getKey());
                cborGenerator.writeBinary(element.getValue());
            }
            cborGenerator.writeEndObject();
        }
        cborGenerator.writeEndObject();
    }
}
