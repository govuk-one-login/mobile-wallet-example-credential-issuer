package uk.gov.di.mobile.wallet.cri.credential.mobile_driving_licence.cbor;

import com.fasterxml.jackson.core.ObjectCodec;
import com.fasterxml.jackson.dataformat.cbor.CBORFactory;
import com.fasterxml.jackson.dataformat.cbor.CBORGenerator;
import uk.gov.di.mobile.wallet.cri.credential.mobile_driving_licence.mdoc.IssuerSignedItem;

import java.io.ByteArrayOutputStream;
import java.io.IOException;

// Utility class to handle IssuerSignedItem encoding
public class IssuerSignedItemEncoder {

    /**
     * Encodes an IssuerSignedItem to CBOR bytes.
     *
     * @param issuerSignedItem The item to encode
     * @param codec The codec from the main generator (for consistent serialization)
     * @return The encoded CBOR bytes
     * @throws IOException If encoding fails
     */
    public static byte[] encode(IssuerSignedItem issuerSignedItem, ObjectCodec codec)
            throws IOException {
        CBORFactory factory = new CBORFactory();
        ByteArrayOutputStream baos = new ByteArrayOutputStream();

        try (CBORGenerator innerGenerator = factory.createGenerator(baos)) {
            innerGenerator.setCodec(codec);

            innerGenerator.writeStartObject(4);
            innerGenerator.writeNumberField("digestID", issuerSignedItem.digestId());
            innerGenerator.writeBinaryField("random", issuerSignedItem.random());
            innerGenerator.writeStringField(
                    "elementIdentifier", issuerSignedItem.elementIdentifier());
            innerGenerator.writeFieldName("elementValue");
            innerGenerator.writeObject(issuerSignedItem.elementValue());
            innerGenerator.writeEndObject();
        }

        return baos.toByteArray();
    }
}
