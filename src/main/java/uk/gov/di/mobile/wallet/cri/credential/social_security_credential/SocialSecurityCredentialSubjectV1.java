package uk.gov.di.mobile.wallet.cri.credential.social_security_credential;

import lombok.Setter;
import uk.gov.di.mobile.wallet.cri.credential.Name;

import java.util.List;

@Setter
public class SocialSecurityCredentialSubjectV1 {

    private List<Name> name;
    private List<SocialSecurityRecord> socialSecurityRecord;

    public SocialSecurityCredentialSubjectV1(
            List<Name> name, List<SocialSecurityRecord> socialSecurityRecord) {
        this.name = name;
        this.socialSecurityRecord = socialSecurityRecord;
    }
}
