package uk.gov.di.mobile.wallet.cri.credential.mobile_driving_licence.cbor;

import com.fasterxml.jackson.core.JsonGenerator;
import com.fasterxml.jackson.databind.JsonSerializer;
import com.fasterxml.jackson.databind.SerializerProvider;
import com.fasterxml.jackson.dataformat.cbor.CBORGenerator;
import uk.gov.di.mobile.wallet.cri.credential.mobile_driving_licence.mdoc.ValidityInfo;

import java.io.IOException;

public class ValidityInfoCBORSerializer extends JsonSerializer<ValidityInfo> {
    @Override
    public void serialize(
            ValidityInfo value, JsonGenerator generator, SerializerProvider serializers)
            throws IOException {
        if (!(generator instanceof CBORGenerator cborGenerator)) {
            throw new IllegalArgumentException("Requires CBORGenerator");
        }

        cborGenerator.writeStartObject(3);
        cborGenerator.writeFieldName("signed");
        cborGenerator.writeObject(value.signed());
        cborGenerator.writeFieldName("validFrom");
        cborGenerator.writeObject(value.validFrom());
        cborGenerator.writeFieldName("validTo");
        cborGenerator.writeObject(value.validTo());
        cborGenerator.writeEndObject();
    }
}
