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
 * <p>Serializes a {@link COSEProtectedHeader} object as a definite-length CBOR map with one entry:
 *
 * <ul>
 *   <li>{@code 1}: integer, algorithm identifier
 * </ul>
 *
 * <p>Key {@code 1}corresponds to the {@code alg} (algorithm) header parameter as defined in <a
 * href="https://datatracker.ietf.org/doc/html/rfc9052#section-3.1">RFC 9052: CBOR Object Signing
 * and Encryption (COSE) - Section 3.1 Header Parameters</a>. The integer value identifies the
 * cryptographic algorithm used for signing the credential.
 *
 * <p>Usage note: In a COSE_Sign1 structure, this map is CBOR-encoded to bytes and embedded as the
 * first array element. This serializer emits only the map; the encoding to bytes and insertion into
 * the COSE_Sign1 structure are handled elsewhere.
 */
public class COSEProtectedHeaderSerializer extends StdSerializer<COSEProtectedHeader> {
    public COSEProtectedHeaderSerializer() {
        super(COSEProtectedHeader.class);
    }

    /**
     * Serializes a {@link COSEProtectedHeader} object as a definite-length CBOR map.
     *
     * @param value the {@link COSEProtectedHeader} object to serialize
     * @param generator the {@link CBORGenerator} used to write CBOR-formatted output
     * @param serializer the {@link SerializerProvider} used to find other serializers
     * @throws IllegalArgumentException if the generator is not a {@link CBORGenerator}
     * @throws IOException on write errors
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
        int algLabel = 1;
        cborGenerator.writeFieldId(algLabel);
        cborGenerator.writeNumber(value.alg());
        cborGenerator.writeEndObject();
    }
}
