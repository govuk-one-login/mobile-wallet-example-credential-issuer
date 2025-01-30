package uk.gov.di.mobile.wallet.cri.credential.socialSecurityCredential;

import uk.gov.di.mobile.wallet.cri.credential.CredentialSubject;
import uk.gov.di.mobile.wallet.cri.credential.Name;

import java.util.List;

public class SocialSecurityCredentialSubject implements CredentialSubject {

    private String id;
    private List<Name> name;
    private List<SocialSecurityRecord> socialSecurityRecord;

    SocialSecurityCredentialSubject(
            String id, List<Name> name, List<SocialSecurityRecord> socialSecurityRecord) {
        this.id = id;
        this.name = name;
        this.socialSecurityRecord = socialSecurityRecord;
    }

    public String getId() {
        return id;
    }


    public List<Name> getName() {
        return name;
    }

    public List<SocialSecurityRecord> getSocialSecurityRecord() {
        return socialSecurityRecord;
    }
}
