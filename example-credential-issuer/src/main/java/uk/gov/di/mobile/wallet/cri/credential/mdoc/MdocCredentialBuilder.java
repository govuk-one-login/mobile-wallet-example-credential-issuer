package uk.gov.di.mobile.wallet.cri.credential.mdoc;

import uk.gov.di.mobile.wallet.cri.credential.StatusListClient;
import uk.gov.di.mobile.wallet.cri.credential.mdoc.cbor.CBOREncoder;
import uk.gov.di.mobile.wallet.cri.services.object_storage.ObjectStoreException;
import uk.gov.di.mobile.wallet.cri.services.signing.SigningException;

import java.security.cert.CertificateException;
import java.security.interfaces.ECPublicKey;
import java.util.Base64;

/**
 * Builds an {@link IssuerSigned} mDoc structure from the given document and returns it as
 * Base64URL-encoded CBOR.
 */
public class MdocCredentialBuilder<T> {

    private final CBOREncoder cborEncoder;
    private final NamespacesFactory<T> namespacesFactory;
    private final IssuerSignedFactory issuerSignedFactory;

    /**
     * Constructs a new {@link MdocCredentialBuilder}.
     *
     * @param cborEncoder CBOR encoder for encoding objects into CBOR binary representation
     * @param namespacesFactory Factory for creating {@link Namespaces} from a document
     * @param issuerSignedFactory Factory for creating {@link IssuerSigned}
     */
    public MdocCredentialBuilder(
            CBOREncoder cborEncoder,
            NamespacesFactory<T> namespacesFactory,
            IssuerSignedFactory issuerSignedFactory) {
        this.cborEncoder = cborEncoder;
        this.namespacesFactory = namespacesFactory;
        this.issuerSignedFactory = issuerSignedFactory;
    }

    /**
     * Creates an {@link IssuerSigned} structure and returns it as Base64URL-encoded CBOR.
     *
     * <ul>
     *   <li>Uses {@link NamespacesFactory} to derive namespaces from the document;
     *   <li>Builds {@link IssuerSigned} via {@link IssuerSignedFactory};
     *   <li>CBOR-encodes the result with {@link CBOREncoder} and Base64URL-encodes it.
     * </ul>
     *
     * @param document Typed credential document
     * @param publicKey Device public key
     * @param statusListInformation Status list information (URI and index)
     * @param credentialTtlMinutes Credential validity period in minutes
     * @return Base64URL string of the CBOR-encoded {@link IssuerSigned}
     * @throws ObjectStoreException When persistence interactions fail
     * @throws SigningException When signing fails
     * @throws CertificateException When certificate material cannot be processed
     */
    public String buildCredential(
            T document,
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
