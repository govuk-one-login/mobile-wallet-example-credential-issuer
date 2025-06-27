package uk.gov.di.mobile.wallet.cri.credential.mobile_driving_licence.cose;

import org.junit.jupiter.api.Test;

import java.util.Map;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;

class COSEProtectedHeaderBuilderTest {
    @Test
    void Should_BuildHeaderWithAlg_When_AlgSet() {
        int algValue = -7; // Example: ES256
        COSEProtectedHeaderBuilder builder = new COSEProtectedHeaderBuilder();

        COSEProtectedHeader header = builder.alg(algValue).build();

        assertNotNull(header);
        Map<Integer, Object> map = header.protectedHeader();
        assertEquals(1, map.size());
        assertEquals(algValue, map.get(1));
    }
}
