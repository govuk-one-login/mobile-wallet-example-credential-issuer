package uk.gov.di.mobile.wallet.cri.credential.mobile_driving_licence;

import com.fasterxml.jackson.databind.ObjectMapper;
import uk.gov.di.mobile.wallet.cri.credential.CredentialHandler;
import uk.gov.di.mobile.wallet.cri.credential.Document;
import uk.gov.di.mobile.wallet.cri.credential.ProofJwtService;
import uk.gov.di.mobile.wallet.cri.services.object_storage.ObjectStoreException;
import uk.gov.di.mobile.wallet.cri.services.signing.SigningException;

import java.security.cert.CertificateException;
import java.util.Objects;

import static uk.gov.di.mobile.wallet.cri.credential.CredentialType.MOBILE_DRIVING_LICENCE;

public class MobileDrivingLicenceHandler implements CredentialHandler {

    private final MobileDrivingLicenceBuilder mobileDrivingLicenceBuilder;
    private final ObjectMapper mapper = new ObjectMapper();

    public MobileDrivingLicenceHandler(MobileDrivingLicenceBuilder mobileDrivingLicenceBuilder) {
        this.mobileDrivingLicenceBuilder = mobileDrivingLicenceBuilder;
    }

    @Override
    public boolean supports(String vcType) {
        return Objects.equals(vcType, MOBILE_DRIVING_LICENCE.getType());
    }

    @Override
    public String buildCredential(Document document, ProofJwtService.ProofJwtData proofData)
            throws ObjectStoreException, SigningException, CertificateException {
        DrivingLicenceDocument drivingLicenceDocument =
                mapper.convertValue(document.getData(), DrivingLicenceDocument.class);

        return mobileDrivingLicenceBuilder.createMobileDrivingLicence(
                drivingLicenceDocument, proofData.publicKey());
    }
}
