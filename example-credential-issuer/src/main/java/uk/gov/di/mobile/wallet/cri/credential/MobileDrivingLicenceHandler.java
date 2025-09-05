package uk.gov.di.mobile.wallet.cri.credential;

import com.fasterxml.jackson.databind.ObjectMapper;
import uk.gov.di.mobile.wallet.cri.credential.mobile_driving_licence.DrivingLicenceDocument;
import uk.gov.di.mobile.wallet.cri.credential.mobile_driving_licence.MobileDrivingLicenceService;

import java.util.Objects;

import static uk.gov.di.mobile.wallet.cri.credential.CredentialType.MOBILE_DRIVING_LICENCE;

public class MobileDrivingLicenceHandler implements CredentialHandler {

    private final MobileDrivingLicenceService mobileDrivingLicenceService;
    private final ObjectMapper mapper = new ObjectMapper();

    public MobileDrivingLicenceHandler(MobileDrivingLicenceService mobileDrivingLicenceService) {
        this.mobileDrivingLicenceService = mobileDrivingLicenceService;
    }

    @Override
    public boolean supports(String vcType) {
        return Objects.equals(vcType, MOBILE_DRIVING_LICENCE.getType());
    }

    @Override
    public String buildCredential(Document document, ProofJwtService.ProofJwtData proofData)
            throws Exception {
        DrivingLicenceDocument drivingLicenceDocument =
                mapper.convertValue(document.getData(), DrivingLicenceDocument.class);

        return mobileDrivingLicenceService.createMobileDrivingLicence(
                drivingLicenceDocument, proofData.publicKey());
    }
}
