package uk.gov.di.mobile.wallet.cri.credential.mobile_driving_licence;

import com.fasterxml.jackson.databind.ObjectMapper;
import uk.gov.di.mobile.wallet.cri.credential.BuildCredentialResult;
import uk.gov.di.mobile.wallet.cri.credential.CredentialHandler;
import uk.gov.di.mobile.wallet.cri.credential.Document;
import uk.gov.di.mobile.wallet.cri.credential.ProofJwtService;
import uk.gov.di.mobile.wallet.cri.credential.StatusList;
import uk.gov.di.mobile.wallet.cri.services.object_storage.ObjectStoreException;
import uk.gov.di.mobile.wallet.cri.services.signing.SigningException;

import java.security.cert.CertificateException;
import java.util.Optional;

public class MobileDrivingLicenceHandler implements CredentialHandler {

    private final MobileDrivingLicenceBuilder mobileDrivingLicenceBuilder;
    private final ObjectMapper mapper = new ObjectMapper();

    public MobileDrivingLicenceHandler(MobileDrivingLicenceBuilder mobileDrivingLicenceBuilder) {
        this.mobileDrivingLicenceBuilder = mobileDrivingLicenceBuilder;
    }

    public BuildCredentialResult buildCredential(
            Document document,
            ProofJwtService.ProofJwtData proofData,
            Optional<StatusList> statusList)
            throws ObjectStoreException, SigningException, CertificateException {
        DrivingLicenceDocument drivingLicenceDocument =
                mapper.convertValue(document.getData(), DrivingLicenceDocument.class);

        String credential =
                mobileDrivingLicenceBuilder.createMobileDrivingLicence(
                        drivingLicenceDocument,
                        proofData.publicKey(),
                        statusList.orElseThrow().getIdx(),
                        statusList.orElseThrow().getUri());

        return new BuildCredentialResult(credential, drivingLicenceDocument.getDocumentNumber());
    }
}
