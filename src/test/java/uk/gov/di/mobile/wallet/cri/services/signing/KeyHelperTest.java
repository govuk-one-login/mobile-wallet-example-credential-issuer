package uk.gov.di.mobile.wallet.cri.services.signing;

import org.junit.jupiter.api.Test;

import java.security.NoSuchAlgorithmException;

import static org.junit.jupiter.api.Assertions.assertEquals;

class KeyHelperTest {
    @Test
    void shouldHashAKeyId() throws NoSuchAlgorithmException {
        String keyId = "ff275b92-0def-4dfc-b0f6-87c96b26c6c7";

        assertEquals(
                "78fa131d677c1ac0f172c53b47ac169a95ad0d92c38bd794a70da59032058274",
                KeyHelper.hashKeyId(keyId));
    }
}
