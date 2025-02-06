package uk.gov.di.mobile.wallet.cri.credential.digitalVeteranCard;

import lombok.Setter;
import uk.gov.di.mobile.wallet.cri.credential.BirthDate;
import uk.gov.di.mobile.wallet.cri.credential.Name;

import java.util.List;

@Setter
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
}
