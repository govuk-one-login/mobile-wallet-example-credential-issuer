package uk.gov.di.mobile.wallet.cri.credential.mobile_driving_licence.cbor;

import com.fasterxml.jackson.core.JsonGenerator;
import com.fasterxml.jackson.databind.SerializerProvider;
import com.fasterxml.jackson.databind.ser.std.StdSerializer;
import com.fasterxml.jackson.dataformat.cbor.CBORGenerator;
import uk.gov.di.mobile.wallet.cri.credential.mobile_driving_licence.cose.COSEProtectedHeader;

import java.io.IOException;

/**
 * CBOR serializer for {@link COSEProtectedHeader}.
 *
 * <p>Produces a definite-length CBOR map with integer labels as required by COSE (RFC 9052):
 *
 * <ul>
 *   <li>Map length: 1
 *   <li>Key 1 (alg) → algorithm identifier (e.g., -7 for ES256)
 * </ul>
 *
 * <p>This serializer is used to create the protected header bytes that are embedded into COSE_Sign1
 * as a bstr and also used when building the Sig_structure to be signed.
 */
public class COSEProtectedHeaderSerializer extends StdSerializer<COSEProtectedHeader> {
    public COSEProtectedHeaderSerializer() {
        super(COSEProtectedHeader.class);
    }

    /**
     * Serializes the protected header as a definite-length CBOR map with label {@code 1 → alg}.
     *
     * @throws IllegalArgumentException if the provided generator is not a {@link CBORGenerator}
     */
    @Override
    public void serialize(
            final COSEProtectedHeader value,
            final JsonGenerator generator,
            final SerializerProvider serializer)
            throws IOException {

        if (!(generator instanceof CBORGenerator cborGenerator)) {
            throw new IllegalArgumentException("Requires CBORGenerator");
        }

        cborGenerator.writeStartObject(1);
        cborGenerator.writeFieldId(1); // from RFC9052 3.1
        cborGenerator.writeNumber(value.getAlg());
        cborGenerator.writeEndObject();
    }
}
