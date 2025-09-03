package uk.gov.di.mobile.wallet.cri.services.certificate;

import uk.gov.di.mobile.wallet.cri.services.object_storage.ObjectStore;
import uk.gov.di.mobile.wallet.cri.services.object_storage.ObjectStoreException;

import java.io.ByteArrayInputStream;
import java.nio.charset.StandardCharsets;
import java.security.cert.CertificateException;
import java.security.cert.CertificateFactory;
import java.security.cert.X509Certificate;

public class CertificateProvider {

    private final ObjectStore objectStore;
    private final String bucketName;

    public CertificateProvider(ObjectStore objectStore, String bucketName) {
        this.objectStore = objectStore;
        this.bucketName = bucketName;
    }

    public X509Certificate getCertificate(String certificateId)
            throws CertificateException, ObjectStoreException {
        byte[] certificateBytes = getCertificateBytes(certificateId);
        return parseX509Certificate(certificateBytes);
    }

    public String getCertificateAsString(String certificateId) throws ObjectStoreException {
        byte[] certificateBytes = getCertificateBytes(certificateId);
        return new String(certificateBytes, StandardCharsets.UTF_8);
    }

    private byte[] getCertificateBytes(String certificateId) throws ObjectStoreException {
        String objectKey = "root/" + certificateId + "/certificate.pem";

        return objectStore.getObject(bucketName, objectKey);
    }

    private X509Certificate parseX509Certificate(byte[] certificateBytes)
            throws CertificateException {
        CertificateFactory cf = CertificateFactory.getInstance("X.509");
        return (X509Certificate) cf.generateCertificate(new ByteArrayInputStream(certificateBytes));
    }
}
