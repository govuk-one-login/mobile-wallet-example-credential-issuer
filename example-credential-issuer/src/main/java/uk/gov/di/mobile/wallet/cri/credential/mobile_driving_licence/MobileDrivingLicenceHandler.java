package uk.gov.di.mobile.wallet.cri.credential.mobile_driving_licence;

import com.fasterxml.jackson.databind.ObjectMapper;
import uk.gov.di.mobile.wallet.cri.credential.BuildCredentialResult;
import uk.gov.di.mobile.wallet.cri.credential.CredentialBuildContext;
import uk.gov.di.mobile.wallet.cri.credential.CredentialHandler;
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
    public BuildCredentialResult buildCredential(CredentialBuildContext context)
            throws ObjectStoreException, SigningException, CertificateException {

        if (context.getStatusListIndex() == null || context.getStatusListUri() == null) {
            throw new IllegalArgumentException("StatusList parameters required for mDL");
        }

        DrivingLicenceDocument drivingLicenceDocument =
                mapper.convertValue(context.getDocument().getData(), DrivingLicenceDocument.class);

        String credential =
                mobileDrivingLicenceBuilder.createMobileDrivingLicence(
                        drivingLicenceDocument,
                        context.getProofData().publicKey(),
                        context.getStatusListIndex(),
                        context.getStatusListUri());

        return new BuildCredentialResult(credential, drivingLicenceDocument.getDocumentNumber());
    }
}
