package uk.gov.di.mobile.wallet.cri.credential.mobile_driving_licence.cbor;

import com.fasterxml.jackson.core.JsonGenerator;
import com.fasterxml.jackson.databind.SerializerProvider;
import com.fasterxml.jackson.databind.ser.std.StdSerializer;
import com.fasterxml.jackson.dataformat.cbor.CBORGenerator;
import uk.gov.di.mobile.wallet.cri.credential.mobile_driving_licence.mdoc.ValueDigests;

import java.io.IOException;
import java.util.Map;
import java.util.TreeMap;

/**
 * CBOR serializer for {@link ValueDigests}.
 *
 * <p>Serializes a {@link ValueDigests} object as a nested, definite-length CBOR map representing a
 * mapping of namespaces to element digest values.
 *
 * <ul>
 *   <li><strong>Outer map:</strong> text keys (namespaces), each mapped to an inner map
 *   <li><strong>Inner map:</strong> integer keys (element identifiers) to digest byte arrays
 * </ul>
 */
public class ValueDigestsSerializer extends StdSerializer<ValueDigests> {
    public ValueDigestsSerializer() {
        super(ValueDigests.class);
    }

    /**
     * Serializes a {@link ValueDigests} object as a definite-length CBOR map.
     *
     * @param value the {@link ValueDigests} object to serialize
     * @param generator the {@link CBORGenerator} used to write CBOR-formatted output
     * @param serializer the {@link SerializerProvider} used to find other serializers
     * @throws IllegalArgumentException if the generator is not a {@link CBORGenerator}
     * @throws IOException on write errors
     */
    @Override
    public void serialize(
            ValueDigests value, JsonGenerator generator, SerializerProvider serializer)
            throws IOException {
        if (!(generator instanceof CBORGenerator cborGenerator)) {
            throw new IllegalArgumentException("Requires CBORGenerator");
        }

        Map<String, Map<Integer, byte[]>> namespaces = value.valueDigests();
        cborGenerator.writeStartObject(namespaces.size());
        for (var namespace : namespaces.entrySet()) {
            cborGenerator.writeFieldName(namespace.getKey());
            Map<Integer, byte[]> elements = namespace.getValue();
            var sortedElements = new TreeMap<>(elements);
            cborGenerator.writeStartObject(sortedElements.size());
            for (var element : sortedElements.entrySet()) {
                cborGenerator.writeFieldId(element.getKey());
                cborGenerator.writeBinary(element.getValue());
            }
            cborGenerator.writeEndObject();
        }
        cborGenerator.writeEndObject();
    }
}
