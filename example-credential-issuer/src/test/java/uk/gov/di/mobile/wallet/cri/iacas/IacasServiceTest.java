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
    private static final String TEST_CERTIFICATE_AUTHORITY_ARN =
            "arn:aws:acm-pca:region:account:certificate-authority/1234abcd-12ab-34cd-56ef-1234567890ab";
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
                TEST_CERTIFICATE_PEM,
                iaca.certificatePem(),
                "Certificate PEM should match expected value");
        assertEquals(
                "3907132c3fccd8335625580cf3dce8a3498e578a2f06cd8ed33e05d570e402bf",
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
