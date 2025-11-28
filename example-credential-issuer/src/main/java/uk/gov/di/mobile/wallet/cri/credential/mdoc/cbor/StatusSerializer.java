package uk.gov.di.mobile.wallet.cri.credential.mdoc.cbor;

import com.fasterxml.jackson.core.JsonGenerator;
import com.fasterxml.jackson.databind.SerializerProvider;
import com.fasterxml.jackson.databind.ser.std.StdSerializer;
import com.fasterxml.jackson.dataformat.cbor.CBORGenerator;
import uk.gov.di.mobile.wallet.cri.credential.mdoc.Status;

import java.io.IOException;

/**
 * CBOR serializer for {@link Status}.
 *
 * <p>Serializes a {@link Status} object as a definite-length CBOR map with one entry:
 *
 * <ul>
 *   <li>{@code "status_list"}: definite-length map with two entries:
 *       <ul>
 *         <li>{@code "idx"}: integer, representing the status list index
 *         <li>{@code "uri"}: string, representing the status list URI
 *       </ul>
 * </ul>
 */
public class StatusSerializer extends StdSerializer<Status> {
    public StatusSerializer() {
        super(Status.class);
    }

    /**
     * Serializes a {@link Status} object as a definite-length CBOR map.
     *
     * @param value the {@link Status} object to serialize
     * @param generator the {@link CBORGenerator} used to write CBOR-formatted output
     * @param serializer the {@link SerializerProvider} used to find other serializers
     * @throws IllegalArgumentException if the generator is not a {@link CBORGenerator}
     * @throws IOException on write errors
     */
    @Override
    public void serialize(
            final Status value, final JsonGenerator generator, final SerializerProvider serializer)
            throws IOException {

        if (!(generator instanceof CBORGenerator cborGenerator)) {
            throw new IllegalArgumentException("Requires CBORGenerator");
        }

        cborGenerator.writeStartObject(1);
        cborGenerator.writeFieldName("status_list");
        cborGenerator.writeStartObject(2);
        cborGenerator.writeFieldName("idx");
        cborGenerator.writeNumber(value.statusList().idx());
        cborGenerator.writeFieldName("uri");
        cborGenerator.writeString(value.statusList().uri());
        cborGenerator.writeEndObject();
        cborGenerator.writeEndObject();
    }
}
