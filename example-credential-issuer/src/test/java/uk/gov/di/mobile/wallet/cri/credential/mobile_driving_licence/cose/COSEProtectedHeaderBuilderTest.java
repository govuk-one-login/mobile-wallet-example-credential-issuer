package uk.gov.di.mobile.wallet.cri.credential.mobile_driving_licence.cose;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;

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

        assertEquals(algorithm, header.getAlg());
    }
}
