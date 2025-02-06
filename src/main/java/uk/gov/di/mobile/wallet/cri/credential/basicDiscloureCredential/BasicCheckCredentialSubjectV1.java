package uk.gov.di.mobile.wallet.cri.credential.basicDiscloureCredential;

import uk.gov.di.mobile.wallet.cri.credential.BirthDate;
import uk.gov.di.mobile.wallet.cri.credential.Name;

import java.util.List;

// Needed for VC MD v1.1 - to be removed once Wallet switches over to VC MD v2.0
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

    public void setIssuanceDate(String issuanceDate) {
        this.issuanceDate = issuanceDate;
    }

    public void setExpirationDate(String expirationDate) {
        this.expirationDate = expirationDate;
    }

    public void setName(List<Name> name) {
        this.name = name;
    }

    public void setBirthDate(List<BirthDate> birthDate) {
        this.birthDate = birthDate;
    }

    public void setAddress(List<Address> address) {
        this.address = address;
    }

    public void setBasicCheckRecord(List<BasicCheckRecord> basicCheckRecord) {
        this.basicCheckRecord = basicCheckRecord;
    }
}
