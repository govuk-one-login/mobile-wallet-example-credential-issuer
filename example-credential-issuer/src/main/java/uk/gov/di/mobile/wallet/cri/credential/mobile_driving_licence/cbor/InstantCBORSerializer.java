package uk.gov.di.mobile.wallet.cri.credential.mobile_driving_licence.cbor;

import com.fasterxml.jackson.core.JsonGenerator;
import com.fasterxml.jackson.databind.JsonSerializer;
import com.fasterxml.jackson.databind.SerializerProvider;
import com.fasterxml.jackson.dataformat.cbor.CBORGenerator;

import java.io.IOException;
import java.time.Instant;
import java.time.temporal.ChronoUnit;

public class InstantCBORSerializer extends JsonSerializer<Instant> {
    @Override
    public void serialize(
            final Instant instant,
            final JsonGenerator generator,
            final SerializerProvider serializers)
            throws IOException {
        if (!(generator instanceof CBORGenerator cborGenerator)) {
            throw new IllegalArgumentException("Requires CBORGenerator");
        }

        String formatted =
                instant.truncatedTo(ChronoUnit.SECONDS).toString(); // "2026-06-24T16:05:21Z"
        // '0' is a tag indicating that the CBOR value should be interpreted as a date-time
        cborGenerator.writeTag(0);
        generator.writeString(formatted);
    }
}
