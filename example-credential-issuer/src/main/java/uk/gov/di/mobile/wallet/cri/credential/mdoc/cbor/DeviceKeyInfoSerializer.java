package uk.gov.di.mobile.wallet.cri.credential.mdoc.cbor;

import com.fasterxml.jackson.core.JsonGenerator;
import com.fasterxml.jackson.databind.SerializerProvider;
import com.fasterxml.jackson.databind.ser.std.StdSerializer;
import com.fasterxml.jackson.dataformat.cbor.CBORGenerator;
import uk.gov.di.mobile.wallet.cri.credential.mdoc.DeviceKeyInfo;

import java.io.IOException;

/**
 * CBOR serializer for {@link DeviceKeyInfo}.
 *
 * <p>Serializes a {@link DeviceKeyInfo} object as a definite-length CBOR map with a single entry:
 *
 * <ul>
 *   <li>{@code "deviceKey"}: definite-length CBOR map with four entries:
 *       <ul>
 *         <li>{@code 1}: integer, key type (kty)
 *         <li>{@code -1}: integer, curve identifier (crv)
 *         <li>{@code -2}: byte string, x-coordinate (x)
 *         <li>{@code -3}: byte string, y-coordinate (y)
 *       </ul>
 * </ul>
 *
 * <p>The integer labels in the {@code "deviceKey"} map are defined by RFC 8152 §13.1.
 */
public class DeviceKeyInfoSerializer extends StdSerializer<DeviceKeyInfo> {
    public DeviceKeyInfoSerializer() {
        super(DeviceKeyInfo.class);
    }

    /**
     * Serializes a {@link DeviceKeyInfo} object as a definite-length CBOR map.
     *
     * @param value the {@link DeviceKeyInfo} object to serialize
     * @param generator the {@link CBORGenerator} used to write CBOR-formatted output
     * @param serializer the {@link SerializerProvider} used to find other serializers
     * @throws IllegalArgumentException if the generator is not a {@link CBORGenerator}
     * @throws IOException on write errors
     */
    @Override
    public void serialize(
            final DeviceKeyInfo value,
            final JsonGenerator generator,
            final SerializerProvider serializer)
            throws IOException {

        if (!(generator instanceof CBORGenerator cborGenerator)) {
            throw new IllegalArgumentException("Requires CBORGenerator");
        }

        cborGenerator.writeStartObject(1);
        cborGenerator.writeFieldName("deviceKey");
        cborGenerator.writeStartObject(4);
        cborGenerator.writeFieldId(1);
        cborGenerator.writeNumber(value.deviceKey().keyType());
        cborGenerator.writeFieldId(-1);
        cborGenerator.writeNumber(value.deviceKey().curve());
        cborGenerator.writeFieldId(-2);
        cborGenerator.writeBinary(value.deviceKey().x());
        cborGenerator.writeFieldId(-3);
        cborGenerator.writeBinary(value.deviceKey().y());
        cborGenerator.writeEndObject();
        cborGenerator.writeEndObject();
    }
}
