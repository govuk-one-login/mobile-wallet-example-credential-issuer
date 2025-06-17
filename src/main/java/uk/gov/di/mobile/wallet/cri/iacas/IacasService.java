package uk.gov.di.mobile.wallet.cri.iacas;

import uk.gov.di.mobile.wallet.cri.services.ConfigurationService;
import uk.gov.di.mobile.wallet.cri.services.object_storage.ObjectStore;

import java.util.List;

import static uk.gov.di.mobile.wallet.cri.util.ArnUtil.extractCertificateId;

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

    public Iacas getIacas() throws Exception {
        String bucketName = configurationService.getCertificatesBucketName();
        String certificateAuthorityArn = configurationService.getCertificateAuthorityArn();

        String rootCertificateId = extractCertificateId(certificateAuthorityArn);
        String objectKey = rootCertificateId + "/certificate.pem";
        String certificatePem = objectStore.getObject(bucketName, objectKey);

        Iaca iaca = Iaca.fromCertificate(rootCertificateId, true, certificatePem);

        return new Iacas(List.of(iaca));
    }
}
