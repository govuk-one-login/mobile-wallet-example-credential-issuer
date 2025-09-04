package uk.gov.di.mobile.wallet.cri.credential.mobile_driving_licence.cbor;

import com.fasterxml.jackson.dataformat.cbor.databind.CBORMapper;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import uk.gov.di.mobile.wallet.cri.credential.mdoc.mobile_driving_licence.MDLException;
import uk.gov.di.mobile.wallet.cri.credential.mdoc.mobile_driving_licence.cbor.CBOREncoder;
import uk.gov.di.mobile.wallet.cri.credential.mdoc.mobile_driving_licence.mdoc.IssuerSigned;
import uk.gov.di.mobile.wallet.cri.credential.mdoc.mobile_driving_licence.mdoc.IssuerSignedItem;

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
    void Should_ReturnEncodedBytes_When_EncodingDocument() throws IOException, MDLException {
        IssuerSigned issuerSigned = mock(IssuerSigned.class);
        byte[] expectedEncodedBytes = {1, 2, 3, 4};
        when(mockMapper.writeValueAsBytes(issuerSigned)).thenReturn(expectedEncodedBytes);

        byte[] actualEncodedBytes = cborEncoder.encode(issuerSigned);

        assertArrayEquals(expectedEncodedBytes, actualEncodedBytes);
    }

    @Test
    void Should_ReturnEncodedBytes_When_EncodingIssuerSignedItem()
            throws IOException, MDLException {
        IssuerSignedItem issuerSignedItem = mock(IssuerSignedItem.class);
        byte[] expectedEncodedBytes = {1, 2, 3, 4};
        when(mockMapper.writeValueAsBytes(issuerSignedItem)).thenReturn(expectedEncodedBytes);

        byte[] actualEncodedBytes = cborEncoder.encode(issuerSignedItem);

        assertArrayEquals(expectedEncodedBytes, actualEncodedBytes);
    }
}
