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
 * CBOR serializer for {@link IssuerSignedItem}.
 *
 * <p>Serializes an {@link IssuerSignedItem} object as an embedded CBOR data item (RFC 8949).
 *
 * <ul>
 *   <li>Encodes the item using the current codec and an inner CBOR generator.
 *   <li>Prefixes with CBOR tag 24 to mark the following byte string as embedded CBOR.
 *   <li>Writes the map's CBOR byte string after the tag.
 * </ul>
 */
public class IssuerSignedItemCBORSerializer extends StdSerializer<IssuerSignedItem> {
    public IssuerSignedItemCBORSerializer() {
        super(IssuerSignedItem.class);
    }

    /**
     * Serializes {@link IssuerSignedItem} as embedded CBOR (tag 24 + byte string).
     *
     * @param value the {@link IssuerSignedItem} object to serialize
     * @param generator the {@link CBORGenerator} used to write CBOR-formatted output
     * @param serializer the {@link SerializerProvider} used to find other serializers
     * @throws IllegalArgumentException if the generator is not a {@link CBORGenerator}
     * @throws IOException on write errors
     */
    @Override
    public void serialize(
            final IssuerSignedItem value,
            final JsonGenerator generator,
            final SerializerProvider serializer)
            throws IOException {
        if (!(generator instanceof CBORGenerator cborGenerator)) {
            throw new IllegalArgumentException("Requires CBORGenerator");
        }

        CBORFactory factory = new CBORFactory();
        ByteArrayOutputStream baos = new ByteArrayOutputStream();
        try (CBORGenerator innerGenerator = factory.createGenerator(baos)) {
            innerGenerator.setCodec(generator.getCodec());
            IssuerSignedItemWriter.write(innerGenerator, value);
        }
        cborGenerator.writeTag(24);
        cborGenerator.writeBinary(baos.toByteArray());
    }
}
