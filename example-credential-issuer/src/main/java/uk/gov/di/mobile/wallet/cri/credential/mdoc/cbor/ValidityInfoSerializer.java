package uk.gov.di.mobile.wallet.cri.credential.mdoc.cbor;

import com.fasterxml.jackson.core.JsonGenerator;
import com.fasterxml.jackson.databind.SerializerProvider;
import com.fasterxml.jackson.databind.ser.std.StdSerializer;
import com.fasterxml.jackson.dataformat.cbor.CBORGenerator;
import uk.gov.di.mobile.wallet.cri.credential.mdoc.ValidityInfo;

import java.io.IOException;

/**
 * CBOR serializer for {@link ValidityInfo}.
 *
 * <p>Serializes a {@link ValidityInfo} object as a definite-length CBOR map with three entries:
 *
 * <ul>
 *   <li>{@code signed}: serialized as a string via the configured {@link InstantCBORSerializer}
 *   <li>{@code validFrom}: serialized as a string via the configured {@link InstantCBORSerializer}
 *   <li>{@code validUntil}: serialized as a string via the configured {@link InstantCBORSerializer}
 * </ul>
 */
public class ValidityInfoSerializer extends StdSerializer<ValidityInfo> {
    public ValidityInfoSerializer() {
        super(ValidityInfo.class);
    }

    /**
     * Serializes a {@link ValidityInfo} object as a definite-length CBOR map.
     *
     * @param value the {@link ValidityInfo} object to serialize
     * @param generator the {@link CBORGenerator} used to write CBOR-formatted output
     * @param serializer the {@link SerializerProvider} used to find other serializers
     * @throws IllegalArgumentException if the generator is not a {@link CBORGenerator}
     * @throws IOException on write errors
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
