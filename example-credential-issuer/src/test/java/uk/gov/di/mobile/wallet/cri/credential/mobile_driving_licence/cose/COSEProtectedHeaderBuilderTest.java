package uk.gov.di.mobile.wallet.cri.credential.mobile_driving_licence.cose;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import uk.gov.di.mobile.wallet.cri.credential.mdoc.mobile_driving_licence.cose.COSEProtectedHeader;
import uk.gov.di.mobile.wallet.cri.credential.mdoc.mobile_driving_licence.cose.COSEProtectedHeaderBuilder;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertThrows;

class COSEProtectedHeaderBuilderTest {

    private COSEProtectedHeaderBuilder builder;

    @BeforeEach
    void setUp() {
        builder = new COSEProtectedHeaderBuilder();
    }

    @Test
    void Should_BuildCOSEProtectedHeader() {
        int algorithm = -7; // ES256 algorithm
        COSEProtectedHeader header = builder.alg(algorithm).build();

        assertNotNull(header);
        assertEquals(algorithm, header.protectedHeader().get(1));
    }

    @Test
    void Should_ThrowIllegalArgumentException_When_X5chainIsNotSet() {
        IllegalStateException exception =
                assertThrows(IllegalStateException.class, () -> builder.build());
        assertEquals("alg must be set", exception.getMessage());
    }
}
