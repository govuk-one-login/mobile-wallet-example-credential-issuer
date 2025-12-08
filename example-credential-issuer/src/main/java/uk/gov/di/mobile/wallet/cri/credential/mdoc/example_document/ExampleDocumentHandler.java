package uk.gov.di.mobile.wallet.cri.credential.mdoc.example_document;

import com.fasterxml.jackson.databind.ObjectMapper;
import uk.gov.di.mobile.wallet.cri.credential.CredentialHandler;
import uk.gov.di.mobile.wallet.cri.credential.DocumentStoreRecord;
import uk.gov.di.mobile.wallet.cri.credential.StatusListClient;
import uk.gov.di.mobile.wallet.cri.credential.mdoc.MdocCredentialBuilder;
import uk.gov.di.mobile.wallet.cri.credential.proof.ProofJwtService;
import uk.gov.di.mobile.wallet.cri.services.object_storage.ObjectStoreException;
import uk.gov.di.mobile.wallet.cri.services.signing.SigningException;

import java.security.cert.CertificateException;
import java.util.Optional;

public class ExampleDocumentHandler implements CredentialHandler {

    private final MdocCredentialBuilder<ExampleDocument> mdocCredentialBuilder;
    private final ObjectMapper mapper = new ObjectMapper();

    public ExampleDocumentHandler(MdocCredentialBuilder<ExampleDocument> mdocCredentialBuilder) {
        this.mdocCredentialBuilder = mdocCredentialBuilder;
    }

    public String buildCredential(
            DocumentStoreRecord documentStoreRecord,
            ProofJwtService.ProofJwtData proofData,
            Optional<StatusListClient.StatusListInformation> statusListInformation)
            throws ObjectStoreException, SigningException, CertificateException {
        ExampleDocument document =
                mapper.convertValue(documentStoreRecord.getData(), ExampleDocument.class);

        return mdocCredentialBuilder.buildCredential(
                document,
                proofData.publicKey(),
                statusListInformation.orElseThrow(),
                documentStoreRecord.getCredentialTtlMinutes());
    }
}
