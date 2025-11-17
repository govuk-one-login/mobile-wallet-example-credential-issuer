package uk.gov.di.mobile.wallet.cri.credential.mobile_driving_licence.cbor;

import com.fasterxml.jackson.core.JsonGenerator;
import com.fasterxml.jackson.databind.SerializerProvider;
import com.fasterxml.jackson.databind.ser.std.StdSerializer;
import com.fasterxml.jackson.dataformat.cbor.CBORGenerator;
import uk.gov.di.mobile.wallet.cri.credential.mobile_driving_licence.cose.COSEProtectedHeader;

import java.io.IOException;

public class COSEProtectedHeaderSerializer extends StdSerializer<COSEProtectedHeader> {
    public COSEProtectedHeaderSerializer() {
        super(COSEProtectedHeader.class);
    }

    @Override
    public void serialize(
            final COSEProtectedHeader value,
            final JsonGenerator generator,
            final SerializerProvider serializer)
            throws IOException {

        if (!(generator instanceof CBORGenerator cborGenerator)) {
            throw new IllegalArgumentException("Requires CBORGenerator");
        }

        cborGenerator.writeStartObject(1);
        cborGenerator.writeFieldId(1); // from RFC9052 3.1
        cborGenerator.writeNumber(value.getAlg());
        cborGenerator.writeEndObject();
    }
}
