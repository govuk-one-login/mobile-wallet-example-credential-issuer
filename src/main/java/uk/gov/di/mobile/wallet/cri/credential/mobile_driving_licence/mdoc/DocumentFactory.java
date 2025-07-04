package uk.gov.di.mobile.wallet.cri.credential.mobile_driving_licence.mdoc;

import uk.gov.di.mobile.wallet.cri.credential.mobile_driving_licence.DrivingLicenceDocument;
import uk.gov.di.mobile.wallet.cri.credential.mobile_driving_licence.mdoc.constants.DocumentTypes;
import uk.gov.di.mobile.wallet.cri.services.object_storage.ObjectStoreException;
import uk.gov.di.mobile.wallet.cri.services.signing.SigningException;

import java.security.cert.CertificateException;

public class DocumentFactory {
    private static final String DOC_TYPE = DocumentTypes.MDL;

    private final NamespacesFactory namespacesFactory;
    private final IssuerSignedFactory issuerSignedFactory;

    public DocumentFactory(
            NamespacesFactory namespacesFactory, IssuerSignedFactory issuerSignedFactory) {
        this.namespacesFactory = namespacesFactory;
        this.issuerSignedFactory = issuerSignedFactory;
    }

    public Document build(final DrivingLicenceDocument drivingLicence)
            throws ObjectStoreException, SigningException, CertificateException {
        Namespaces namespaces = namespacesFactory.build(drivingLicence);
        IssuerSigned issuerSigned = issuerSignedFactory.build(namespaces);
        return new Document(DOC_TYPE, issuerSigned);
    }
}
