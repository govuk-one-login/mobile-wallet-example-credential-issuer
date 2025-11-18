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
 * <p>Produces a definite-length CBOR map containing the unprotected header parameters used in
 * COSE_Sign1. The certificate chain is included under label 33 (x5chain) as defined in RFC 9360.
 *
 * <ul>
 *   <li>Map length: 1
 *   <li>Key 33 (x5chain) → DER-encoded certificate chain as a byte string
 * </ul>
 */
public class COSEUnprotectedHeaderSerializer extends StdSerializer<COSEUnprotectedHeader> {
    public COSEUnprotectedHeaderSerializer() {
        super(COSEUnprotectedHeader.class);
    }

    /**
     * Serializes the unprotected header as a definite-length CBOR map ({@code 1} entry), using
     * integer label {@code 33 → x5chain}.
     *
     * @throws IllegalArgumentException if the provided generator is not a {@link CBORGenerator}
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
        cborGenerator.writeFieldId(33); // from RFC9360 2
        cborGenerator.writeBinary(value.getX5chain());
        cborGenerator.writeEndObject();
    }
}
