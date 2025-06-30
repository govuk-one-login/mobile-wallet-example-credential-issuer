package uk.gov.di.mobile.wallet.cri.credential.mobile_driving_licence.cose;

import uk.gov.di.mobile.wallet.cri.services.ConfigurationService;
import uk.gov.di.mobile.wallet.cri.services.object_storage.ObjectStore;
import uk.gov.di.mobile.wallet.cri.services.object_storage.ObjectStoreException;

import java.io.ByteArrayInputStream;
import java.nio.charset.StandardCharsets;
import java.security.cert.CertificateException;
import java.security.cert.CertificateFactory;
import java.security.cert.X509Certificate;

import static uk.gov.di.mobile.wallet.cri.util.ArnUtil.extractCertificateAuthorityId;

public class CertificateProvider {

    private final ObjectStore objectStore;
    private final ConfigurationService configurationService;

    public CertificateProvider(ObjectStore objectStore, ConfigurationService configurationService) {
        this.objectStore = objectStore;
        this.configurationService = configurationService;
    }

    public X509Certificate getCertificate() throws CertificateException, ObjectStoreException {
        //        String certificatePem =
        //                """
        //                        -----BEGIN CERTIFICATE-----
        //                        MIIBXzCCAQSgAwIBAgIGAYwpA4/aMAoGCCqGSM49BAMCMDYxNDAyBgNVBAMMKzNf
        //                        d1F3Y3Qxd28xQzBST3FfWXRqSTRHdTBqVXRiVTJCQXZteEltQzVqS3MwHhcNMjMx
        //                        MjAyMDUzMjI4WhcNMjQwOTI3MDUzMjI4WjA2MTQwMgYDVQQDDCszX3dRd2N0MXdv
        //                        MUMwUk9xX1l0akk0R3UwalV0YlUyQkF2bXhJbUM1aktzMFkwEwYHKoZIzj0CAQYI
        //                        KoZIzj0DAQcDQgAEQw7367PjIwU17ckX/G4ZqLW2EjPG0efV0cYzhvq2Ujkymrc3
        //                        3RVkgEE6q9iAAeLhl85IraAzT39SjOBV1EKu3jAKBggqhkjOPQQDAgNJADBGAiEA
        //                        o4TsuxDl5+3eEp6SHDrBVn1rqOkGGLoOukJhelndGqICIQCpocrjWDwrWexoQZOO
        //                        rwnEYRBmmfhaPor2OZCrbP3U6w==
        //                        -----END CERTIFICATE-----
        //                        """;

        String bucketName = configurationService.getCertificatesBucketName();
        String certificateAuthorityArn = configurationService.getCertificateAuthorityArn();

        String certificateAuthorityId = extractCertificateAuthorityId(certificateAuthorityArn);
        String objectKey = certificateAuthorityId + "/certificate.pem";
        String certificatePem = objectStore.getObject(bucketName, objectKey);

        // Certificate as X509Certificate.
        return (X509Certificate)
                CertificateFactory.getInstance("X.509")
                        .generateCertificate(
                                new ByteArrayInputStream(
                                        certificatePem.getBytes(StandardCharsets.UTF_8)));
    }
}
