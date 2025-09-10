package uk.gov.di.mobile.wallet.cri.credential.mobile_driving_licence;

import uk.gov.di.mobile.wallet.cri.credential.mobile_driving_licence.cbor.CBOREncoder;
import uk.gov.di.mobile.wallet.cri.credential.mobile_driving_licence.mdoc.IssuerSigned;
import uk.gov.di.mobile.wallet.cri.credential.mobile_driving_licence.mdoc.IssuerSignedFactory;
import uk.gov.di.mobile.wallet.cri.credential.mobile_driving_licence.mdoc.Namespaces;
import uk.gov.di.mobile.wallet.cri.credential.mobile_driving_licence.mdoc.NamespacesFactory;
import uk.gov.di.mobile.wallet.cri.services.object_storage.ObjectStoreException;
import uk.gov.di.mobile.wallet.cri.services.signing.SigningException;

import java.security.cert.CertificateException;
import java.security.interfaces.ECPublicKey;
import java.util.Base64;

/**
 * Class responsible for creating the {@code IssuerSigned} structure of Mobile Driving Licences
 * (mDLs).
 */
public class MobileDrivingLicenceBuilder {

    private final CBOREncoder cborEncoder;
    private final NamespacesFactory namespacesFactory;
    private final IssuerSignedFactory issuerSignedFactory;

    /**
     * Constructs a new MobileDrivingLicenceBuilder.
     *
     * @param cborEncoder CBOR encoder for encoding objects into CBOR binary representation
     * @param namespacesFactory Factory for creating {@link Namespaces} from a driving licence
     *     document
     * @param issuerSignedFactory Factory for creating {@link IssuerSigned}
     */
    public MobileDrivingLicenceBuilder(
            CBOREncoder cborEncoder,
            NamespacesFactory namespacesFactory,
            IssuerSignedFactory issuerSignedFactory) {
        this.cborEncoder = cborEncoder;
        this.namespacesFactory = namespacesFactory;
        this.issuerSignedFactory = issuerSignedFactory;
    }

    /**
     * Creates an {@link IssuerSigned} structure in Base64URL-encoded CBOR format.
     *
     * @param drivingLicenceDocument The driving licence data to serialise and sign
     * @return A Base64URL-encoded string containing the CBOR-encoded {@code IssuerSigned} structure
     */
    public String createMobileDrivingLicence(
            DrivingLicenceDocument drivingLicenceDocument, ECPublicKey publicKey)
            throws ObjectStoreException, SigningException, CertificateException {
        Namespaces namespaces = namespacesFactory.build(drivingLicenceDocument);
        IssuerSigned issuerSigned = issuerSignedFactory.build(namespaces, publicKey);
        byte[] cborEncodedMobileDrivingLicence = cborEncoder.encode(issuerSigned);
        return Base64.getUrlEncoder()
                .withoutPadding()
                .encodeToString(cborEncodedMobileDrivingLicence);
    }
}
