package uk.gov.di.mobile.wallet.cri.credential.mdoc.cbor;

import com.fasterxml.jackson.dataformat.cbor.CBORGenerator;
import uk.gov.di.mobile.wallet.cri.annotations.ExcludeFromGeneratedCoverageReport;
import uk.gov.di.mobile.wallet.cri.credential.mdoc.MobileSecurityObject;

import java.io.IOException;

/**
 * Utility writer that encodes a {@link MobileSecurityObject} (MSO) to CBOR.
 *
 * <p>The object is encoded as a definite-length CBOR map with the following seven fields:
 *
 * <ul>
 *   <li><b>"version"</b> (text)
 *   <li><b>"digestAlgorithm"</b> (text)
 *   <li><b>"valueDigests"</b> — written via the configured {@link ValueDigestsSerializer}
 *   <li><b>"deviceKeyInfo"</b> — written via the configured {@link DeviceKeyInfoSerializer}
 *   <li><b>"docType"</b> (text)
 *   <li><b>"validityInfo"</b> — written via the configured {@link ValidityInfoSerializer}
 *   <li><b>"status"</b> — written via the configured {@link StatusSerializer}
 * </ul>
 */
class MobileSecurityObjectWriter {

    @ExcludeFromGeneratedCoverageReport
    private MobileSecurityObjectWriter() {
        throw new IllegalStateException("Instantiation is not valid for this class.");
    }

    /**
     * Writes the provided {@link MobileSecurityObject} as a 7-entry CBOR map to the given {@link
     * CBORGenerator}.
     *
     * @param generator the CBOR generator to write to; must be positioned to accept a value
     * @param value the mobile security object to encode
     * @throws IOException if writing to the underlying output fails
     */
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
