package uk.gov.di.mobile.wallet.cri.credential.socialSecurityCredential;

import uk.gov.di.mobile.wallet.cri.credential.Name;

import java.util.List;

// Needed for VC MD v1.1 - to be removed once Wallet switches over to VC MD v2.0
public class SocialSecurityCredentialSubjectV1 {

    private List<Name> name;
    private List<SocialSecurityRecord> socialSecurityRecord;

    public SocialSecurityCredentialSubjectV1(
            List<Name> name, List<SocialSecurityRecord> socialSecurityRecord) {
        this.name = name;
        this.socialSecurityRecord = socialSecurityRecord;
    }

    public void setSocialSecurityRecord(List<SocialSecurityRecord> socialSecurityRecord) {
        this.socialSecurityRecord = socialSecurityRecord;
    }

    public void setName(List<Name> name) {
        this.name = name;
    }
}
