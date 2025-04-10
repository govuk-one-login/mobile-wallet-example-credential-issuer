package uk.gov.di.mobile.wallet.cri.credential.mobile_driving_licence.cbor;

import com.fasterxml.jackson.core.JsonGenerator;
import com.fasterxml.jackson.databind.JsonSerializer;
import com.fasterxml.jackson.databind.SerializerProvider;
import com.fasterxml.jackson.dataformat.cbor.CBORGenerator;
import uk.gov.di.mobile.wallet.cri.credential.mobile_driving_licence.mdoc.IssuerSigned;
import uk.gov.di.mobile.wallet.cri.credential.mobile_driving_licence.mdoc.IssuerSignedItem;

import java.io.IOException;
import java.util.List;
import java.util.Map;

/** Custom Jackson serializer for CBOR encoding {@link IssuerSigned} objects. */
public class IssuerSignedCBORSerializer extends JsonSerializer<IssuerSigned> {

    @Override
    public void serialize(
            final IssuerSigned issuerSigned,
            final JsonGenerator generator,
            final SerializerProvider serializer)
            throws IOException {
        if (generator instanceof CBORGenerator cborGenerator) {
            cborGenerator.writeStartObject();
            cborGenerator.writeFieldName("nameSpaces");
            cborGenerator.writeStartObject();
            for (Map.Entry<String, List<IssuerSignedItem>> entry :
                    issuerSigned.nameSpaces().entrySet()) {
                cborGenerator.writeFieldName(entry.getKey());
                cborGenerator.writeStartArray();
                for (IssuerSignedItem item : entry.getValue()) {
                    // '24' is a tag that represents encoded CBOR data items. It's used when
                    // embedding CBOR data within CBOR.
                    cborGenerator.writeTag(24);
                    cborGenerator.writeObject(item);
                }
                cborGenerator.writeEndArray();
            }
            cborGenerator.writeEndObject();
            cborGenerator.writeEndObject();
        } else {
            throw new IllegalArgumentException("This serializer only supports CBORGenerator");
        }
    }
}
