package uk.gov.di.mobile.wallet.cri.credential;

import com.fasterxml.jackson.databind.ObjectMapper;
import uk.gov.di.mobile.wallet.cri.credential.basic_check_credential.BasicCheckDocument;
import uk.gov.di.mobile.wallet.cri.credential.digital_veteran_card.VeteranCardDocument;
import uk.gov.di.mobile.wallet.cri.credential.mobile_driving_licence.DrivingLicenceDocument;
import uk.gov.di.mobile.wallet.cri.credential.social_security_credential.SocialSecurityDocument;
import uk.gov.di.mobile.wallet.cri.util.ExpiryUtil;

public class CredentialExpiryCalculator implements ExpiryCalculator {
  @Override
  public long calculateExpiry(Document document) {
    String vcType = document.getVcType();

    switch (vcType) {
      case "SOCIAL_SECURITY":
        SocialSecurityDocument ssDoc = new ObjectMapper().convertValue(document.getData(), SocialSecurityDocument.class);
        return ExpiryUtil.calculateExpiryTimeFromTtl(ssDoc.getCredentialTtlMinutes());
      case "BASIC_DISCLOSURE":
        BasicCheckDocument bcDoc = new ObjectMapper().convertValue(document.getData(), BasicCheckDocument.class);
        return ExpiryUtil.calculateExpiryTimeFromTtl(bcDoc.getCredentialTtlMinutes());
      case "DIGITAL_VETERAN_CARD":
        VeteranCardDocument vetDoc = new ObjectMapper().convertValue(document.getData(), VeteranCardDocument.class);
        return ExpiryUtil.calculateExpiryTimeFromTtl(vetDoc.getCredentialTtlMinutes());
      case "MOBILE_DRIVING_LICENCE":
        DrivingLicenceDocument dlDoc = new ObjectMapper().convertValue(document.getData(), DrivingLicenceDocument.class);
        return ExpiryUtil.calculateExpiryTimeFromDate(dlDoc.getExpiryDate());
      default:
        throw new IllegalArgumentException("Unsupported credential type for expiry calculation: " + vcType);
    }
  }
}