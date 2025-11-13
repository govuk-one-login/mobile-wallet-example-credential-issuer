package uk.gov.di.mobile.wallet.cri.credential.mobile_driving_licence.cbor;

import com.fasterxml.jackson.core.ObjectCodec;
import com.fasterxml.jackson.dataformat.cbor.CBORFactory;
import com.fasterxml.jackson.dataformat.cbor.CBORGenerator;
import uk.gov.di.mobile.wallet.cri.credential.mobile_driving_licence.mdoc.IssuerSignedItem;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Map;
import java.util.Objects;

public class IssuerSignedItemEncoder {

    private IssuerSignedItemEncoder() {
        // Can't be instantiated
    }

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
            if (Objects.equals(issuerSignedItem.elementIdentifier(), "driving_privileges")
                    || Objects.equals(
                            issuerSignedItem.elementIdentifier(),
                            "provisional_driving_privileges")) {
                ArrayList<Map<String, Object>> privileges =
                        (ArrayList<Map<String, Object>>) issuerSignedItem.elementValue();
                innerGenerator.writeStartArray(privileges.size());
                for (Map<String, Object> privilege : privileges) {
                    innerGenerator.writeStartObject(privilege.size());
                    for (Map.Entry<String, Object> entry : privilege.entrySet()) {
                        innerGenerator.writeFieldName(entry.getKey());
                        innerGenerator.writeObject(entry.getValue());
                    }
                    innerGenerator.writeEndObject();
                }
                innerGenerator.writeEndArray();
            } else {
                innerGenerator.writeObject(issuerSignedItem.elementValue());
            }
            innerGenerator.writeEndObject();
        }

        return baos.toByteArray();
    }
}
