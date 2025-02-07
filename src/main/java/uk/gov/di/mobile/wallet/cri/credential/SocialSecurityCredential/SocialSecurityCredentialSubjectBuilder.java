package uk.gov.di.mobile.wallet.cri.credential.SocialSecurityCredential;

import uk.gov.di.mobile.wallet.cri.credential.Name;

import java.util.List;

public class SocialSecurityCredentialSubjectBuilder {

    private String id;
    private List<Name> name;
    private List<SocialSecurityRecord> socialSecurityRecord;

    public SocialSecurityCredentialSubjectBuilder setId(String id) {
        this.id = id;
        return this;
    }

    public SocialSecurityCredentialSubjectBuilder setName(List<Name> name) {
        this.name = name;
        return this;
    }

    public SocialSecurityCredentialSubjectBuilder setSocialSecurityRecord(
            List<SocialSecurityRecord> socialSecurityRecord) {
        this.socialSecurityRecord = socialSecurityRecord;
        return this;
    }

    public SocialSecurityCredentialSubject build() {
        return new SocialSecurityCredentialSubject(id, name, socialSecurityRecord);
    }
}
