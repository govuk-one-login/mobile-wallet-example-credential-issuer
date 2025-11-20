package uk.gov.di.mobile.wallet.cri.credential.mobile_driving_licence.cbor;

import com.fasterxml.jackson.dataformat.cbor.CBORGenerator;
import uk.gov.di.mobile.wallet.cri.credential.mobile_driving_licence.mdoc.IssuerSignedItem;

import java.io.IOException;

class IssuerSignedItemInnerWriter {

    static void write(final CBORGenerator generator, final IssuerSignedItem item)
            throws IOException {
        generator.writeStartObject(4);
        generator.writeNumberField("digestID", item.digestId());
        generator.writeBinaryField("random", item.random());
        generator.writeStringField("elementIdentifier", item.elementIdentifier());
        generator.writeFieldName("elementValue");
        generator.writeObject(item.elementValue());
        generator.writeEndObject();
    }
}
