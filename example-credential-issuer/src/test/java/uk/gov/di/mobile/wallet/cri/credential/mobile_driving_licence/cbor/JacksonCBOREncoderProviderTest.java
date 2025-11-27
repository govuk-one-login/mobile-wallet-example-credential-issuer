package uk.gov.di.mobile.wallet.cri.credential.mobile_driving_licence.cbor;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.dataformat.cbor.databind.CBORMapper;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.junit.jupiter.MockitoExtension;
import uk.gov.di.mobile.wallet.cri.credential.mobile_driving_licence.cose.COSESign1;
import uk.gov.di.mobile.wallet.cri.credential.mobile_driving_licence.cose.COSEUnprotectedHeader;
import uk.gov.di.mobile.wallet.cri.credential.mobile_driving_licence.mdoc.IssuerSigned;
import uk.gov.di.mobile.wallet.cri.credential.mobile_driving_licence.mdoc.IssuerSignedItem;

import java.time.LocalDate;
import java.util.List;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;

@ExtendWith(MockitoExtension.class)
class JacksonCBOREncoderProviderTest {

    private static final CBORMapper mapper = JacksonCBOREncoderProvider.configuredCBORMapper();

    @Test
    void Should_ReturnNonNullMapper_When_CBORMapperIsConfigured() {
        assertNotNull(mapper, "Configured CBOR mapper should not be null");
    }

    @Test
    void Should_BeAbleToSerializeLocalDate() {
        LocalDate testDate = LocalDate.of(2025, 4, 4);

        assertDoesNotThrow(() -> mapper.writeValueAsBytes(testDate));
    }

    @Test
    void Should_BeAbleToSerializeIssuerSigned() {
        byte[] protectedHeaderBytes = {1, 2, 3, 4};
        COSEUnprotectedHeader unprotectedHeader =
                new COSEUnprotectedHeader(new byte[] {1, 2, 3, 4});
        byte[] payloadBytes = {1, 2, 3, 4};
        byte[] signatureBytes = {1, 2, 3, 4};
        COSESign1 coseSign1 =
                new COSESign1(
                        protectedHeaderBytes, unprotectedHeader, payloadBytes, signatureBytes);
        Map<String, List<IssuerSignedItem>> nameSpaces =
                Map.of(
                        "namespace",
                        List.of(
                                new IssuerSignedItem(
                                        1,
                                        new byte[] {1, 2, 3, 4},
                                        "test_element_identifier",
                                        "Test Element Value")));
        IssuerSigned valueToSerialize = new IssuerSigned(nameSpaces, coseSign1);

        assertDoesNotThrow(() -> mapper.writeValueAsBytes(valueToSerialize));
    }

    @Test
    void Should_SetDefaultInclusion_ToNonAbsent() {
        var inclusion = mapper.getSerializationConfig().getDefaultPropertyInclusion();

        assertEquals(JsonInclude.Include.NON_ABSENT, inclusion.getValueInclusion());
    }
}
