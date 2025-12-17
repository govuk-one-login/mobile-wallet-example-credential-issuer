package uk.gov.di.mobile.wallet.cri.credential.mdoc.cbor;

import com.fasterxml.jackson.dataformat.cbor.databind.CBORMapper;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import uk.gov.di.mobile.wallet.cri.credential.mdoc.IssuerSigned;
import uk.gov.di.mobile.wallet.cri.credential.mdoc.MdocException;

import java.io.IOException;

import static org.junit.jupiter.api.Assertions.assertArrayEquals;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class CBOREncoderTest {

    @Mock private CBORMapper mockMapper;

    @Test
    void Should_ReturnEncodedBytes() throws IOException, MdocException {
        IssuerSigned valueToEncode = mock(IssuerSigned.class);
        byte[] expectedEncodedBytes = {1, 2, 3, 4};
        when(mockMapper.writeValueAsBytes(valueToEncode)).thenReturn(expectedEncodedBytes);

        byte[] actualEncodedBytes = new CBOREncoder(mockMapper).encode(valueToEncode);

        assertArrayEquals(expectedEncodedBytes, actualEncodedBytes);
    }
}
