package uk.gov.di.mobile.wallet.cri.credential.mdoc.mobile_driving_licence;

import uk.gov.di.mobile.wallet.cri.credential.StatusListClient;
import uk.gov.di.mobile.wallet.cri.credential.mdoc.IssuerSigned;
import uk.gov.di.mobile.wallet.cri.credential.mdoc.IssuerSignedFactory;
import uk.gov.di.mobile.wallet.cri.credential.mdoc.Namespaces;
import uk.gov.di.mobile.wallet.cri.credential.mdoc.NamespacesFactory;
import uk.gov.di.mobile.wallet.cri.credential.mdoc.cbor.CBOREncoder;
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
     * @param document The driving licence data to serialise and sign
     * @return A Base64URL-encoded string containing the CBOR-encoded {@code IssuerSigned} structure
     */
    public String createMobileDrivingLicence(
            DrivingLicenceDocument document,
            ECPublicKey publicKey,
            StatusListClient.StatusListInformation statusListInformation,
            long credentialTtlMinutes)
            throws ObjectStoreException, SigningException, CertificateException {
        Namespaces namespaces = namespacesFactory.build(document);
        IssuerSigned issuerSigned =
                issuerSignedFactory.build(
                        namespaces, publicKey, statusListInformation, credentialTtlMinutes);
        byte[] cborEncodedMobileDrivingLicence = cborEncoder.encode(issuerSigned);
        return Base64.getUrlEncoder()
                .withoutPadding()
                .encodeToString(cborEncodedMobileDrivingLicence);
    }
}
