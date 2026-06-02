package uk.gov.di.mobile.wallet.cri.credential.mdoc;

import uk.gov.di.mobile.wallet.cri.credential.StatusListClient;
import uk.gov.di.mobile.wallet.cri.credential.mdoc.cbor.CBOREncoder;
import uk.gov.di.mobile.wallet.cri.services.object_storage.ObjectStoreException;
import uk.gov.di.mobile.wallet.cri.services.signing.SigningException;

import java.security.cert.CertificateException;
import java.security.interfaces.ECPublicKey;
import java.util.Base64;
import java.util.Optional;

/**
 * Builds an {@link IssuerSigned} mDoc structure from the given document and returns it as
 * Base64URL-encoded CBOR.
 */
public class MdocCredentialBuilder<T> {

    private final CBOREncoder cborEncoder;
    private final NamespacesFactory<T> namespacesFactory;
    private final IssuerSignedFactory issuerSignedFactory;
    private final String docType;

    /**
     * Constructs a new {@link MdocCredentialBuilder}.
     *
     * @param cborEncoder CBOR encoder for encoding objects into CBOR binary representation
     * @param namespacesFactory Factory for creating {@link Namespaces} from a document
     * @param issuerSignedFactory Factory for creating {@link IssuerSigned}
     * @param docType Document type for the {@link MobileSecurityObject}
     */
    public MdocCredentialBuilder(
            CBOREncoder cborEncoder,
            NamespacesFactory<T> namespacesFactory,
            IssuerSignedFactory issuerSignedFactory,
            String docType) {
        this.cborEncoder = cborEncoder;
        this.namespacesFactory = namespacesFactory;
        this.issuerSignedFactory = issuerSignedFactory;
        this.docType = docType;
    }

    /**
     * Creates an {@link IssuerSigned} structure without an expected update in the MSO ValidityInfo.
     * Used by credential types that do not support the expectedUpdate concept (e.g. SimpleMdoc).
     *
     * @param document Typed credential document
     * @param publicKey Device public key
     * @param statusListInformation Status list information (URI and index)
     * @param credentialTtlSeconds Credential validity period in seconds
     * @return Base64URL string of the CBOR-encoded {@link IssuerSigned}
     * @throws ObjectStoreException When persistence interactions fail
     * @throws SigningException When signing fails
     * @throws CertificateException When certificate material cannot be processed
     */
    public String buildCredential(
            T document,
            ECPublicKey publicKey,
            StatusListClient.StatusListInformation statusListInformation,
            long credentialTtlSeconds)
            throws ObjectStoreException, SigningException, CertificateException {
        return buildCredential(
                document, publicKey, statusListInformation, credentialTtlSeconds, Optional.empty());
    }

    /**
     * Creates an {@link IssuerSigned} structure with an optional expected update in the MSO
     * ValidityInfo and returns it as Base64URL-encoded CBOR. Used by credential types that support
     * expectedUpdate (e.g. MobileDrivingLicence).
     *
     * @param document Typed credential document
     * @param publicKey Device public key
     * @param statusListInformation Status list information (URI and index)
     * @param credentialTtlSeconds Credential validity period in seconds
     * @param expectedUpdateSeconds Optional duration in seconds from issuance when the credential
     *     is expected to be updated. When present, sets the {@code expectedUpdate} field in the MSO
     *     ValidityInfo.
     * @return Base64URL string of the CBOR-encoded {@link IssuerSigned}
     * @throws ObjectStoreException When persistence interactions fail
     * @throws SigningException When signing fails
     * @throws CertificateException When certificate material cannot be processed
     */
    public String buildCredential(
            T document,
            ECPublicKey publicKey,
            StatusListClient.StatusListInformation statusListInformation,
            long credentialTtlSeconds,
            Optional<Long> expectedUpdateSeconds)
            throws ObjectStoreException, SigningException, CertificateException {
        Namespaces namespaces = namespacesFactory.build(document);
        IssuerSigned issuerSigned =
                issuerSignedFactory.build(
                        namespaces,
                        publicKey,
                        statusListInformation,
                        credentialTtlSeconds,
                        expectedUpdateSeconds,
                        docType);
        byte[] cborEncodedMobileDrivingLicence = cborEncoder.encode(issuerSigned);
        return Base64.getUrlEncoder()
                .withoutPadding()
                .encodeToString(cborEncodedMobileDrivingLicence);
    }
}
