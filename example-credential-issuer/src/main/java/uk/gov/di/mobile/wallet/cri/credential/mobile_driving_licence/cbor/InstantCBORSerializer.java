package uk.gov.di.mobile.wallet.cri.credential.mobile_driving_licence.cbor;

import com.fasterxml.jackson.core.JsonGenerator;
import com.fasterxml.jackson.databind.SerializerProvider;
import com.fasterxml.jackson.databind.ser.std.StdSerializer;
import com.fasterxml.jackson.dataformat.cbor.CBORGenerator;

import java.io.IOException;
import java.time.Instant;
import java.time.temporal.ChronoUnit;

/**
 * CBOR serializer for {@link Instant}.
 *
 * <p>Serializes an {@link Instant} object as a tagged CBOR text string.
 *
 * <ul>
 *   <li>{@code Tag 0}: indicates a date/time string as defined in RFC 8949
 *   <li>The string value is formatted as {@code "YYYY-MM-DDTHH:MM:SS"} according to ISO-8601
 * </ul>
 */
public class InstantCBORSerializer extends StdSerializer<Instant> {
    public InstantCBORSerializer() {
        super(Instant.class);
    }

    /**
     * Serializes an {@link Instant} object as a CBOR text string tagged with {@code Tag 0}.
     *
     * @param value the {@link Instant} object to serialize
     * @param generator the {@link CBORGenerator} used to write CBOR-formatted output
     * @param serializer the {@link SerializerProvider} used to find other serializers
     * @throws IllegalArgumentException if the generator is not a {@link CBORGenerator}
     * @throws IOException on write errors
     */
    @Override
    public void serialize(
            final Instant value, final JsonGenerator generator, final SerializerProvider serializer)
            throws IOException {
        if (!(generator instanceof CBORGenerator cborGenerator)) {
            throw new IllegalArgumentException("Requires CBORGenerator");
        }

        String formatted = value.truncatedTo(ChronoUnit.SECONDS).toString();
        cborGenerator.writeTag(0);
        generator.writeString(formatted);
    }
}
