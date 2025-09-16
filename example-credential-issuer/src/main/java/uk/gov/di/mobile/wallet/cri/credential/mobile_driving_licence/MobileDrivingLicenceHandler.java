package uk.gov.di.mobile.wallet.cri.credential.mobile_driving_licence;

import com.fasterxml.jackson.databind.ObjectMapper;
import uk.gov.di.mobile.wallet.cri.credential.BuildCredentialResult;
import uk.gov.di.mobile.wallet.cri.credential.CredentialHandler;
import uk.gov.di.mobile.wallet.cri.credential.Document;
import uk.gov.di.mobile.wallet.cri.credential.ProofJwtService;
import uk.gov.di.mobile.wallet.cri.services.object_storage.ObjectStoreException;
import uk.gov.di.mobile.wallet.cri.services.signing.SigningException;

import java.security.cert.CertificateException;

public class MobileDrivingLicenceHandler implements CredentialHandler {

    private final MobileDrivingLicenceBuilder mobileDrivingLicenceBuilder;
    private final ObjectMapper mapper = new ObjectMapper();

    public MobileDrivingLicenceHandler(MobileDrivingLicenceBuilder mobileDrivingLicenceBuilder) {
        this.mobileDrivingLicenceBuilder = mobileDrivingLicenceBuilder;
    }

    @Override
    public BuildCredentialResult buildCredential(
            Document document, ProofJwtService.ProofJwtData proofData)
            throws ObjectStoreException, SigningException, CertificateException {
        throw new UnsupportedOperationException(
                "Use the method that accepts idx and uri for MobileDrivingLicence");
    }

    public BuildCredentialResult buildCredential(
            Document document,
            ProofJwtService.ProofJwtData proofData,
            int statusListIndex,
            String statusListUri)
            throws ObjectStoreException, SigningException, CertificateException {
        DrivingLicenceDocument drivingLicenceDocument =
                mapper.convertValue(document.getData(), DrivingLicenceDocument.class);

        String credential =
                mobileDrivingLicenceBuilder.createMobileDrivingLicence(
                        drivingLicenceDocument,
                        proofData.publicKey(),
                        statusListIndex,
                        statusListUri);

        return new BuildCredentialResult(credential, drivingLicenceDocument.getDocumentNumber());
    }
}
