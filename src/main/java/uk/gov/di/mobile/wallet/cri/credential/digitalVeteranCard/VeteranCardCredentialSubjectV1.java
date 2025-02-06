package uk.gov.di.mobile.wallet.cri.credential.digitalVeteranCard;

import uk.gov.di.mobile.wallet.cri.credential.BirthDate;
import uk.gov.di.mobile.wallet.cri.credential.Name;

import java.util.List;

// Needed for VC MD v1.1 - to be removed once Wallet switches over to VC MD v2.0
public class VeteranCardCredentialSubjectV1 {

    private List<Name> name;
    private List<BirthDate> birthDate;
    private List<VeteranCard> veteranCard;

    public VeteranCardCredentialSubjectV1(
            List<Name> name, List<BirthDate> birthDate, List<VeteranCard> veteranCard) {
        this.name = name;
        this.birthDate = birthDate;
        this.veteranCard = veteranCard;
    }

    public void setName(List<Name> name) {
        this.name = name;
    }

    public void setBirthDate(List<BirthDate> birthDate) {
        this.birthDate = birthDate;
    }

    public void setVeteranCard(List<VeteranCard> veteranCard) {
        this.veteranCard = veteranCard;
    }
}
