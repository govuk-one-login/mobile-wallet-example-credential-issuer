package uk.gov.di.mobile.wallet.cri.credential.mobile_driving_licence.cbor;

import com.fasterxml.jackson.core.JsonGenerator;
import com.fasterxml.jackson.databind.SerializerProvider;
import com.fasterxml.jackson.databind.ser.std.StdSerializer;
import com.fasterxml.jackson.dataformat.cbor.CBORGenerator;

import java.io.IOException;
import java.time.Instant;
import java.time.temporal.ChronoUnit;

/**
 * Custom Jackson serializer for {@link Instant} to CBOR format.
 *
 * <p>Serializes the {@link Instant} as a timestamp string truncated to seconds, tagged with CBOR
 * tag 0 to indicate a standard date-time string.
 *
 * <p>Tag 0 signifies the string follows the date-time format: "YYYY-MM-DDThh:mm:ssZ".
 *
 * @throws IllegalArgumentException if the generator is not a {@link CBORGenerator}.
 */
public class InstantSerializer extends StdSerializer<Instant> {
    public InstantSerializer() {
        super(Instant.class);
    }

    @Override
    public void serialize(
            final Instant value,
            final JsonGenerator generator,
            final SerializerProvider serializers)
            throws IOException {
        if (!(generator instanceof CBORGenerator cborGenerator)) {
            throw new IllegalArgumentException("Requires CBORGenerator");
        }

        String formatted =
                value.truncatedTo(ChronoUnit.SECONDS).toString(); // "2026-06-24T16:05:21Z"
        cborGenerator.writeTag(0);
        generator.writeString(formatted);
    }
}
