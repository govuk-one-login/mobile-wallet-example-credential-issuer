package uk.gov.di.mobile.wallet.cri.credential.mobile_driving_licence.cbor;

import com.fasterxml.jackson.core.JsonGenerator;
import com.fasterxml.jackson.databind.JsonSerializer;
import com.fasterxml.jackson.databind.SerializerProvider;
import com.fasterxml.jackson.dataformat.cbor.CBORFactory;
import com.fasterxml.jackson.dataformat.cbor.CBORGenerator;
import uk.gov.di.mobile.wallet.cri.credential.mobile_driving_licence.mdoc.MobileSecurityObject;

import java.io.ByteArrayOutputStream;
import java.io.IOException;

public class MobileSecurityObjectSerializer extends JsonSerializer<MobileSecurityObject> {
    @Override
    public void serialize(
            final MobileSecurityObject mobileSecurityObject,
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

            innerGenerator.writeStartObject(7);
            innerGenerator.writeStringField("version", mobileSecurityObject.version());
            innerGenerator.writeStringField(
                    "digestAlgorithm", mobileSecurityObject.digestAlgorithm());
            innerGenerator.writeFieldName("valueDigests");
            innerGenerator.writeObject(mobileSecurityObject.valueDigests());
            innerGenerator.writeFieldName("deviceKeyInfo");
            innerGenerator.writeObject(mobileSecurityObject.deviceKeyInfo());
            innerGenerator.writeFieldName("docType");
            innerGenerator.writeString(mobileSecurityObject.docType());
            innerGenerator.writeFieldName("validityInfo");
            innerGenerator.writeObject(mobileSecurityObject.validityInfo());
            innerGenerator.writeFieldName("status");
            innerGenerator.writeObject(mobileSecurityObject.status());
            innerGenerator.writeEndObject();
        }
        cborGenerator.writeTag(24);
        cborGenerator.writeBinary(baos.toByteArray());
    }
}
