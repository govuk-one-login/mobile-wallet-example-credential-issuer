package uk.gov.di.mobile.wallet.cri.credential.mobile_driving_licence.cbor;

import com.fasterxml.jackson.core.JsonGenerator;
import com.fasterxml.jackson.databind.SerializerProvider;
import com.fasterxml.jackson.databind.ser.std.StdSerializer;
import com.fasterxml.jackson.dataformat.cbor.CBORGenerator;
import uk.gov.di.mobile.wallet.cri.credential.mobile_driving_licence.mdoc.ValidityInfo;

import java.io.IOException;

/**
 * CBOR serializer for {@link ValidityInfo}.
 *
 * <p>Encodes a definite-length map with three entries representing the validity window of the MSO:
 *
 * <ul>
 *   <li>signed → timestamp (serialized via configured {@link java.time.Instant} serializer)
 *   <li>validFrom → timestamp (serialized via configured {@link java.time.Instant} serializer)
 *   <li>validUntil → timestamp (serialized via configured {@link java.time.Instant} serializer)
 * </ul>
 */
public class ValidityInfoSerializer extends StdSerializer<ValidityInfo> {
    public ValidityInfoSerializer() {
        super(ValidityInfo.class);
    }

    /**
     * Serializes {@link ValidityInfo} as a definite-length CBOR map with exactly 3 fields.
     *
     * @throws IllegalArgumentException if the provided generator is not a {@link CBORGenerator}
     */
    @Override
    public void serialize(
            final ValidityInfo value,
            final JsonGenerator generator,
            final SerializerProvider serializer)
            throws IOException {

        if (!(generator instanceof CBORGenerator cborGenerator)) {
            throw new IllegalArgumentException("Requires CBORGenerator");
        }

        cborGenerator.writeStartObject(3);
        cborGenerator.writeFieldName("signed");
        cborGenerator.writeObject(value.signed());
        cborGenerator.writeFieldName("validFrom");
        cborGenerator.writeObject(value.validFrom());
        cborGenerator.writeFieldName("validUntil");
        cborGenerator.writeObject(value.validUntil());
        cborGenerator.writeEndObject();
    }
}
