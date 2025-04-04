package uk.gov.di.mobile.wallet.cri.credential.mobile_driving_licence.cbor;

import com.fasterxml.jackson.dataformat.cbor.databind.CBORMapper;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import uk.gov.di.mobile.wallet.cri.credential.mobile_driving_licence.mdoc.DeviceResponse;

import java.io.IOException;

import static org.junit.jupiter.api.Assertions.assertArrayEquals;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

class CBOREncoderTest {

    private CBORMapper mockMapper;
    private CBOREncoder cborEncoder;

    @BeforeEach
    void setUp() {
        mockMapper = mock(CBORMapper.class);
        cborEncoder = new CBOREncoder(mockMapper);
    }

    @Test
    void Should_ReturnEncodedBytes() throws IOException {
        DeviceResponse mockDeviceResponse = mock(DeviceResponse.class);
        byte[] expectedEncodedBytes = {1, 2, 3, 4};
        when(mockMapper.writeValueAsBytes(mockDeviceResponse)).thenReturn(expectedEncodedBytes);

        byte[] actualEncodedBytes = cborEncoder.encode(mockDeviceResponse);

        assertArrayEquals(expectedEncodedBytes, actualEncodedBytes);
    }
}
