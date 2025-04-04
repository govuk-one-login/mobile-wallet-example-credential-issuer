package uk.gov.di.mobile.wallet.cri.credential.mobile_driving_licence.cbor;

import com.fasterxml.jackson.dataformat.cbor.databind.CBORMapper;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import uk.gov.di.mobile.wallet.cri.credential.mobile_driving_licence.mdoc.IssuerAuth;
import uk.gov.di.mobile.wallet.cri.credential.mobile_driving_licence.mdoc.IssuerSigned;
import uk.gov.di.mobile.wallet.cri.credential.mobile_driving_licence.mdoc.IssuerSignedItem;

import java.time.LocalDate;
import java.util.List;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.*;

class JacksonCBOREncoderProviderTest {

    private CBORMapper mapper;

    @BeforeEach
    void setUp() {
        mapper = JacksonCBOREncoderProvider.configuredCBORMapper();
    }

    @Test
    void Should_ConfigureCBORMapperWithCustomSerializers() {
        assertTrue(mapper.canSerialize(LocalDate.class));
        assertTrue(mapper.canSerialize(IssuerSigned.class));
    }

    @Test
    void Should_SerializeLocalDate() throws Exception {
        LocalDate testDate = LocalDate.of(2025, 4, 4);

        byte[] serialized = mapper.writeValueAsBytes(testDate);

        assertNotNull(serialized);
        assertTrue(serialized.length > 0);
    }

    @Test
    void Should_SerializeIssuerSigned() throws Exception {
        IssuerSigned testIssuerSigned = createTestIssuerSigned();

        byte[] serialized = mapper.writeValueAsBytes(testIssuerSigned);

        assertNotNull(serialized);
        assertTrue(serialized.length > 0);
    }

    private IssuerSigned createTestIssuerSigned() {
        byte[] randomBytes = {0x01, 0x02, 0x03, 0x04};
        IssuerSignedItem testIssuerSignedItem =
                new IssuerSignedItem(2, randomBytes, "family_name", "Bonbon");
        Map<String, List<IssuerSignedItem>> testNameSpaces =
                Map.of("namespace", List.of(testIssuerSignedItem));
        return new IssuerSigned(testNameSpaces, new IssuerAuth());
    }
}
