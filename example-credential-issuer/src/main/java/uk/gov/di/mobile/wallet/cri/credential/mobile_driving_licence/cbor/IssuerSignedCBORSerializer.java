package uk.gov.di.mobile.wallet.cri.credential.mobile_driving_licence.cbor;

import com.fasterxml.jackson.core.JsonGenerator;
import com.fasterxml.jackson.databind.SerializerProvider;
import com.fasterxml.jackson.databind.ser.std.StdSerializer;
import com.fasterxml.jackson.dataformat.cbor.CBORGenerator;
import uk.gov.di.mobile.wallet.cri.credential.mobile_driving_licence.cose.COSESign1;
import uk.gov.di.mobile.wallet.cri.credential.mobile_driving_licence.mdoc.IssuerSigned;
import uk.gov.di.mobile.wallet.cri.credential.mobile_driving_licence.mdoc.IssuerSignedItem;

import java.io.IOException;
import java.util.List;
import java.util.Map;

/**
 * CBOR serializer for {@link IssuerSigned}.
 *
 * <p>Produces a map with two entries:
 *
 * <ul>
 *   <li><b>nameSpaces</b> → a map of namespace → array of IssuerSignedItem; each item is encoded as
 *       embedded CBOR using tag 24 followed by a byte string via the registered {@link
 *       IssuerSignedItemCBORSerializer}.
 *   <li><b>issuerAuth</b> → COSE_Sign1 structure represented as an array of 4 elements in order:
 *       <ol>
 *         <li>protected header (CBOR-encoded map as bstr)
 *         <li>unprotected header (map; definite-length via its own serializer)
 *         <li>payload (bstr)
 *         <li>signature (bstr, IEEE P-1363)
 *       </ol>
 * </ul>
 */
public class IssuerSignedCBORSerializer extends StdSerializer<IssuerSigned> {

    public IssuerSignedCBORSerializer() {
        super(IssuerSigned.class);
    }

    /**
     * Serializes {@link IssuerSigned} as a CBOR map with fields {@code nameSpaces} and {@code
     * issuerAuth}.
     *
     * @param value the object to serialize
     * @param generator must be a {@link CBORGenerator}
     * @param serializer the provider
     * @throws IOException on write errors
     * @throws IllegalArgumentException if the generator is not a {@link CBORGenerator}
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
        for (Map.Entry<String, List<IssuerSignedItem<?>>> entry : value.nameSpaces().entrySet()) {
            cborGenerator.writeFieldName(entry.getKey());
            cborGenerator.writeStartArray();

            for (IssuerSignedItem<?> issuerSignedItem : entry.getValue()) {
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
