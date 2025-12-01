package uk.gov.di.mobile.wallet.cri.credential.mdoc.fishing_licence;

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

public class FishingLicenceHandler implements CredentialHandler {

    private final MdocCredentialBuilder<FishingLicenceDocument> mdocCredentialBuilder;
    private final ObjectMapper mapper = new ObjectMapper();

    public FishingLicenceHandler(
            MdocCredentialBuilder<FishingLicenceDocument> mdocCredentialBuilder) {
        this.mdocCredentialBuilder = mdocCredentialBuilder;
    }

    public String buildCredential(
            DocumentStoreRecord documentStoreRecord,
            ProofJwtService.ProofJwtData proofData,
            Optional<StatusListClient.StatusListInformation> statusListInformation)
            throws ObjectStoreException, SigningException, CertificateException {
        FishingLicenceDocument document =
                mapper.convertValue(documentStoreRecord.getData(), FishingLicenceDocument.class);

        return mdocCredentialBuilder.buildCredential(
                document,
                proofData.publicKey(),
                statusListInformation.orElseThrow(),
                documentStoreRecord.getCredentialTtlMinutes());
    }
}
