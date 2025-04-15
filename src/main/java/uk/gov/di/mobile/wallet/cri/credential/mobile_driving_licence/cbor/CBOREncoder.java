package uk.gov.di.mobile.wallet.cri.credential.mobile_driving_licence.cbor;

import com.fasterxml.jackson.dataformat.cbor.databind.CBORMapper;

import java.io.IOException;

/**
 * Encodes Java objects into CBOR (Concise Binary Object Representation) format. This class utilises
 * a {@link CBORMapper} to perform the encoding.
 */
public class CBOREncoder {
    private final CBORMapper mapper;

    /**
     * Constructs a CBOREncoder with the provided CBORMapper.
     *
     * @param mapper The CBORMapper instance to use for encoding. This mapper has been
     *     pre-configured to handle specific data types.
     */
    public CBOREncoder(CBORMapper mapper) {
        this.mapper = mapper;
    }

    /**
     * Encodes the given object into a CBOR byte array.
     *
     * @param dataToEncode The Java object to encode into CBOR format.
     * @return A byte array containing the CBOR representation of the input object.
     * @throws MDLException If an error occurs during the encoding process.
     */
    public byte[] encode(Object dataToEncode) throws MDLException {
        try {
            return this.mapper.writeValueAsBytes(dataToEncode);
        } catch (IOException exception) {
            throw new MDLException("Failed to CBOR encode data", exception);
        }
    }
}
