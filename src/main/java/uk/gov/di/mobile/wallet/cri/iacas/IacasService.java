package uk.gov.di.mobile.wallet.cri.iacas;

import com.nimbusds.jose.JOSEException;
import uk.gov.di.mobile.wallet.cri.services.ConfigurationService;
import uk.gov.di.mobile.wallet.cri.services.certificate.CertificateProvider;
import uk.gov.di.mobile.wallet.cri.services.object_storage.ObjectStoreException;

import java.security.NoSuchAlgorithmException;
import java.security.cert.CertificateEncodingException;
import java.util.List;

import static uk.gov.di.mobile.wallet.cri.util.ArnUtil.extractCertificateAuthorityId;

public class IacasService {

    private final ConfigurationService configurationService;
    private final CertificateProvider certificateProvider;

    /**
     * Constructs the IacasService with required dependencies.
     *
     * @param configurationService Provides configuration values.
     * @param certificateProvider Provides access to the object storage.
     */
    public IacasService(
            ConfigurationService configurationService, CertificateProvider certificateProvider) {
        this.configurationService = configurationService;
        this.certificateProvider = certificateProvider;
    }

    public Iacas getIacas()
            throws ObjectStoreException,
                    CertificateEncodingException,
                    NoSuchAlgorithmException,
                    JOSEException {
        String certificateAuthorityArn = configurationService.getCertificateAuthorityArn();
        String certificateAuthorityId = extractCertificateAuthorityId(certificateAuthorityArn);
        String certificatePem = certificateProvider.getCertificateAsString();

        Iaca iaca = Iaca.fromCertificate(certificateAuthorityId, true, certificatePem);

        return new Iacas(List.of(iaca));
    }
}
