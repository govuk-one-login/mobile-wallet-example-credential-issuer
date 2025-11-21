package uk.gov.di.mobile.wallet.cri.credential.mobile_driving_licence.cbor;

import com.fasterxml.jackson.dataformat.cbor.CBORGenerator;
import uk.gov.di.mobile.wallet.cri.annotations.ExcludeFromGeneratedCoverageReport;
import uk.gov.di.mobile.wallet.cri.credential.mobile_driving_licence.mdoc.MobileSecurityObject;

import java.io.IOException;

class MobileSecurityObjectWriter {

    @ExcludeFromGeneratedCoverageReport
    private MobileSecurityObjectWriter() {
        throw new IllegalStateException("Instantiation is not valid for this class.");
    }

    static void write(final CBORGenerator generator, final MobileSecurityObject value)
            throws IOException {
        generator.writeStartObject(7);
        generator.writeStringField("version", value.version());
        generator.writeStringField("digestAlgorithm", value.digestAlgorithm());
        generator.writeFieldName("valueDigests");
        generator.writeObject(value.valueDigests());
        generator.writeFieldName("deviceKeyInfo");
        generator.writeObject(value.deviceKeyInfo());
        generator.writeFieldName("docType");
        generator.writeString(value.docType());
        generator.writeFieldName("validityInfo");
        generator.writeObject(value.validityInfo());
        generator.writeFieldName("status");
        generator.writeObject(value.status());
        generator.writeEndObject();
    }
}
