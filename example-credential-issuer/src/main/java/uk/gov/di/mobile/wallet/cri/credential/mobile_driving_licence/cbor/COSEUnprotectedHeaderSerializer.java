package uk.gov.di.mobile.wallet.cri.credential.mobile_driving_licence.cbor;

import com.fasterxml.jackson.core.JsonGenerator;
import com.fasterxml.jackson.databind.SerializerProvider;
import com.fasterxml.jackson.databind.ser.std.StdSerializer;
import com.fasterxml.jackson.dataformat.cbor.CBORGenerator;
import uk.gov.di.mobile.wallet.cri.credential.mobile_driving_licence.cose.COSEUnprotectedHeader;

import java.io.IOException;

/**
 * CBOR serializer for {@link COSEUnprotectedHeader}.
 *
 * <p>Serializes a {@link COSEUnprotectedHeader} object as a definite-length CBOR map with one
 * entry:
 *
 * <ul>
 *   <li>{@code 33}: byte string, certificate chain as DER-encoded bytes (per RFC 9360)
 * </ul>
 */
public class COSEUnprotectedHeaderSerializer extends StdSerializer<COSEUnprotectedHeader> {
    public COSEUnprotectedHeaderSerializer() {
        super(COSEUnprotectedHeader.class);
    }

    /**
     * Serializes a {@link COSEUnprotectedHeader} object as a definite-length CBOR map.
     *
     * @param value the {@link COSEUnprotectedHeader} object to serialize
     * @param generator the {@link CBORGenerator} used to write CBOR-formatted output
     * @param serializer the {@link SerializerProvider} used to find other serializers
     * @throws IllegalArgumentException if the generator is not a {@link CBORGenerator}
     * @throws IOException on write errors
     */
    @Override
    public void serialize(
            final COSEUnprotectedHeader value,
            final JsonGenerator generator,
            final SerializerProvider serializer)
            throws IOException {

        if (!(generator instanceof CBORGenerator cborGenerator)) {
            throw new IllegalArgumentException("Requires CBORGenerator");
        }

        cborGenerator.writeStartObject(1);
        cborGenerator.writeFieldId(33);
        cborGenerator.writeBinary(value.getX5chain());
        cborGenerator.writeEndObject();
    }
}
