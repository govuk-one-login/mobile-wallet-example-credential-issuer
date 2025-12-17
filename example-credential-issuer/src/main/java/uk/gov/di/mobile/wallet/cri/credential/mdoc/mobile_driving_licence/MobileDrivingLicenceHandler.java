package uk.gov.di.mobile.wallet.cri.credential.mdoc.mobile_driving_licence;

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

public class MobileDrivingLicenceHandler implements CredentialHandler {

    private final MdocCredentialBuilder<DrivingLicenceDocument> mdocCredentialBuilder;
    private final ObjectMapper mapper = new ObjectMapper();

    public MobileDrivingLicenceHandler(
            MdocCredentialBuilder<DrivingLicenceDocument> mdocCredentialBuilder) {
        this.mdocCredentialBuilder = mdocCredentialBuilder;
    }

    public String buildCredential(
            DocumentStoreRecord documentStoreRecord,
            ProofJwtService.ProofJwtData proofData,
            Optional<StatusListClient.StatusListInformation> statusListInformation)
            throws ObjectStoreException, SigningException, CertificateException {
        DrivingLicenceDocument document =
                mapper.convertValue(documentStoreRecord.getData(), DrivingLicenceDocument.class);

        return mdocCredentialBuilder.buildCredential(
                document,
                proofData.publicKey(),
                statusListInformation.orElseThrow(),
                documentStoreRecord.getCredentialTtlMinutes());
    }
}
