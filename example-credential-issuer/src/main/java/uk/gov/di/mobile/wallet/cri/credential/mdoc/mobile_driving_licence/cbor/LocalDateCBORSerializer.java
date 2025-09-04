package uk.gov.di.mobile.wallet.cri.credential.mdoc.mobile_driving_licence.cbor;

import com.fasterxml.jackson.core.JsonGenerator;
import com.fasterxml.jackson.databind.JsonSerializer;
import com.fasterxml.jackson.databind.SerializerProvider;
import com.fasterxml.jackson.dataformat.cbor.CBORGenerator;

import java.io.IOException;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;

/**
 * Custom Jackson serializer for {@link LocalDate} to CBOR format.
 *
 * <p>Serializes the {@link LocalDate} as a text string formatted according to ISO-8601, tagged with
 * CBOR tag 1004 to indicate a date without a time.
 *
 * <p>Tag 1004 indicates that the tagged string represents a calendar date (YYYY-MM-DD) without time
 * or timezone information.
 */
public class LocalDateCBORSerializer extends JsonSerializer<LocalDate> {
    @Override
    public void serialize(
            final LocalDate localDate,
            final JsonGenerator generator,
            final SerializerProvider serializers)
            throws IOException {
        if (!(generator instanceof CBORGenerator cborGenerator)) {
            throw new IllegalArgumentException("Requires CBORGenerator");
        }

        String dateString = localDate.format(DateTimeFormatter.ISO_LOCAL_DATE);
        cborGenerator.writeTag(1004);
        generator.writeString(dateString);
    }
}
