package uk.gov.di.mobile.wallet.cri.credential.basic_check_credential;

import lombok.Setter;
import uk.gov.di.mobile.wallet.cri.credential.BirthDate;
import uk.gov.di.mobile.wallet.cri.credential.Name;

import java.util.List;

@Setter
public class BasicCheckCredentialSubjectV1 {
    private String issuanceDate;
    private String expirationDate;
    private List<Name> name;
    private List<BirthDate> birthDate;
    private List<Address> address;
    private List<BasicCheckRecord> basicCheckRecord;

    public BasicCheckCredentialSubjectV1(
            String issuanceDate,
            String expirationDate,
            List<Name> name,
            List<BirthDate> birthDate,
            List<Address> address,
            List<BasicCheckRecord> basicCheckRecord) {
        this.issuanceDate = issuanceDate;
        this.expirationDate = expirationDate;
        this.name = name;
        this.birthDate = birthDate;
        this.address = address;
        this.basicCheckRecord = basicCheckRecord;
    }
}
