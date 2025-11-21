package uk.gov.di.mobile.wallet.cri.credential.mobile_driving_licence.cbor;

import com.fasterxml.jackson.dataformat.cbor.CBORGenerator;
import uk.gov.di.mobile.wallet.cri.annotations.ExcludeFromGeneratedCoverageReport;
import uk.gov.di.mobile.wallet.cri.credential.mobile_driving_licence.mdoc.IssuerSignedItem;

import java.io.IOException;

/**
 * Utility writer that encodes an {@link IssuerSignedItem} object to CBOR.
 *
 * <p>The object is encoded as a definite-length CBOR map with the following four fields:
 *
 * <ul>
 *   <li><b>"digestID"</b> (unsigned int) – the digest ID associated with the data element
 *   <li><b>"random"</b> (byte string) – random value used as a salt in the digest calculation
 *   <li><b>"elementIdentifier"</b> (text) – the data element identifier
 *   <li><b>"elementValue"</b> – the element value; written via {@link
 *       CBORGenerator#writeObject(Object)} so any configured serializers for the nested type are
 *       applied
 * </ul>
 */
class IssuerSignedItemWriter {

    @ExcludeFromGeneratedCoverageReport
    private IssuerSignedItemWriter() {
        throw new IllegalStateException("Instantiation is not valid for this class.");
    }

    /**
     * Writes the provided {@link IssuerSignedItem} as a CBOR map to the supplied {@link
     * CBORGenerator}.
     *
     * @param generator the CBOR generator to write to; must be open for writing a value
     * @param value the issuer-signed item to encode
     * @throws IOException if writing to the underlying output fails
     */
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
