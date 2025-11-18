package uk.gov.di.mobile.wallet.cri.credential.mobile_driving_licence.cbor;

import com.fasterxml.jackson.core.JsonGenerator;
import com.fasterxml.jackson.databind.SerializerProvider;
import com.fasterxml.jackson.databind.ser.std.StdSerializer;
import com.fasterxml.jackson.dataformat.cbor.CBORGenerator;
import uk.gov.di.mobile.wallet.cri.credential.mobile_driving_licence.mdoc.Status;

import java.io.IOException;

/**
 * CBOR serializer for {@link Status}.
 *
 * <p>Produces a definite-length map with a single entry <b>status_list</b>, which itself is a
 * definite-length map containing:
 *
 * <ul>
 *   <li><b>idx</b> → numeric index in the status list
 *   <li><b>uri</b> → URI string of the status list
 * </ul>
 */
public class StatusSerializer extends StdSerializer<Status> {
    public StatusSerializer() {
        super(Status.class);
    }

    /**
     * Serializes {@link Status} as a nested, definite-length CBOR map.
     *
     * @throws IllegalArgumentException if the provided generator is not a {@link CBORGenerator}
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
