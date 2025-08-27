package uk.gov.di.mobile.wallet.cri.credential.mobile_driving_licence.cbor;

import com.fasterxml.jackson.core.JsonGenerator;
import com.fasterxml.jackson.databind.JsonSerializer;
import com.fasterxml.jackson.databind.SerializerProvider;
import com.fasterxml.jackson.dataformat.cbor.CBORGenerator;

import java.io.IOException;
import java.util.List;

public class DefiniteLengthListSerializer extends JsonSerializer<List> {
    @Override
    public void serialize(List list, JsonGenerator generator, SerializerProvider serializers)
            throws IOException {
        if (!(generator instanceof CBORGenerator cborGenerator)) {
            throw new IllegalArgumentException("Requires CBORGenerator");
        }

        cborGenerator.writeStartArray(list.size());
        for (Object item : list) {
            serializers.defaultSerializeValue(item, generator);
        }
        cborGenerator.writeEndArray();
    }
}
