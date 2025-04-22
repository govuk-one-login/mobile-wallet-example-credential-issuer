package uk.gov.di.mobile.wallet.cri.credential.mobile_driving_licence.cbor;

import com.fasterxml.jackson.core.JsonGenerator;
import com.fasterxml.jackson.databind.JsonSerializer;
import com.fasterxml.jackson.databind.SerializerProvider;
import com.fasterxml.jackson.dataformat.cbor.CBORGenerator;

import java.io.IOException;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;

/** Custom Jackson serializer for CBOR encoding {@link LocalDate} objects. */
public class LocalDateCBORSerializer extends JsonSerializer<LocalDate> {

    @Override
    public void serialize(
            final LocalDate localDate,
            final JsonGenerator generator,
            final SerializerProvider serializers)
            throws IOException {
        if (generator instanceof CBORGenerator cborGenerator) {
            String dateString = localDate.format(DateTimeFormatter.ISO_LOCAL_DATE);
            // '1004' is a tag indicating that the CBOR value should be interpreted as a date
            // value.
            cborGenerator.writeTag(1004);
            generator.writeString(dateString);
        } else {
            throw new IllegalArgumentException("This serializer only supports CBORGenerator");
        }
    }
}
