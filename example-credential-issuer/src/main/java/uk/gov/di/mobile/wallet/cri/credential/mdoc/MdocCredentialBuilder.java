package uk.gov.di.mobile.wallet.cri.credential.mdoc;

import uk.gov.di.mobile.wallet.cri.credential.StatusListClient;
import uk.gov.di.mobile.wallet.cri.credential.mdoc.cbor.CBOREncoder;
import uk.gov.di.mobile.wallet.cri.services.object_storage.ObjectStoreException;
import uk.gov.di.mobile.wallet.cri.services.signing.SigningException;

import java.security.cert.CertificateException;
import java.security.interfaces.ECPublicKey;
import java.util.Base64;

/** Class responsible for creating the {@code IssuerSigned} structure of mDocs. */
public class MdocCredentialBuilder<T> {

    private final CBOREncoder cborEncoder;
    private final NamespacesFactory<T> namespacesFactory;
    private final IssuerSignedFactory issuerSignedFactory;

    /**
     * Constructs a new CredentialBuilder.
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
     * Creates an {@link IssuerSigned} structure in Base64URL-encoded CBOR format.
     *
     * @param document The document to serialise and sign
     * @return A Base64URL-encoded string containing the CBOR-encoded {@code IssuerSigned} structure
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
