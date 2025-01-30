package uk.gov.di.mobile.wallet.cri.credential.digitalVeteranCard;

import uk.gov.di.mobile.wallet.cri.credential.BirthDate;
import uk.gov.di.mobile.wallet.cri.credential.CredentialSubject;
import uk.gov.di.mobile.wallet.cri.credential.Name;

import java.util.List;

public class VeteranCardCredentialSubject implements CredentialSubject {

    private final String id;
    private final List<Name> name;
    private final List<BirthDate> birthDate;
    private final List<VeteranCard> veteranCard;

    VeteranCardCredentialSubject(
            String id, List<Name> name, List<BirthDate> birthDate, List<VeteranCard> veteranCard) {
        this.id = id;
        this.name = name;
        this.birthDate = birthDate;
        this.veteranCard = veteranCard;
    }

    public String getId() {
        return id;
    }

    public List<Name> getName() {
        return name;
    }

    public List<BirthDate> getBirthDate() {
        return birthDate;
    }

    public List<VeteranCard> getVeteranCard() {
        return veteranCard;
    }
}
