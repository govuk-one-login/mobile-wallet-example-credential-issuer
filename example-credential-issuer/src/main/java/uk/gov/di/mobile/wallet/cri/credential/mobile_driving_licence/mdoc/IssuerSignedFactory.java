package uk.gov.di.mobile.wallet.cri.credential.mobile_driving_licence.mdoc;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
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

    private static final Logger LOGGER = LoggerFactory.getLogger(IssuerSignedFactory.class);

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
            Namespaces namespaces, ECPublicKey publicKey, long credentialTtlMinutes)
            throws MDLException, SigningException, CertificateException, ObjectStoreException {
        MobileSecurityObject mobileSecurityObject =
                mobileSecurityObjectFactory.build(namespaces, publicKey, credentialTtlMinutes);
        byte[] mobileSecurityObjectBytes = cborEncoder.encode(mobileSecurityObject);

        String certificateId = ArnUtil.extractKeyId(documentSigningKey1Arn);

        X509Certificate certificate;

        try {
            certificate = certificateProvider.getSigningCertificate(certificateId);
            LOGGER.info("FETCHED certificate: {}", certificateId);

        } catch (Exception e) {
            LOGGER.error("FETCHING certificate failed certificate: {}", certificateId);
            throw e;
        }

        try {
            COSESign1 sign1 = coseSigner.sign(mobileSecurityObjectBytes, certificate);
            LOGGER.info("ISSUER_SIGNED_SUCCESS: {}", certificateId);
            return new IssuerSigned(namespaces.namespaces(), sign1);

        } catch (Exception e) {
            LOGGER.error("ISSUER_SIGNED_FAILED: {}", certificateId);
            throw e;
        }
    }
}
