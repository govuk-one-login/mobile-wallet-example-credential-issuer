package uk.gov.di.mobile.wallet.cri.credential.mobile_driving_licence.cbor;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.dataformat.cbor.databind.CBORMapper;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ValueSource;
import org.mockito.junit.jupiter.MockitoExtension;
import uk.gov.di.mobile.wallet.cri.credential.mobile_driving_licence.DrivingPrivilege;
import uk.gov.di.mobile.wallet.cri.credential.mobile_driving_licence.cose.COSEProtectedHeader;
import uk.gov.di.mobile.wallet.cri.credential.mobile_driving_licence.cose.COSEUnprotectedHeader;
import uk.gov.di.mobile.wallet.cri.credential.mobile_driving_licence.mdoc.DeviceKeyInfo;
import uk.gov.di.mobile.wallet.cri.credential.mobile_driving_licence.mdoc.IssuerSigned;
import uk.gov.di.mobile.wallet.cri.credential.mobile_driving_licence.mdoc.IssuerSignedItem;
import uk.gov.di.mobile.wallet.cri.credential.mobile_driving_licence.mdoc.MobileSecurityObject;
import uk.gov.di.mobile.wallet.cri.credential.mobile_driving_licence.mdoc.Status;
import uk.gov.di.mobile.wallet.cri.credential.mobile_driving_licence.mdoc.ValidityInfo;
import uk.gov.di.mobile.wallet.cri.credential.mobile_driving_licence.mdoc.ValueDigests;

import java.time.Instant;
import java.time.LocalDate;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

@ExtendWith(MockitoExtension.class)
class JacksonCBOREncoderProviderTest {

    private static final CBORMapper mapper = JacksonCBOREncoderProvider.configuredCBORMapper();

    @Test
    void Should_ReturnNonNullMapper_When_CBORMapperIsConfigured() {
        assertNotNull(mapper, "Configured CBOR mapper should not be null");
    }

    @ParameterizedTest(name = "Should be able to serialize {0}")
    @ValueSource(
            classes = {
                LocalDate.class,
                Instant.class,
                IssuerSignedItem.class,
                MobileSecurityObject.class,
                IssuerSigned.class,
                DrivingPrivilege.class,
                ValidityInfo.class,
                Status.class,
                DeviceKeyInfo.class,
                ValueDigests.class,
                COSEProtectedHeader.class,
                COSEUnprotectedHeader.class,
            })
    void Should_BeAbleToSerialize_Types(Class<?> type) {
        var mapper = JacksonCBOREncoderProvider.configuredCBORMapper();
        assertTrue(mapper.canSerialize(type));
    }

    @Test
    void Should_SetDefaultInclusion_ToNonAbsent() {
        CBORMapper mapper = JacksonCBOREncoderProvider.configuredCBORMapper();
        var inclusion = mapper.getSerializationConfig().getDefaultPropertyInclusion();

        assertEquals(JsonInclude.Include.NON_ABSENT, inclusion.getValueInclusion());
    }
}
