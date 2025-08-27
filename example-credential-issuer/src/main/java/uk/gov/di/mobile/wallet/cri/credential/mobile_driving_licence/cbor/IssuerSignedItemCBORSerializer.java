package uk.gov.di.mobile.wallet.cri.credential.mobile_driving_licence.cbor;

import com.fasterxml.jackson.core.JsonGenerator;
import com.fasterxml.jackson.databind.JsonSerializer;
import com.fasterxml.jackson.databind.SerializerProvider;
import com.fasterxml.jackson.dataformat.cbor.CBORGenerator;
import uk.gov.di.mobile.wallet.cri.credential.mobile_driving_licence.mdoc.IssuerSignedItem;

import java.io.IOException;

/** Custom Jackson serializer for CBOR encoding encoded CBOR data items. */
public class IssuerSignedItemCBORSerializer extends JsonSerializer<IssuerSignedItem> {
    @Override
    public void serialize(
            final IssuerSignedItem issuerSignedItem,
            final JsonGenerator generator,
            final SerializerProvider serializer)
            throws IOException {
        if (!(generator instanceof CBORGenerator cborGenerator)) {
            throw new IllegalArgumentException("Requires CBORGenerator");
        }

        byte[] encodedBytes =
                IssuerSignedItemEncoder.encode(issuerSignedItem, generator.getCodec());
        // '24' is a tag that represents encoded CBOR data items. It's used when
        // embedding CBOR data within CBOR.
        cborGenerator.writeTag(24);
        cborGenerator.writeBinary(encodedBytes);
    }
}
