package uk.gov.di.mobile.wallet.cri.iacas;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.junit.jupiter.MockitoExtension;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

@ExtendWith(MockitoExtension.class)
class IacaTest {

    private static final String TEST_CERTIFICATE_PEM =
            """
            -----BEGIN CERTIFICATE-----
            MIICzzCCAnWgAwIBAgIUFBD7/XkDw4D/UTy7/pf1Q7c43/kwCgYIKoZIzj0EAwIw
            gbwxCzAJBgNVBAYTAlVLMQ8wDQYDVQQIDAZMb25kb24xNDAyBgNVBAoMK21ETCBF
            eGFtcGxlIElBQ0EgUm9vdCAtIERFTE9DQUwgZW52aXJvbm1lbnQxMjAwBgNVBAsM
            KW1ETCBFeGFtcGxlIElBQ0EgUm9vdCAtIExPQ0FMIGVudmlyb25tZW50MTIwMAYD
            VQQDDCltREwgRXhhbXBsZSBJQUNBIFJvb3QgLSBMT0NBTCBlbnZpcm9ubWVudDAe
            Fw0yNTA2MTkxMTA4NTFaFw0zNTA2MTcxMTA4NTFaMIG8MQswCQYDVQQGEwJVSzEP
            MA0GA1UECAwGTG9uZG9uMTQwMgYDVQQKDCttREwgRXhhbXBsZSBJQUNBIFJvb3Qg
            LSBERUxPQ0FMIGVudmlyb25tZW50MTIwMAYDVQQLDCltREwgRXhhbXBsZSBJQUNB
            IFJvb3QgLSBMT0NBTCBlbnZpcm9ubWVudDEyMDAGA1UEAwwpbURMIEV4YW1wbGUg
            SUFDQSBSb290IC0gTE9DQUwgZW52aXJvbm1lbnQwWTATBgcqhkjOPQIBBggqhkjO
            PQMBBwNCAATK8ZrETZ7FQXw3+xj7fLV2yv1vFLOlZE0r2MQ0ysBOa/uZ7dUlOCvR
            OTt5fpDR9e+Hdq0h9trZwwBY2HODAWVbo1MwUTAdBgNVHQ4EFgQUnelQVCApK3NI
            xVeQ3X+zUsogQxgwHwYDVR0jBBgwFoAUnelQVCApK3NIxVeQ3X+zUsogQxgwDwYD
            VR0TAQH/BAUwAwEB/zAKBggqhkjOPQQDAgNIADBFAiBwnpi6jeCSLxZgFeFLSN+z
            aG3zj9t6QcGFklY521tMtQIhAOF65mV0uski5+50FtKkJcVnS/1EDGrgor5bFeZD
            vdAI
            -----END CERTIFICATE-----
            """;

    @Test
    void Should_CreateFromValidCertificate() throws Exception {
        String id = "83c71bc0-6d39-4908-ac9f-216424e0c5c5";
        boolean active = true;

        Iaca iaca = Iaca.fromCertificate(id, active, TEST_CERTIFICATE_PEM);

        assertEquals("83c71bc0-6d39-4908-ac9f-216424e0c5c5", iaca.id());
        assertTrue(iaca.active());
        assertNotNull(iaca.certificatePem());
        assertNotNull(iaca.certificateData());
        assertNotNull(iaca.certificateFingerprint());
        assertNotNull(iaca.publicKeyJwk());
        assertFalse(
                iaca.certificatePem().contains("\n"),
                "PEM should be normalised (i.e., no line breaks)");
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
