package uk.gov.di.mobile.wallet.cri.credential.mobile_driving_licence.cbor;

import com.fasterxml.jackson.core.JsonGenerator;
import com.fasterxml.jackson.databind.SerializerProvider;
import com.fasterxml.jackson.databind.ser.std.StdSerializer;
import com.fasterxml.jackson.dataformat.cbor.CBORGenerator;
import uk.gov.di.mobile.wallet.cri.credential.mobile_driving_licence.Code;
import uk.gov.di.mobile.wallet.cri.credential.mobile_driving_licence.DrivingPrivilege;

import java.io.IOException;
import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

/**
 * Custom CBOR serializer for {@link DrivingPrivilege} that enforces definite-length maps and
 * arrays.
 */
public class DrivingPrivilegeSerializer extends StdSerializer<DrivingPrivilege> {

    public DrivingPrivilegeSerializer() {
        super(DrivingPrivilege.class);
    }

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
