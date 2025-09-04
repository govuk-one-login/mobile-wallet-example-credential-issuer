package uk.gov.di.mobile.wallet.cri.iacas;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.junit.jupiter.MockitoExtension;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

@ExtendWith(MockitoExtension.class)
class IacaTest {

    private static final String TEST_CERTIFICATE_PEM =
            """
            -----BEGIN CERTIFICATE-----
            MIIB1zCCAX2gAwIBAgIUIatAsTQsYXy6Wrb1Cdp8tJ3RLC0wCgYIKoZIzj0EAwIw
            QTELMAkGA1UEBhMCR0IxMjAwBgNVBAMMKW1ETCBFeGFtcGxlIElBQ0EgUm9vdCAt
            IExPQ0FMIGVudmlyb25tZW50MB4XDTI1MDkwMjEwMjQyNVoXDTI4MDYyMjEwMjQy
            NVowQTELMAkGA1UEBhMCR0IxMjAwBgNVBAMMKW1ETCBFeGFtcGxlIElBQ0EgUm9v
            dCAtIExPQ0FMIGVudmlyb25tZW50MFkwEwYHKoZIzj0CAQYIKoZIzj0DAQcDQgAE
            mBxJk2MqFKn7c4MSEwlA8EUbMMxyU8DnPXwERUs4VjBF7534WDQQLCZBxvaYn73M
            35NYkWiXO8oiRmWG9AzDn6NTMFEwHQYDVR0OBBYEFPY4eri7CuGrxh14YMTQe1qn
            BVjoMB8GA1UdIwQYMBaAFPY4eri7CuGrxh14YMTQe1qnBVjoMA8GA1UdEwEB/wQF
            MAMBAf8wCgYIKoZIzj0EAwIDSAAwRQIgPJmIjY1hoYRHjBMgLeV0x+wWietEyBfx
            zyaulhhqnewCIQCmJ0kwBidqVzCOIx5H8CaEHUnTA/ULJGC2DDFzT7s54A==
            -----END CERTIFICATE-----
            """;

    @Test
    void Should_CreateFromValidCertificate() throws Exception {
        String id = "83c71bc0-6d39-4908-ac9f-216424e0c5c5";
        boolean active = true;

        Iaca iaca = Iaca.fromCertificate(id, active, TEST_CERTIFICATE_PEM);

        assertEquals("83c71bc0-6d39-4908-ac9f-216424e0c5c5", iaca.id());
        assertTrue(iaca.active());
        assertEquals(TEST_CERTIFICATE_PEM, iaca.certificatePem());
        assertEquals(
                new CertificateData(
                        "2028-06-22T10:24:25.000Z",
                        "2025-09-02T10:24:25.000Z",
                        "GB",
                        "mDL Example IACA Root - LOCAL environment"),
                iaca.certificateData());
        assertEquals(
                "3907132c3fccd8335625580cf3dce8a3498e578a2f06cd8ed33e05d570e402bf",
                iaca.certificateFingerprint());
        assertEquals(
                new PublicKeyJwk(
                        "EC",
                        "P-256",
                        "mBxJk2MqFKn7c4MSEwlA8EUbMMxyU8DnPXwERUs4VjA",
                        "Re-d-Fg0ECwmQcb2mJ-9zN-TWJFolzvKIkZlhvQMw58",
                        "ES256"),
                iaca.publicKeyJwk());
    }

    @Test
    void Should_ThrowException_WhenPemIsInvalid() {
        String invalidPem = "-----BEGIN CERTIFICATE-----\nINVALID\n-----END CERTIFICATE-----";
        String id = "83c71bc0-6d39-4908-ac9f-216424e0c5c5";
        boolean active = false;

        Exception exception =
                assertThrows(
                        IllegalArgumentException.class,
                        () -> Iaca.fromCertificate(id, active, invalidPem));
        assertEquals(
                "Failed to parse PEM certificate: parsing returned null", exception.getMessage());
    }

    @Test
    void Should_GenerateFingerprintCorrectly() throws Exception {
        String id = "83c71bc0-6d39-4908-ac9f-216424e0c5c5";
        boolean active = true;

        Iaca iaca = Iaca.fromCertificate(id, active, TEST_CERTIFICATE_PEM);

        String fingerprint = iaca.certificateFingerprint();
        assertNotNull(fingerprint);
        assertEquals(
                64,
                fingerprint.length(),
                "Fingerprint should be a 64-character hex string for SHA-256");
        assertTrue(
                fingerprint.matches("[0-9a-f]++"),
                "Fingerprint should contain only lowercase hexadecimal characters");
    }
}
