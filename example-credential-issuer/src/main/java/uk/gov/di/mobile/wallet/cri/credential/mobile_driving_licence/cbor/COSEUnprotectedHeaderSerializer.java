package uk.gov.di.mobile.wallet.cri.credential.mobile_driving_licence.cbor;

import com.fasterxml.jackson.core.JsonGenerator;
import com.fasterxml.jackson.databind.SerializerProvider;
import com.fasterxml.jackson.databind.ser.std.StdSerializer;
import com.fasterxml.jackson.dataformat.cbor.CBORGenerator;
import uk.gov.di.mobile.wallet.cri.credential.mobile_driving_licence.cose.COSEUnprotectedHeader;

import java.io.IOException;

public class COSEUnprotectedHeaderSerializer extends StdSerializer<COSEUnprotectedHeader> {
    public COSEUnprotectedHeaderSerializer() {
        super(COSEUnprotectedHeader.class);
    }

    @Override
    public void serialize(
            final COSEUnprotectedHeader value,
            final JsonGenerator generator,
            final SerializerProvider serializer)
            throws IOException {

        if (!(generator instanceof CBORGenerator cborGenerator)) {
            throw new IllegalArgumentException("Requires CBORGenerator");
        }

        cborGenerator.writeStartObject(1);
        cborGenerator.writeFieldId(33); // from RFC9360 2
        cborGenerator.writeBinary(value.getX5chain());
        cborGenerator.writeEndObject();
    }
}
