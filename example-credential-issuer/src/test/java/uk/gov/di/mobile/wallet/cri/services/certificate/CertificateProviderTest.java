package uk.gov.di.mobile.wallet.cri.services.certificate;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import uk.gov.di.mobile.wallet.cri.services.object_storage.ObjectStore;
import uk.gov.di.mobile.wallet.cri.services.object_storage.ObjectStoreException;

import java.nio.charset.StandardCharsets;
import java.security.cert.CertificateException;
import java.security.cert.X509Certificate;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

class CertificateProviderTest {

    private ObjectStore objectStore;
    private CertificateProvider certificateProvider;

    private static final String BUCKET_NAME = "test-bucket";
    private static final String CERTIFICATE_ID = "1234abcd-12ab-34cd-56ef-1234567890ab";
    private static final String ROOT_OBJECT_KEY = "root/" + CERTIFICATE_ID + "/certificate.pem";
    private static final String SIGN_OBJECT_KEY = "sign/" + CERTIFICATE_ID + "/certificate.pem";
    private static final String CERTIFICATE_PEM =
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
    private static final byte[] CERTIFICATE_BYTES =
            CERTIFICATE_PEM.getBytes(StandardCharsets.UTF_8);

    @BeforeEach
    void setUp() {
        objectStore = mock(ObjectStore.class);
        certificateProvider = new CertificateProvider(objectStore, BUCKET_NAME);
    }

    @Test
    void Should_ReturnReturnCertificateString() throws Exception {
        when(objectStore.getObject(BUCKET_NAME, ROOT_OBJECT_KEY)).thenReturn(CERTIFICATE_BYTES);

        String result = certificateProvider.getRootCertificate(CERTIFICATE_ID);

        assertEquals(CERTIFICATE_PEM, result);
        verify(objectStore).getObject(BUCKET_NAME, ROOT_OBJECT_KEY);
    }

    @Test
    void Should_ReturnX509Certificate() throws Exception {
        when(objectStore.getObject(BUCKET_NAME, SIGN_OBJECT_KEY)).thenReturn(CERTIFICATE_BYTES);

        X509Certificate result = certificateProvider.getSigningCertificate(CERTIFICATE_ID);

        assertNotNull(result);
        assertEquals("X.509", result.getType());
    }

    @Test
    void Should_PropagateExceptionThrownByObjectStore() throws Exception {
        when(objectStore.getObject(BUCKET_NAME, SIGN_OBJECT_KEY))
                .thenThrow(new ObjectStoreException("Not found", new RuntimeException()));

        assertThrows(
                ObjectStoreException.class,
                () -> certificateProvider.getSigningCertificate(CERTIFICATE_ID));
    }

    @Test
    void Should_ThrowsCertificateException_When_CertificateIsInvalid() throws Exception {
        when(objectStore.getObject(BUCKET_NAME, SIGN_OBJECT_KEY))
                .thenReturn("not a cert".getBytes(StandardCharsets.UTF_8));

        assertThrows(
                CertificateException.class,
                () -> certificateProvider.getSigningCertificate(CERTIFICATE_ID));
    }
}
