package uk.gov.di.mobile.wallet.cri.credential.mobile_driving_licence.mdoc;

import uk.gov.di.mobile.wallet.cri.credential.mobile_driving_licence.MDLException;
import uk.gov.di.mobile.wallet.cri.credential.mobile_driving_licence.cbor.CBOREncoder;
import uk.gov.di.mobile.wallet.cri.credential.mobile_driving_licence.cose.COSESign1;
import uk.gov.di.mobile.wallet.cri.credential.mobile_driving_licence.cose.COSESigner;
import uk.gov.di.mobile.wallet.cri.services.certificate.CertificateProvider;
import uk.gov.di.mobile.wallet.cri.services.object_storage.ObjectStoreException;
import uk.gov.di.mobile.wallet.cri.services.signing.SigningException;
import uk.gov.di.mobile.wallet.cri.util.ArnUtil;

import java.security.cert.CertificateException;
import java.security.cert.X509Certificate;
import java.security.interfaces.ECPublicKey;

public class IssuerSignedFactory {
    private final MobileSecurityObjectFactory mobileSecurityObjectFactory;
    private final CBOREncoder cborEncoder;
    private final COSESigner coseSigner;
    private final CertificateProvider certificateProvider;
    private final String documentSigningKey1Arn;

    public IssuerSignedFactory(
            MobileSecurityObjectFactory mobileSecurityObjectFactory,
            CBOREncoder cborEncoder,
            COSESigner coseSigner,
            CertificateProvider certificateProvider,
            String documentSigningKey1Arn) {
        this.mobileSecurityObjectFactory = mobileSecurityObjectFactory;
        this.cborEncoder = cborEncoder;
        this.coseSigner = coseSigner;
        this.certificateProvider = certificateProvider;
        this.documentSigningKey1Arn = documentSigningKey1Arn;
    }

    public IssuerSigned build(
            Namespaces namespaces, ECPublicKey publicKey, int statusListIndex, String statusListUri)
            throws MDLException, SigningException, CertificateException, ObjectStoreException {
        MobileSecurityObject mobileSecurityObject =
                mobileSecurityObjectFactory.build(
                        namespaces, publicKey, statusListIndex, statusListUri);
        byte[] mobileSecurityObjectBytes = cborEncoder.encode(mobileSecurityObject);

        String certificateId = ArnUtil.extractKeyId(documentSigningKey1Arn);

        X509Certificate certificate = certificateProvider.getSigningCertificate(certificateId);
        COSESign1 sign1 = coseSigner.sign(mobileSecurityObjectBytes, certificate);

        return new IssuerSigned(namespaces.namespaces(), sign1);
    }
}
