package uk.gov.di.mobile.wallet.cri.iacas;

import com.nimbusds.jose.JOSEException;
import uk.gov.di.mobile.wallet.cri.services.ConfigurationService;
import uk.gov.di.mobile.wallet.cri.services.object_storage.ObjectStore;
import uk.gov.di.mobile.wallet.cri.services.object_storage.ObjectStoreException;

import java.security.NoSuchAlgorithmException;
import java.security.cert.CertificateEncodingException;
import java.util.List;

import static uk.gov.di.mobile.wallet.cri.util.ArnUtil.extractCertificateAuthorityId;

public class IacasService {

    private final ConfigurationService configurationService;
    private final ObjectStore objectStore;

    /**
     * Constructs the IacasService with required dependencies.
     *
     * @param configurationService Provides configuration values.
     * @param objectStore Provides access to the object storage.
     */
    public IacasService(ConfigurationService configurationService, ObjectStore objectStore) {
        this.configurationService = configurationService;
        this.objectStore = objectStore;
    }

    public Iacas getIacas()
            throws ObjectStoreException,
                    CertificateEncodingException,
                    NoSuchAlgorithmException,
                    JOSEException {
        String bucketName = configurationService.getCertificatesBucketName();
        String certificateAuthorityArn = configurationService.getCertificateAuthorityArn();

        String certificateAuthorityId = extractCertificateAuthorityId(certificateAuthorityArn);
        String objectKey = certificateAuthorityId + "/certificate.pem";
        String certificatePem = objectStore.getObject(bucketName, objectKey);

        Iaca iaca = Iaca.fromCertificate(certificateAuthorityId, true, certificatePem);

        return new Iacas(List.of(iaca));
    }
}
