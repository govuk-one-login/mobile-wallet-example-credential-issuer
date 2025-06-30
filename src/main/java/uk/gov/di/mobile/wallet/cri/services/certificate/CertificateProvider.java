package uk.gov.di.mobile.wallet.cri.services.certificate;

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
        byte[] certificateBytes = getCertificateBytes();
        return parseX509Certificate(certificateBytes);
    }

    public String getCertificateAsString() throws ObjectStoreException {
        byte[] certificateBytes = getCertificateBytes();
        return new String(certificateBytes, StandardCharsets.UTF_8);
    }

    private byte[] getCertificateBytes() throws ObjectStoreException {
        String bucketName = configurationService.getCertificatesBucketName();
        String certificateAuthorityArn = configurationService.getCertificateAuthorityArn();

        String certificateAuthorityId = extractCertificateAuthorityId(certificateAuthorityArn);
        String objectKey = certificateAuthorityId + "/certificate.pem";

        return objectStore.getObject(bucketName, objectKey);
    }

    private X509Certificate parseX509Certificate(byte[] certificateBytes)
            throws CertificateException {
        CertificateFactory cf = CertificateFactory.getInstance("X.509");
        return (X509Certificate) cf.generateCertificate(new ByteArrayInputStream(certificateBytes));
    }
}
