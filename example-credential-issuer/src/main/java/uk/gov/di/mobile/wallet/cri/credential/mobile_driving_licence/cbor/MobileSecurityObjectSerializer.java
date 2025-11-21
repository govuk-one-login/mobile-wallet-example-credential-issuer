package uk.gov.di.mobile.wallet.cri.credential.mobile_driving_licence.cbor;

import com.fasterxml.jackson.core.JsonGenerator;
import com.fasterxml.jackson.databind.SerializerProvider;
import com.fasterxml.jackson.databind.ser.std.StdSerializer;
import com.fasterxml.jackson.dataformat.cbor.CBORFactory;
import com.fasterxml.jackson.dataformat.cbor.CBORGenerator;
import uk.gov.di.mobile.wallet.cri.credential.mobile_driving_licence.mdoc.MobileSecurityObject;

import java.io.ByteArrayOutputStream;
import java.io.IOException;

public class MobileSecurityObjectSerializer extends StdSerializer<MobileSecurityObject> {
    public MobileSecurityObjectSerializer() {
        super(MobileSecurityObject.class);
    }

    @Override
    public void serialize(
            final MobileSecurityObject value,
            final JsonGenerator generator,
            final SerializerProvider serializer)
            throws IOException {
        if (!(generator instanceof CBORGenerator cborGenerator)) {
            throw new IllegalArgumentException("Requires CBORGenerator");
        }

        CBORFactory factory = new CBORFactory();
        ByteArrayOutputStream baos = new ByteArrayOutputStream();
        try (CBORGenerator innerGenerator = factory.createGenerator(baos)) {
            innerGenerator.setCodec(generator.getCodec());
            MobileSecurityObjectWriter.write(innerGenerator, value);
        }
        cborGenerator.writeTag(24);
        cborGenerator.writeBinary(baos.toByteArray());
    }
}
