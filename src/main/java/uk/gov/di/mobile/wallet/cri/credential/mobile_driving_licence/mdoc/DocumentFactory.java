package uk.gov.di.mobile.wallet.cri.credential.mobile_driving_licence.mdoc;

import uk.gov.di.mobile.wallet.cri.credential.mobile_driving_licence.cbor.MDLException;

import java.util.List;
import java.util.Map;

public class DocumentFactory {
    private static final String MOBILE_DRIVING_LICENCE_DOCUMENT_TYPE = "org.iso.18013.5.1.mDL";

    public DocumentFactory() {}

    public Document build(final Map<String, List<byte[]>> nameSpaces) throws MDLException {
        IssuerSigned issuerSigned = buildIssuerSigned(nameSpaces);
        return new Document(MOBILE_DRIVING_LICENCE_DOCUMENT_TYPE, issuerSigned);
    }

    private IssuerSigned buildIssuerSigned(final Map<String, List<byte[]>> nameSpaces) {
        return new IssuerSigned(nameSpaces, new IssuerAuth());
    }
}
