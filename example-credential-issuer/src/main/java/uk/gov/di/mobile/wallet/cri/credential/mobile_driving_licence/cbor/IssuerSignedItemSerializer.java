package uk.gov.di.mobile.wallet.cri.credential.mobile_driving_licence.cbor;

import com.fasterxml.jackson.core.JsonGenerator;
import com.fasterxml.jackson.databind.SerializerProvider;
import com.fasterxml.jackson.databind.ser.std.StdSerializer;
import com.fasterxml.jackson.dataformat.cbor.CBORGenerator;
import uk.gov.di.mobile.wallet.cri.credential.mobile_driving_licence.mdoc.IssuerSignedItem;

import java.io.IOException;

/**
 * Custom Jackson serializer for {@link IssuerSignedItem} to CBOR format.
 *
 * <p>This serializer encodes the {@link IssuerSignedItem} into a CBOR byte array, then writes it as
 * a tagged CBOR binary with tag 24. Tag 24 indicates that the following byte string contains a
 * fully encoded embedded CBOR data item.
 */
public class IssuerSignedItemSerializer extends StdSerializer<IssuerSignedItem<?>> {
    public IssuerSignedItemSerializer() {
        super((Class<IssuerSignedItem<?>>) (Class<?>) IssuerSignedItem.class);
    }

    @Override
    public void serialize(
            final IssuerSignedItem<?> issuerSignedItem,
            final JsonGenerator generator,
            final SerializerProvider serializer)
            throws IOException {
        if (!(generator instanceof CBORGenerator cborGenerator)) {
            throw new IllegalArgumentException("Requires CBORGenerator");
        }

        byte[] encodedBytes =
                IssuerSignedItemEncoder.encode(issuerSignedItem, generator.getCodec());

        cborGenerator.writeTag(24);
        cborGenerator.writeBinary(encodedBytes);
    }
}
