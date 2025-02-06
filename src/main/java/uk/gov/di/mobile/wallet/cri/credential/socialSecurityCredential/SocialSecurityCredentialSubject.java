package uk.gov.di.mobile.wallet.cri.credential.socialSecurityCredential;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonProperty;
import uk.gov.di.mobile.wallet.cri.credential.CredentialSubject;
import uk.gov.di.mobile.wallet.cri.credential.Name;

import java.util.List;

public class SocialSecurityCredentialSubject implements CredentialSubject {

    private String id;
    private List<Name> name;
    private List<SocialSecurityRecord> socialSecurityRecord;

    @JsonCreator
    public SocialSecurityCredentialSubject(
            @JsonProperty("id") String id,
            @JsonProperty("name") List<Name> name,
            @JsonProperty("socialSecurityRecord") List<SocialSecurityRecord> socialSecurityRecord) {
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
