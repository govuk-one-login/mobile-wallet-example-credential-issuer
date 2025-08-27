package uk.gov.di.mobile.wallet.cri.credential.mobile_driving_licence.cbor;

import com.fasterxml.jackson.core.JsonGenerator;
import com.fasterxml.jackson.databind.JsonSerializer;
import com.fasterxml.jackson.databind.SerializerProvider;
import com.fasterxml.jackson.dataformat.cbor.CBORGenerator;

import java.io.IOException;
import java.util.Map;

public class DefiniteLengthMapSerializer extends JsonSerializer<Map> {
    @Override
    public void serialize(Map map, JsonGenerator generator, SerializerProvider serializers)
            throws IOException {
        if (!(generator instanceof CBORGenerator cborGenerator)) {
            throw new IllegalArgumentException("Requires CBORGenerator");
        }

        cborGenerator.writeStartObject(map.size());
        for (Object entryObj : map.entrySet()) {
            Map.Entry<?, ?> entry = (Map.Entry<?, ?>) entryObj;
            if (entry.getKey() instanceof String) {
                cborGenerator.writeFieldName((String) entry.getKey());
            } else {
                cborGenerator.writeFieldName(String.valueOf(entry.getKey()));
            }
            serializers.defaultSerializeValue(entry.getValue(), generator);
        }
        cborGenerator.writeEndObject();
    }
}
