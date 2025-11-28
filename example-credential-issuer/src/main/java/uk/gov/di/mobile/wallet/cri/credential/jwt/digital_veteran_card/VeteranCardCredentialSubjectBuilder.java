package uk.gov.di.mobile.wallet.cri.credential.jwt.digital_veteran_card;

import uk.gov.di.mobile.wallet.cri.credential.jwt.BirthDate;
import uk.gov.di.mobile.wallet.cri.credential.jwt.Name;

import java.util.List;

public class VeteranCardCredentialSubjectBuilder {
    private String id;
    private List<Name> name;
    private List<BirthDate> birthDate;
    private List<VeteranCard> veteranCard;

    public VeteranCardCredentialSubjectBuilder setId(String id) {
        this.id = id;
        return this;
    }

    public VeteranCardCredentialSubjectBuilder setName(List<Name> name) {
        this.name = name;
        return this;
    }

    public VeteranCardCredentialSubjectBuilder setBirthDate(List<BirthDate> birthDate) {
        this.birthDate = birthDate;
        return this;
    }

    public VeteranCardCredentialSubjectBuilder setVeteranCard(List<VeteranCard> veteranCard) {
        this.veteranCard = veteranCard;
        return this;
    }

    public VeteranCardCredentialSubject build() {
        return new VeteranCardCredentialSubject(id, name, birthDate, veteranCard);
    }
}
