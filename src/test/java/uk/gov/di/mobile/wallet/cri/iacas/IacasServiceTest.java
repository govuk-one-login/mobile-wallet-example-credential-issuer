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
                    + "MIIB8TCCAZegAwIBAgIQLmndcaaE19g+3lJupyQCojAKBggqhkjOPQQDAjA/MQsw\n"
                    + "CQYDVQQGEwJVSzEwMC4GA1UEAwwnbURMIEV4YW1wbGUgSUFDQSBSb290IC0gREVW\n"
                    + "IGVudmlyb25tZW50MB4XDTI1MDQxNTA5MTQyMFoXDTM0MDQxNjEwMTQyMFowPzEL\n"
                    + "MAkGA1UEBhMCVUsxMDAuBgNVBAMMJ21ETCBFeGFtcGxlIElBQ0EgUm9vdCAtIERF\n"
                    + "ViBlbnZpcm9ubWVudDBZMBMGByqGSM49AgEGCCqGSM49AwEHA0IABBV+UF7RuBa4\n"
                    + "gu0aVFRRD1plr+Bnu1dsv9eNXbU2ZqGq0FkM4IknCZ12Y/zENDVA8VyM+hNRlkvr\n"
                    + "SZMoqCSnobejdTBzMBIGA1UdEwEB/wQIMAYBAf8CAQAwHQYDVR0OBBYEFHomsAv2\n"
                    + "hf6lUS/4necL1PqJLNEJMA8GA1UdDwEB/wQFAwMHBgAwLQYDVR0SBCYwJIYiaHR0\n"
                    + "cHM6Ly9tb2JpbGUuZGV2LmFjY291bnQuZ292LnVrLzAKBggqhkjOPQQDAgNIADBF\n"
                    + "AiBFqaelXoq3kySjLkoy6cbnv5mFfUjyFN9emgHyWcy2OgIhAOtizfGVyHNAQ2wD\n"
                    + "z6mnTX/lWqYiEThH9Gb3xRXKrslN\n"
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
                "-----BEGIN CERTIFICATE-----MIIB8TCCAZegAwIBAgIQLmndcaaE19g+3lJupyQCojAKBggqhkjOPQQDAjA/MQswCQYDVQQGEwJVSzEwMC4GA1UEAwwnbURMIEV4YW1wbGUgSUFDQSBSb290IC0gREVWIGVudmlyb25tZW50MB4XDTI1MDQxNTA5MTQyMFoXDTM0MDQxNjEwMTQyMFowPzELMAkGA1UEBhMCVUsxMDAuBgNVBAMMJ21ETCBFeGFtcGxlIElBQ0EgUm9vdCAtIERFViBlbnZpcm9ubWVudDBZMBMGByqGSM49AgEGCCqGSM49AwEHA0IABBV+UF7RuBa4gu0aVFRRD1plr+Bnu1dsv9eNXbU2ZqGq0FkM4IknCZ12Y/zENDVA8VyM+hNRlkvrSZMoqCSnobejdTBzMBIGA1UdEwEB/wQIMAYBAf8CAQAwHQYDVR0OBBYEFHomsAv2hf6lUS/4necL1PqJLNEJMA8GA1UdDwEB/wQFAwMHBgAwLQYDVR0SBCYwJIYiaHR0cHM6Ly9tb2JpbGUuZGV2LmFjY291bnQuZ292LnVrLzAKBggqhkjOPQQDAgNIADBFAiBFqaelXoq3kySjLkoy6cbnv5mFfUjyFN9emgHyWcy2OgIhAOtizfGVyHNAQ2wDz6mnTX/lWqYiEThH9Gb3xRXKrslN-----END CERTIFICATE-----",
                iaca.certificatePem(),
                "Certificate PEM should match expected value");
        assertEquals(
                "269893baa0eda2ceb9e8d9db971c5750c330f5e5baac1b0501d9d45327eff212",
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
