package uk.gov.di.mobile.wallet.cri.services.certificate;

import uk.gov.di.mobile.wallet.cri.services.object_storage.ObjectStore;
import uk.gov.di.mobile.wallet.cri.services.object_storage.ObjectStoreException;

import java.io.ByteArrayInputStream;
import java.nio.charset.StandardCharsets;
import java.security.cert.CertificateException;
import java.security.cert.CertificateFactory;
import java.security.cert.X509Certificate;

public class CertificateProvider {

    private static final String SIGN_CERT_PATH = "sign/";
    private static final String ROOT_CERT_PATH = "root/";
    private static final String CERTIFICATE_FILE_SUFFIX = "/certificate.pem";
    private static final String CERTIFICATE_TYPE = "X.509";

    private final ObjectStore objectStore;
    private final String bucketName;

    public CertificateProvider(ObjectStore objectStore, String bucketName) {
        this.objectStore = objectStore;
        this.bucketName = bucketName;
    }

    public X509Certificate getSigningCertificate(String certificateId)
            throws CertificateException, ObjectStoreException {
        byte[] certificateBytes = getCertificateBytes(certificateId, SIGN_CERT_PATH);
        return parseX509Certificate(certificateBytes);
    }

    public String getRootCertificate(String certificateId) throws ObjectStoreException {
        byte[] certificateBytes = getCertificateBytes(certificateId, ROOT_CERT_PATH);
        return new String(certificateBytes, StandardCharsets.UTF_8);
    }

    private byte[] getCertificateBytes(String certificateId, String path)
            throws ObjectStoreException {
        String objectKey = path + certificateId + CERTIFICATE_FILE_SUFFIX;

        return objectStore.getObject(bucketName, objectKey);
    }

    private X509Certificate parseX509Certificate(byte[] certificateBytes)
            throws CertificateException {
        CertificateFactory cf = CertificateFactory.getInstance(CERTIFICATE_TYPE);
        return (X509Certificate) cf.generateCertificate(new ByteArrayInputStream(certificateBytes));
    }
}
