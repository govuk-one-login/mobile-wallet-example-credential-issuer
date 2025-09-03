package uk.gov.di.mobile.wallet.cri.iacas;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import uk.gov.di.mobile.wallet.cri.services.certificate.CertificateProvider;

import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertInstanceOf;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

class IacasServiceTest {

    private CertificateProvider certificateProvider;
    private IacasService iacasService;

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
    private static final String TEST_CERTIFICATE_AUTHORITY_ARN =
            "arn:aws:acm-pca:region:account:certificate-authority/root/1234abcd-12ab-34cd-56ef-1234567890ab";
    private static final String TEST_CERTIFICATE_AUTHORITY_ID =
            "1234abcd-12ab-34cd-56ef-1234567890ab";

    @BeforeEach
    void setUp() {
        certificateProvider = mock(CertificateProvider.class);
        iacasService = new IacasService(certificateProvider, TEST_CERTIFICATE_AUTHORITY_ARN);
    }

    @Test
    void Should_PropagatesException_When_ObjectStoreThrowsException() throws Exception {
        when(certificateProvider.getCertificateAsString(TEST_CERTIFICATE_AUTHORITY_ID))
                .thenThrow(new RuntimeException("Failed to get certificate"));

        Exception exception = assertThrows(RuntimeException.class, () -> iacasService.getIacas());

        assertEquals("Failed to get certificate", exception.getMessage());
    }

    @Test
    void Should_ReturnIacasWithExpectedValues() throws Exception {
        when(certificateProvider.getCertificateAsString(TEST_CERTIFICATE_AUTHORITY_ID))
                .thenReturn(TEST_CERTIFICATE_PEM);

        Iacas result = iacasService.getIacas();

        List<Iaca> iacaList = result.data();
        assertEquals(1, iacaList.size(), "Iacas contain exactly one certificate");
        Iaca iaca = iacaList.get(0);
        assertEquals(
                TEST_CERTIFICATE_AUTHORITY_ID,
                iaca.id(),
                "Certificate ID should match expected value");
        assertTrue(iaca.active(), "Certificate should be active");
        assertEquals(
                "-----BEGIN CERTIFICATE-----MIICzzCCAnWgAwIBAgIUFBD7/XkDw4D/UTy7/pf1Q7c43/kwCgYIKoZIzj0EAwIwgbwxCzAJBgNVBAYTAlVLMQ8wDQYDVQQIDAZMb25kb24xNDAyBgNVBAoMK21ETCBFeGFtcGxlIElBQ0EgUm9vdCAtIERFTE9DQUwgZW52aXJvbm1lbnQxMjAwBgNVBAsMKW1ETCBFeGFtcGxlIElBQ0EgUm9vdCAtIExPQ0FMIGVudmlyb25tZW50MTIwMAYDVQQDDCltREwgRXhhbXBsZSBJQUNBIFJvb3QgLSBMT0NBTCBlbnZpcm9ubWVudDAeFw0yNTA2MTkxMTA4NTFaFw0zNTA2MTcxMTA4NTFaMIG8MQswCQYDVQQGEwJVSzEPMA0GA1UECAwGTG9uZG9uMTQwMgYDVQQKDCttREwgRXhhbXBsZSBJQUNBIFJvb3QgLSBERUxPQ0FMIGVudmlyb25tZW50MTIwMAYDVQQLDCltREwgRXhhbXBsZSBJQUNBIFJvb3QgLSBMT0NBTCBlbnZpcm9ubWVudDEyMDAGA1UEAwwpbURMIEV4YW1wbGUgSUFDQSBSb290IC0gTE9DQUwgZW52aXJvbm1lbnQwWTATBgcqhkjOPQIBBggqhkjOPQMBBwNCAATK8ZrETZ7FQXw3+xj7fLV2yv1vFLOlZE0r2MQ0ysBOa/uZ7dUlOCvROTt5fpDR9e+Hdq0h9trZwwBY2HODAWVbo1MwUTAdBgNVHQ4EFgQUnelQVCApK3NIxVeQ3X+zUsogQxgwHwYDVR0jBBgwFoAUnelQVCApK3NIxVeQ3X+zUsogQxgwDwYDVR0TAQH/BAUwAwEB/zAKBggqhkjOPQQDAgNIADBFAiBwnpi6jeCSLxZgFeFLSN+zaG3zj9t6QcGFklY521tMtQIhAOF65mV0uski5+50FtKkJcVnS/1EDGrgor5bFeZDvdAI-----END CERTIFICATE-----",
                iaca.certificatePem(),
                "Certificate PEM should match expected value");
        assertEquals(
                "62c8cec7894b4dad6ead9301644aacdec5cf864638d7d65790c5c1f152cd1507",
                iaca.certificateFingerprint(),
                "Certificate fingerprint should match expected value");
        assertInstanceOf(
                CertificateData.class,
                iaca.certificateData(),
                "certificateData should be of type CertificateData");
        assertInstanceOf(
                PublicKeyJwk.class,
                iaca.publicKeyJwk(),
                "publicKeyJwk should be of type PublicKeyJwk");
    }
}
