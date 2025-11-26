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
 *   <li>{@code 33}: byte string, certificate chain as DER-encoded bytes
 * </ul>
 *
 * <p>Key {@code 33} corresponds to the {@code x5chain} header parameter as defined in <a
 * href="https://datatracker.ietf.org/doc/html/rfc9360">RFC 9360: CBOR Object Signing and Encryption
 * (COSE): Header Parameters for Carrying and Referencing X.509 Certificates</a>. The {@code
 * x5chain} parameter is used to carry a chain of X.509 certificates, where each certificate is
 * DER-encoded. The first certificate in the chain is the end-entity certificate, followed by the
 * certificate that signed it, and so on.
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
        cborGenerator.writeBinary(value.x5chain());
        cborGenerator.writeEndObject();
    }
}
