package uk.gov.di.mobile.wallet.cri.credential.mdoc.simple_mdoc;

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

public class SimpleMdocHandler implements CredentialHandler {

    private final MdocCredentialBuilder<SimpleDocument> mdocBuilder;
    private final ObjectMapper mapper = new ObjectMapper();

    public SimpleMdocHandler(MdocCredentialBuilder<SimpleDocument> mdocBuilder) {
        this.mdocBuilder = mdocBuilder;
    }

    public String buildCredential(
            DocumentStoreRecord documentStoreRecord,
            ProofJwtService.ProofJwtData proofData,
            Optional<StatusListClient.StatusListInformation> statusListInformation)
            throws ObjectStoreException, SigningException, CertificateException {
        SimpleDocument document =
                mapper.convertValue(documentStoreRecord.getData(), SimpleDocument.class);

        return mdocBuilder.buildCredential(
                document,
                proofData.publicKey(),
                statusListInformation.orElseThrow(),
                documentStoreRecord.getCredentialTtlMinutes());
    }
}
