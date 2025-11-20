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
 * <p>Encodes a map of namespaces to element digests using definite-length CBOR maps:
 *
 * <ul>
 *   <li>Outer: namespaces → definite-length map where keys are namespace strings.
 *   <li>Inner: for each namespace, a definite-length map of element identifiers to digest bytes.
 *       Element identifiers are encoded as CBOR integer keys using {@code writeFieldId(...)}.
 * </ul>
 */
public class ValueDigestsSerializer extends StdSerializer<ValueDigests> {
    public ValueDigestsSerializer() {
        super(ValueDigests.class);
    }

    /**
     * Serializes {@link ValueDigests} as nested, definite-length CBOR maps.
     *
     * @throws IllegalArgumentException if the provided generator is not a {@link CBORGenerator}
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
