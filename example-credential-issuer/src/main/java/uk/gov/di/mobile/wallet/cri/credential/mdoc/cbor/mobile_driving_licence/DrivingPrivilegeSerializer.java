package uk.gov.di.mobile.wallet.cri.credential.mdoc.cbor.mobile_driving_licence;

import com.fasterxml.jackson.core.JsonGenerator;
import com.fasterxml.jackson.databind.SerializerProvider;
import com.fasterxml.jackson.databind.ser.std.StdSerializer;
import com.fasterxml.jackson.dataformat.cbor.CBORGenerator;
import uk.gov.di.mobile.wallet.cri.credential.mdoc.cbor.LocalDateCBORSerializer;
import uk.gov.di.mobile.wallet.cri.credential.mdoc.mobile_driving_licence.Code;
import uk.gov.di.mobile.wallet.cri.credential.mdoc.mobile_driving_licence.DrivingPrivilege;

import java.io.IOException;
import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

/**
 * CBOR serializer for {@link DrivingPrivilege}.
 *
 * <p>Serializes a {@link DrivingPrivilege} object as a definite-length CBOR map containing between
 * one and four entries:
 *
 * <ul>
 *   <li>{@code vehicle_category_code}: serialized as a string
 *   <li>{@code issue_date}: serialized as a string via the configured {@link
 *       LocalDateCBORSerializer}, included only when non-empty
 *   <li>{@code expiry_date}: serialized as a string via the configured {@link
 *       LocalDateCBORSerializer}, included only when non-empty
 *   <li>{@code codes}: definite-length CBOR array containing one or more definite-length CBOR maps
 *       containing one entry, included only when non-empty:
 *       <ul>
 *         <li>{@code code}: string, representing the restriction code
 *       </ul>
 * </ul>
 */
public class DrivingPrivilegeSerializer extends StdSerializer<DrivingPrivilege> {

    public DrivingPrivilegeSerializer() {
        super(DrivingPrivilege.class);
    }

    /**
     * Serializes a {@link DrivingPrivilege} object as a definite-length CBOR map.
     *
     * @param value the {@link DrivingPrivilege} object to serialize
     * @param generator the {@link CBORGenerator} used to write CBOR-formatted output
     * @param serializer the {@link SerializerProvider} used to find other serializers
     * @throws IllegalArgumentException if the generator is not a {@link CBORGenerator}
     * @throws IOException on write errors
     */
    @Override
    public void serialize(
            DrivingPrivilege value, JsonGenerator generator, SerializerProvider serializer)
            throws IOException {
        if (!(generator instanceof CBORGenerator cborGenerator)) {
            throw new IllegalArgumentException("Requires CBORGenerator");
        }

        int fieldCount = 1;
        Optional<LocalDate> issue = value.getIssueDate();
        Optional<LocalDate> expiry = value.getExpiryDate();
        Optional<List<Code>> codes = value.getCodes();

        if (issue.isPresent()) fieldCount++;
        if (expiry.isPresent()) fieldCount++;
        if (codes.isPresent() && !codes.get().isEmpty()) fieldCount++;

        cborGenerator.writeStartObject(fieldCount);

        cborGenerator.writeStringField("vehicle_category_code", value.getVehicleCategoryCode());

        if (issue.isPresent()) {
            cborGenerator.writeFieldName("issue_date");
            generator.writeObject(issue.get());
        }

        if (expiry.isPresent()) {
            cborGenerator.writeFieldName("expiry_date");
            generator.writeObject(expiry.get());
        }

        if (codes.isPresent() && !codes.get().isEmpty()) {
            List<Code> list = codes.get();
            cborGenerator.writeFieldName("codes");
            cborGenerator.writeStartArray(list, list.size());
            for (Code code : list) {
                cborGenerator.writeStartObject(1);
                cborGenerator.writeFieldName("code");
                cborGenerator.writeString(code.code());
                cborGenerator.writeEndObject();
            }
            cborGenerator.writeEndArray();
        }

        cborGenerator.writeEndObject();
    }
}
