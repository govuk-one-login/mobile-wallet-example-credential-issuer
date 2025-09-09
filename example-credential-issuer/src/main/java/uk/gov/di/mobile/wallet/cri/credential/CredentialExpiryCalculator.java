package uk.gov.di.mobile.wallet.cri.credential;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.datatype.jdk8.Jdk8Module;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;
import uk.gov.di.mobile.wallet.cri.credential.basic_check_credential.BasicCheckDocument;
import uk.gov.di.mobile.wallet.cri.credential.digital_veteran_card.VeteranCardDocument;
import uk.gov.di.mobile.wallet.cri.credential.mobile_driving_licence.DrivingLicenceDocument;
import uk.gov.di.mobile.wallet.cri.credential.social_security_credential.SocialSecurityDocument;
import uk.gov.di.mobile.wallet.cri.util.ExpiryUtil;

import java.time.Clock;

public class CredentialExpiryCalculator implements ExpiryCalculator {
    private final ObjectMapper mapper =
            new ObjectMapper()
                    .registerModule(new JavaTimeModule())
                    .registerModule(new Jdk8Module());
    ;
    ExpiryUtil expiryUtil = new ExpiryUtil(Clock.systemDefaultZone());

    @Override
    public long calculateExpiry(Document document) {
        String vcType = document.getVcType();

        CredentialType credentialType = CredentialType.fromType(vcType);
        return switch (credentialType) {
            case SOCIAL_SECURITY_CREDENTIAL -> {
                SocialSecurityDocument socialSecurityDocument =
                        mapper.convertValue(document.getData(), SocialSecurityDocument.class);
                yield expiryUtil.calculateExpiryTimeFromTtl(
                        socialSecurityDocument.getCredentialTtlMinutes());
            }
            case BASIC_DISCLOSURE_CREDENTIAL -> {
                BasicCheckDocument basicCheckDocument =
                        mapper.convertValue(document.getData(), BasicCheckDocument.class);
                yield expiryUtil.calculateExpiryTimeFromTtl(
                        basicCheckDocument.getCredentialTtlMinutes());
            }
            case DIGITAL_VETERAN_CARD -> {
                VeteranCardDocument veteranCardDocument =
                        mapper.convertValue(document.getData(), VeteranCardDocument.class);
                yield expiryUtil.calculateExpiryTimeFromTtl(
                        veteranCardDocument.getCredentialTtlMinutes());
            }
            case MOBILE_DRIVING_LICENCE -> {
                DrivingLicenceDocument drivingLicenceDocument =
                        mapper.convertValue(document.getData(), DrivingLicenceDocument.class);
                yield expiryUtil.calculateExpiryTimeFromDate(
                        drivingLicenceDocument.getExpiryDate());
            }
        };
    }
}
