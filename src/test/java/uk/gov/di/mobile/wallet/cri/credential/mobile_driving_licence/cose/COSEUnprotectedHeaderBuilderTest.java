package uk.gov.di.mobile.wallet.cri.credential.mobile_driving_licence.cose;

import org.junit.jupiter.api.Test;

import java.util.Map;

import static org.hibernate.validator.internal.util.Contracts.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertEquals;

class COSEUnprotectedHeaderBuilderTest {

    @Test
    void Should_BuildHeaderWithX5Chain_When_X5ChainSet() {
        Object x5chainValue = "dummy-certificate-chain";
        COSEUnprotectedHeaderBuilder builder = new COSEUnprotectedHeaderBuilder();
        builder.x5chain(x5chainValue);

        COSEUnprotectedHeader header = builder.build();

        assertNotNull(header);
        Map<Integer, Object> map = header.unprotectedHeader();
        assertEquals(1, map.size());
        assertEquals(x5chainValue, map.get(33));
    }
}
