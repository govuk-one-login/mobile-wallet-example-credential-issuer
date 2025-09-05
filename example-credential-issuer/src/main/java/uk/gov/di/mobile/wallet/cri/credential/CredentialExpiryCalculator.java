package uk.gov.di.mobile.wallet.cri.credential;

import com.fasterxml.jackson.databind.ObjectMapper;
import uk.gov.di.mobile.wallet.cri.credential.basic_check_credential.BasicCheckDocument;
import uk.gov.di.mobile.wallet.cri.credential.digital_veteran_card.VeteranCardDocument;
import uk.gov.di.mobile.wallet.cri.credential.mobile_driving_licence.DrivingLicenceDocument;
import uk.gov.di.mobile.wallet.cri.credential.social_security_credential.SocialSecurityDocument;
import uk.gov.di.mobile.wallet.cri.util.ExpiryUtil;

public class CredentialExpiryCalculator implements ExpiryCalculator {
    private final ObjectMapper mapper = new ObjectMapper();

    @Override
    public long calculateExpiry(Document document) {
        String vcType = document.getVcType();
        System.out.println(vcType);

        CredentialType credentialType = CredentialType.fromType(vcType);
        return switch (credentialType) {
            case SOCIAL_SECURITY_CREDENTIAL -> {
                SocialSecurityDocument socialSecurityDocument =
                        mapper.convertValue(document.getData(), SocialSecurityDocument.class);
                yield ExpiryUtil.calculateExpiryTimeFromTtl(socialSecurityDocument.getCredentialTtlMinutes());
            }
            case BASIC_DISCLOSURE_CREDENTIAL -> {
                BasicCheckDocument basicCheckDocument =
                        mapper.convertValue(document.getData(), BasicCheckDocument.class);
                yield ExpiryUtil.calculateExpiryTimeFromTtl(basicCheckDocument.getCredentialTtlMinutes());
            }
            case DIGITAL_VETERAN_CARD -> {
                VeteranCardDocument veteranCardDocument =
                        mapper.convertValue(document.getData(), VeteranCardDocument.class);
                yield ExpiryUtil.calculateExpiryTimeFromTtl(veteranCardDocument.getCredentialTtlMinutes());
            }
            case MOBILE_DRIVING_LICENCE -> {
                DrivingLicenceDocument drivingLicenceDocument =
                        mapper.convertValue(document.getData(), DrivingLicenceDocument.class);
                yield ExpiryUtil.calculateExpiryTimeFromDate(drivingLicenceDocument.getExpiryDate());
            }
        };
    }
}
