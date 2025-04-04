package uk.gov.di.mobile.wallet.cri.credential.mobile_driving_licence.cbor;

import com.fasterxml.jackson.dataformat.cbor.databind.CBORMapper;
import uk.gov.di.mobile.wallet.cri.credential.mobile_driving_licence.mdoc.DeviceResponse;

import java.io.IOException;

/**
 * Encodes a {@link DeviceResponse} into its CBOR representation according to the ISO-18013-5
 * specification.
 */
public class CBOREncoder {
    private final CBORMapper mapper;

    /**
     * Constructs the encoder with a configured CBOR mapper.
     *
     * @param mapper The mapper used to write out CBOR values.
     */
    public CBOREncoder(CBORMapper mapper) {
        this.mapper = mapper;
    }

    /**
     * Encodes the given device response into CBOR representation.
     *
     * @param deviceResponse The DeviceResponse to encode.
     * @return The CBOR encoded representation of the device response.
     * @throws IOException If an error occurs during encoding.
     */
    public byte[] encode(DeviceResponse deviceResponse) throws IOException {
        return this.mapper.writeValueAsBytes(deviceResponse);
    }
}
