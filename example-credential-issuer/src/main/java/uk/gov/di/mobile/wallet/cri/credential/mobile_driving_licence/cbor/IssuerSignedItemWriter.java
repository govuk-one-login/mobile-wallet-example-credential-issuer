package uk.gov.di.mobile.wallet.cri.credential.mobile_driving_licence.cbor;

import com.fasterxml.jackson.dataformat.cbor.CBORGenerator;
import uk.gov.di.mobile.wallet.cri.annotations.ExcludeFromGeneratedCoverageReport;
import uk.gov.di.mobile.wallet.cri.credential.mobile_driving_licence.mdoc.IssuerSignedItem;

import java.io.IOException;

class IssuerSignedItemWriter {

    @ExcludeFromGeneratedCoverageReport
    private IssuerSignedItemWriter() {
        throw new IllegalStateException("Instantiation is not valid for this class.");
    }

    static void write(final CBORGenerator generator, final IssuerSignedItem value)
            throws IOException {
        generator.writeStartObject(4);
        generator.writeNumberField("digestID", value.digestId());
        generator.writeBinaryField("random", value.random());
        generator.writeStringField("elementIdentifier", value.elementIdentifier());
        generator.writeFieldName("elementValue");
        generator.writeObject(value.elementValue());
        generator.writeEndObject();
    }
}
