package uk.gov.di.mobile.wallet.cri.iacas;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;

class PublicKeyJwkTest {

    @Test
    void Should_CreateRecordWithAllFields() {
        String kty = "EC";
        String crv = "P-256";
        String x = "WKn-ZIGevcwGIyyrzFoZNBdaq9_TsqzGHwHitJBcBmXmyPK";
        String y = "y77As5vbZdIGd9lrAqMqkTI1Kj9DiVTmBX5KbKWpkCg";

        PublicKeyJwk jwk = new PublicKeyJwk(kty, crv, x, y);

        assertEquals(kty, jwk.kty());
        assertEquals(crv, jwk.crv());
        assertEquals(x, jwk.x());
        assertEquals(y, jwk.y());
    }
}
