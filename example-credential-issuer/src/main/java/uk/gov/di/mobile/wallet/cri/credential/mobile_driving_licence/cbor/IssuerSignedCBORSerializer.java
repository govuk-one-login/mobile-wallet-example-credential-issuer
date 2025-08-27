package uk.gov.di.mobile.wallet.cri.credential.mobile_driving_licence.cbor;

import com.fasterxml.jackson.core.JsonGenerator;
import com.fasterxml.jackson.databind.JsonSerializer;
import com.fasterxml.jackson.databind.SerializerProvider;
import com.fasterxml.jackson.dataformat.cbor.CBORGenerator;
import uk.gov.di.mobile.wallet.cri.credential.mobile_driving_licence.cose.COSESign1;
import uk.gov.di.mobile.wallet.cri.credential.mobile_driving_licence.mdoc.IssuerSigned;
import uk.gov.di.mobile.wallet.cri.credential.mobile_driving_licence.mdoc.IssuerSignedItem;

import java.io.IOException;
import java.util.List;
import java.util.Map;

/**
 * A custom serializer for the {@link IssuerSigned} object to CBOR format. This serializer handles
 * the serialization of nested byte arrays within the 'nameSpaces' map, tagging them with the CBOR
 * tag 24 to indicate embedded CBOR data items.
 */
public class IssuerSignedCBORSerializer extends JsonSerializer<IssuerSigned> {

    /**
     * Serializes the {@link IssuerSigned} object to CBOR.
     *
     * @param issuerSigned The {@link IssuerSigned} object to serialize.
     * @param generator The {@link JsonGenerator} (must be a {@link CBORGenerator}).
     * @param serializer The {@link SerializerProvider}.
     * @throws IOException If an I/O error occurs during serialization.
     * @throws IllegalArgumentException If the provided generator is not a {@link CBORGenerator}.
     */
    @Override
    public void serialize(
            final IssuerSigned issuerSigned,
            final JsonGenerator generator,
            final SerializerProvider serializer)
            throws IOException {
        if (!(generator instanceof CBORGenerator cborGenerator)) {
            throw new IllegalArgumentException("Requires CBORGenerator");
        }

        cborGenerator.writeStartObject();

        cborGenerator.writeFieldName("nameSpaces");
        cborGenerator.writeStartObject();
        for (Map.Entry<String, List<IssuerSignedItem>> entry :
                issuerSigned.nameSpaces().entrySet()) {
            cborGenerator.writeFieldName(entry.getKey());
            cborGenerator.writeStartArray();

            for (IssuerSignedItem issuerSignedItem : entry.getValue()) {
                byte[] encodedBytes =
                        IssuerSignedItemEncoder.encode(issuerSignedItem, generator.getCodec());
                // '24' is a tag that represents encoded CBOR data items. It's used when
                // embedding CBOR data within CBOR.
                cborGenerator.writeTag(24);
                cborGenerator.writeBinary(encodedBytes);
            }
            cborGenerator.writeEndArray();
        }
        cborGenerator.writeEndObject();

        COSESign1 issuerAuth = issuerSigned.issuerAuth();
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
