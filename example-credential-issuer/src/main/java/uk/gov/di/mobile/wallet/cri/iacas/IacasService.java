package uk.gov.di.mobile.wallet.cri.iacas;

import com.nimbusds.jose.JOSEException;
import uk.gov.di.mobile.wallet.cri.services.certificate.CertificateProvider;
import uk.gov.di.mobile.wallet.cri.services.object_storage.ObjectStoreException;

import java.security.NoSuchAlgorithmException;
import java.security.cert.CertificateEncodingException;
import java.util.List;

import static uk.gov.di.mobile.wallet.cri.util.ArnUtil.extractCertificateAuthorityId;

public class IacasService {

    private final CertificateProvider certificateProvider;
    private final String certificateAuthorityArn;

    /**
     * Constructs the IacasService with required dependencies.
     *
     * @param certificateProvider Provides access to the object storage.
     * @param certificateAuthorityArn The certificate authority ARN.
     */
    public IacasService(CertificateProvider certificateProvider, String certificateAuthorityArn) {
        this.certificateProvider = certificateProvider;
        this.certificateAuthorityArn = certificateAuthorityArn;
    }

    public Iacas getIacas()
            throws ObjectStoreException,
                    CertificateEncodingException,
                    NoSuchAlgorithmException,
                    JOSEException {
        String certificateAuthorityId = extractCertificateAuthorityId(certificateAuthorityArn);
        String certificatePem = certificateProvider.getRootCertificate(certificateAuthorityId);

        Iaca iaca = Iaca.fromCertificate(certificateAuthorityId, true, certificatePem);

        return new Iacas(List.of(iaca));
    }
}
