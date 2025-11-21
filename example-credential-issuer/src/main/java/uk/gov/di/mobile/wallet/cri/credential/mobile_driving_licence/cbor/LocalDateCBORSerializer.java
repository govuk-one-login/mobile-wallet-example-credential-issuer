package uk.gov.di.mobile.wallet.cri.credential.mobile_driving_licence.cbor;

import com.fasterxml.jackson.core.JsonGenerator;
import com.fasterxml.jackson.databind.SerializerProvider;
import com.fasterxml.jackson.databind.ser.std.StdSerializer;
import com.fasterxml.jackson.dataformat.cbor.CBORGenerator;

import java.io.IOException;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;

/**
 * CBOR serializer for {@link LocalDate}.
 *
 * <p>Serializes a {@link LocalDate} object as a tagged CBOR text string.
 *
 * <ul>
 *   <li>{@code Tag 1004}: indicates a full-date string as defined in RFC 8943
 *   <li>The string value is formatted as {@code "YYYY-MM-DD"} according to ISO-8601
 * </ul>
 */
public class LocalDateCBORSerializer extends StdSerializer<LocalDate> {
    public LocalDateCBORSerializer() {
        super(LocalDate.class);
    }

    /**
     * Serializes an {@link LocalDate} object as a CBOR text string tagged with {@code Tag 1004}.
     *
     * @param value the {@link LocalDate} object to serialize
     * @param generator the {@link CBORGenerator} used to write CBOR-formatted output
     * @param serializer the {@link SerializerProvider} used to find other serializers
     * @throws IllegalArgumentException if the generator is not a {@link CBORGenerator}
     * @throws IOException on write errors
     */
    @Override
    public void serialize(
            final LocalDate value,
            final JsonGenerator generator,
            final SerializerProvider serializer)
            throws IOException {
        if (!(generator instanceof CBORGenerator cborGenerator)) {
            throw new IllegalArgumentException("Requires CBORGenerator");
        }

        String dateString = value.format(DateTimeFormatter.ISO_LOCAL_DATE);
        cborGenerator.writeTag(1004);
        generator.writeString(dateString);
    }
}
