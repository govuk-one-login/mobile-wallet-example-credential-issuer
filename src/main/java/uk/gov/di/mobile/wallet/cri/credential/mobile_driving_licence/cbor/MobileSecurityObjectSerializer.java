package uk.gov.di.mobile.wallet.cri.credential.mobile_driving_licence.cbor;

import com.fasterxml.jackson.core.JsonGenerator;
import com.fasterxml.jackson.databind.JsonSerializer;
import com.fasterxml.jackson.databind.SerializerProvider;
import com.fasterxml.jackson.databind.module.SimpleModule;
import com.fasterxml.jackson.dataformat.cbor.CBORGenerator;
import com.fasterxml.jackson.dataformat.cbor.databind.CBORMapper;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;
import uk.gov.di.mobile.wallet.cri.credential.mobile_driving_licence.mdoc.MobileSecurityObject;

import java.io.IOException;
import java.time.Instant;

public class MobileSecurityObjectSerializer extends JsonSerializer<MobileSecurityObject> {

    public void serialize(
            final MobileSecurityObject mobileSecurityObject,
            final JsonGenerator generator,
            final SerializerProvider serializer)
            throws IOException {
        if (generator instanceof CBORGenerator cborGenerator) {
            CBORMapper mapper = new CBORMapper();
            mapper.registerModule(new JavaTimeModule());
            SimpleModule simpleModule =
                    new SimpleModule().addSerializer(Instant.class, new InstantCBORSerializer());
            mapper.registerModule(simpleModule);

            byte[] mobileSecurityObjectBytes = mapper.writeValueAsBytes(mobileSecurityObject);

            cborGenerator.writeTag(24);
            cborGenerator.writeBinary(mobileSecurityObjectBytes);
        } else {
            throw new IllegalArgumentException("This serializer only supports CBORGenerator");
        }
    }
}
