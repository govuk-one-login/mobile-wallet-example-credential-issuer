package uk.gov.di.mobile.wallet.cri.iacas;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import uk.gov.di.mobile.wallet.cri.services.ConfigurationService;
import uk.gov.di.mobile.wallet.cri.services.object_storage.ObjectStore;

import java.util.List;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

class IacasServiceTest {

    private ConfigurationService configurationService;
    private ObjectStore objectStore;
    private IacasService iacasService;

    private static final String TEST_CERTIFICATE_PEM =
            "-----BEGIN CERTIFICATE-----\n"
                    + "MIICzzCCAnWgAwIBAgIUFBD7/XkDw4D/UTy7/pf1Q7c43/kwCgYIKoZIzj0EAwIw\n"
                    + "gbwxCzAJBgNVBAYTAlVLMQ8wDQYDVQQIDAZMb25kb24xNDAyBgNVBAoMK21ETCBF\n"
                    + "eGFtcGxlIElBQ0EgUm9vdCAtIERFTE9DQUwgZW52aXJvbm1lbnQxMjAwBgNVBAsM\n"
                    + "KW1ETCBFeGFtcGxlIElBQ0EgUm9vdCAtIExPQ0FMIGVudmlyb25tZW50MTIwMAYD\n"
                    + "VQQDDCltREwgRXhhbXBsZSBJQUNBIFJvb3QgLSBMT0NBTCBlbnZpcm9ubWVudDAe\n"
                    + "Fw0yNTA2MTkxMTA4NTFaFw0zNTA2MTcxMTA4NTFaMIG8MQswCQYDVQQGEwJVSzEP\n"
                    + "MA0GA1UECAwGTG9uZG9uMTQwMgYDVQQKDCttREwgRXhhbXBsZSBJQUNBIFJvb3Qg\n"
                    + "LSBERUxPQ0FMIGVudmlyb25tZW50MTIwMAYDVQQLDCltREwgRXhhbXBsZSBJQUNB\n"
                    + "IFJvb3QgLSBMT0NBTCBlbnZpcm9ubWVudDEyMDAGA1UEAwwpbURMIEV4YW1wbGUg\n"
                    + "SUFDQSBSb290IC0gTE9DQUwgZW52aXJvbm1lbnQwWTATBgcqhkjOPQIBBggqhkjO\n"
                    + "PQMBBwNCAATK8ZrETZ7FQXw3+xj7fLV2yv1vFLOlZE0r2MQ0ysBOa/uZ7dUlOCvR\n"
                    + "OTt5fpDR9e+Hdq0h9trZwwBY2HODAWVbo1MwUTAdBgNVHQ4EFgQUnelQVCApK3NI\n"
                    + "xVeQ3X+zUsogQxgwHwYDVR0jBBgwFoAUnelQVCApK3NIxVeQ3X+zUsogQxgwDwYD\n"
                    + "VR0TAQH/BAUwAwEB/zAKBggqhkjOPQQDAgNIADBFAiBwnpi6jeCSLxZgFeFLSN+z\n"
                    + "aG3zj9t6QcGFklY521tMtQIhAOF65mV0uski5+50FtKkJcVnS/1EDGrgor5bFeZD\n"
                    + "vdAI\n"
                    + "-----END CERTIFICATE-----";

    @BeforeEach
    void setUp() {
        configurationService = mock(ConfigurationService.class);
        objectStore = mock(ObjectStore.class);
        iacasService = new IacasService(configurationService, objectStore);
    }

    @Test
    void Should_CallConfigurationServiceToGetBucketNameAndCertificateArn() throws Exception {
        String bucketName = "test-bucket";
        String certificateAuthorityArn =
                "arn:aws:acm-pca:region:account:certificate-authority/1234abcd";
        when(configurationService.getCertificatesBucketName()).thenReturn(bucketName);
        when(configurationService.getCertificateAuthorityArn()).thenReturn(certificateAuthorityArn);
        when(objectStore.getObject(anyString(), anyString())).thenReturn(TEST_CERTIFICATE_PEM);

        iacasService.getIacas();

        verify(configurationService).getCertificatesBucketName();
        verify(configurationService).getCertificateAuthorityArn();
    }

    @Test
    void Should_CallObjectStoreWithCorrectArguments() throws Exception {
        String bucketName = "test-bucket";
        String certificateAuthorityArn =
                "arn:aws:acm-pca:region:account:certificate-authority/1234abcd";
        String rootCertificateId = "1234abcd";
        String objectKey = rootCertificateId + "/certificate.pem";
        when(configurationService.getCertificatesBucketName()).thenReturn(bucketName);
        when(configurationService.getCertificateAuthorityArn()).thenReturn(certificateAuthorityArn);
        when(objectStore.getObject(anyString(), anyString())).thenReturn(TEST_CERTIFICATE_PEM);

        iacasService.getIacas();

        verify(objectStore).getObject(bucketName, objectKey);
    }

    @Test
    void Should_PropagatesException_When_ObjectStoreThrowsException() throws Exception {
        String bucketName = "test-bucket";
        String certificateAuthorityArn =
                "arn:aws:acm-pca:region:account:certificate-authority/1234abcd";
        String rootCertificateId = "1234abcd";
        String objectKey = rootCertificateId + "/certificate.pem";
        when(configurationService.getCertificatesBucketName()).thenReturn(bucketName);
        when(configurationService.getCertificateAuthorityArn()).thenReturn(certificateAuthorityArn);
        when(objectStore.getObject(bucketName, objectKey))
                .thenThrow(new RuntimeException("Object not found"));

        Exception exception = assertThrows(RuntimeException.class, () -> iacasService.getIacas());

        assertEquals("Object not found", exception.getMessage());
    }

    @Test
    void Should_ReturnIacasWithExpectedValues() throws Exception {
        String bucketName = "test-bucket";
        String certificateAuthorityArn =
                "arn:aws:acm-pca:region:account:certificate-authority/1234abcd";
        String rootCertificateId = "1234abcd";
        String objectKey = rootCertificateId + "/certificate.pem";
        when(configurationService.getCertificatesBucketName()).thenReturn(bucketName);
        when(configurationService.getCertificateAuthorityArn()).thenReturn(certificateAuthorityArn);
        when(objectStore.getObject(bucketName, objectKey)).thenReturn(TEST_CERTIFICATE_PEM);

        Iacas result = iacasService.getIacas();

        List<Iaca> iacaList = result.data();
        assertEquals(1, iacaList.size(), "Iacas contain exactly one certificate");
        Iaca iaca = iacaList.get(0);
        assertEquals(rootCertificateId, iaca.id(), "Certificate ID should match expected value");
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
