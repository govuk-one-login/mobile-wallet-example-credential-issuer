package uk.gov.di.mobile.wallet.cri.credential.mobile_driving_licence.cbor;

import com.fasterxml.jackson.core.JsonGenerator;
import com.fasterxml.jackson.databind.SerializerProvider;
import com.fasterxml.jackson.databind.ser.std.StdSerializer;
import com.fasterxml.jackson.dataformat.cbor.CBORFactory;
import com.fasterxml.jackson.dataformat.cbor.CBORGenerator;
import uk.gov.di.mobile.wallet.cri.credential.mobile_driving_licence.mdoc.IssuerSignedItem;

import java.io.ByteArrayOutputStream;
import java.io.IOException;

/**
 * Custom Jackson serializer for {@link IssuerSignedItem} to CBOR format.
 *
 * <p>This serializer encodes the {@link IssuerSignedItem} into a CBOR byte array using an inner
 * generator (definite-length map with 4 fields), then writes it as a tagged CBOR binary with tag
 * 24. Tag 24 indicates that the following byte string contains a fully encoded embedded CBOR data
 * item.
 */
public class IssuerSignedItemSerializer extends StdSerializer<IssuerSignedItem<?>> {
    public IssuerSignedItemSerializer() {
        super((Class<IssuerSignedItem<?>>) (Class<?>) IssuerSignedItem.class);
    }

    /**
     * Serializes an {@link IssuerSignedItem} as embedded CBOR: first encodes the item to CBOR bytes
     * using the current codec, then writes CBOR tag 24 followed by the byte string.
     *
     * @throws IllegalArgumentException if the provided generator is not a {@link CBORGenerator}
     */
    @Override
    public void serialize(
            final IssuerSignedItem<?> issuerSignedItem,
            final JsonGenerator generator,
            final SerializerProvider serializer)
            throws IOException {
        if (!(generator instanceof CBORGenerator cborGenerator)) {
            throw new IllegalArgumentException("Requires CBORGenerator");
        }

        // Encode the IssuerSignedItem into CBOR bytes using an inner generator to produce a
        // complete CBOR data item that will be embedded (tag 24 + bstr)
        CBORFactory factory = new CBORFactory();
        ByteArrayOutputStream baos = new ByteArrayOutputStream();
        try (CBORGenerator innerGenerator = factory.createGenerator(baos)) {
            innerGenerator.setCodec(generator.getCodec());

            innerGenerator.writeStartObject(4);
            innerGenerator.writeNumberField("digestID", issuerSignedItem.digestId());
            innerGenerator.writeBinaryField("random", issuerSignedItem.random());
            innerGenerator.writeStringField(
                    "elementIdentifier", issuerSignedItem.elementIdentifier());
            innerGenerator.writeFieldName("elementValue");
            innerGenerator.writeObject(issuerSignedItem.elementValue());
            innerGenerator.writeEndObject();
        }

        cborGenerator.writeTag(24);
        cborGenerator.writeBinary(baos.toByteArray());
    }
}
