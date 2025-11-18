package uk.gov.di.mobile.wallet.cri.credential.mobile_driving_licence.cbor;

import com.fasterxml.jackson.dataformat.cbor.databind.CBORMapper;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.junit.jupiter.MockitoExtension;

import static org.junit.jupiter.api.Assertions.assertNotNull;

@ExtendWith(MockitoExtension.class)
class JacksonCBOREncoderProviderTest {

    @Test
    void Should_ReturnNonNullMapper_When_CBORMapperIsConfigured() {
        CBORMapper mapper = JacksonCBOREncoderProvider.configuredCBORMapper();

        assertNotNull(mapper, "Configured CBOR mapper should not be null");
    }
}
