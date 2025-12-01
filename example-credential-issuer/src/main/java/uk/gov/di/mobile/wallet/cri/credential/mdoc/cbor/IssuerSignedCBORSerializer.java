package uk.gov.di.mobile.wallet.cri.credential.mdoc.cbor;

import com.fasterxml.jackson.core.JsonGenerator;
import com.fasterxml.jackson.databind.SerializerProvider;
import com.fasterxml.jackson.databind.ser.std.StdSerializer;
import com.fasterxml.jackson.dataformat.cbor.CBORGenerator;
import uk.gov.di.mobile.wallet.cri.credential.mdoc.IssuerSigned;
import uk.gov.di.mobile.wallet.cri.credential.mdoc.IssuerSignedItem;
import uk.gov.di.mobile.wallet.cri.credential.mdoc.cose.COSESign1;

import java.io.IOException;
import java.util.List;
import java.util.Map;

/**
 * CBOR serializer for {@link IssuerSigned}.
 *
 * <p>Serializes an {@link IssuerSigned} object as a definite-length CBOR map with two entries:
 *
 * <ul>
 *   <li><b>nameSpaces</b>: a definite-length CBOR map from namespace strings to arrays of {@link
 *       IssuerSignedItem} objects serialized via the configured {@link IssuerSignedCBORSerializer}
 *   <li><b>issuerAuth</b>: a COSE_Sign1 structure represented as an array of four elements:
 *       <ol>
 *         <li>protected header, a byte string
 *         <li>unprotected header, a definite-length CBOR map serialized via the configured {@link
 *             COSEUnprotectedHeaderSerializer}
 *         <li>payload, a byte string
 *         <li>signature, a byte string
 *       </ol>
 * </ul>
 */
public class IssuerSignedCBORSerializer extends StdSerializer<IssuerSigned> {

    public IssuerSignedCBORSerializer() {
        super(IssuerSigned.class);
    }

    /**
     * Serializes a {@link IssuerSigned} object as a definite-length CBOR map.
     *
     * @param value the {@link IssuerSigned} object to serialize
     * @param generator the {@link CBORGenerator} used to write CBOR-formatted output
     * @param serializer the {@link SerializerProvider} used to find other serializers
     * @throws IllegalArgumentException if the generator is not a {@link CBORGenerator}
     * @throws IOException on write errors
     */
    @Override
    public void serialize(
            final IssuerSigned value,
            final JsonGenerator generator,
            final SerializerProvider serializer)
            throws IOException {
        if (!(generator instanceof CBORGenerator cborGenerator)) {
            throw new IllegalArgumentException("Requires CBORGenerator");
        }

        cborGenerator.writeStartObject();

        cborGenerator.writeFieldName("nameSpaces");
        cborGenerator.writeStartObject();
        for (Map.Entry<String, List<IssuerSignedItem>> entry : value.nameSpaces().entrySet()) {
            cborGenerator.writeFieldName(entry.getKey());
            cborGenerator.writeStartArray();

            for (IssuerSignedItem issuerSignedItem : entry.getValue()) {
                cborGenerator.writeObject(issuerSignedItem);
            }
            cborGenerator.writeEndArray();
        }
        cborGenerator.writeEndObject();

        COSESign1 issuerAuth = value.issuerAuth();
        cborGenerator.writeFieldName("issuerAuth");
        cborGenerator.writeStartArray();
        cborGenerator.writeBinary(issuerAuth.protectedHeader());
        cborGenerator.writeObject(issuerAuth.unprotectedHeader());
        cborGenerator.writeBinary(issuerAuth.payload());
        cborGenerator.writeBinary(issuerAuth.signature());
        cborGenerator.writeEndArray();

        cborGenerator.writeEndObject();
    }
}
