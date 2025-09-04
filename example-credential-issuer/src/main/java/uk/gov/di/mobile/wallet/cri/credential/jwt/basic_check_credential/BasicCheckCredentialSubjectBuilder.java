package uk.gov.di.mobile.wallet.cri.credential.jwt.basic_check_credential;

import uk.gov.di.mobile.wallet.cri.credential.jwt.basic_check_credential.domain.Address;
import uk.gov.di.mobile.wallet.cri.credential.jwt.basic_check_credential.domain.BasicCheckRecord;
import uk.gov.di.mobile.wallet.cri.credential.jwt.domain.BirthDate;
import uk.gov.di.mobile.wallet.cri.credential.jwt.domain.Name;

import java.util.List;

public class BasicCheckCredentialSubjectBuilder {

    private String id;
    private String issuanceDate;
    private String expirationDate;
    private List<Name> name;
    private List<BirthDate> birthDate;
    private List<Address> address;
    private List<BasicCheckRecord> basicCheckRecord;

    public BasicCheckCredentialSubjectBuilder setId(String id) {
        this.id = id;
        return this;
    }

    public BasicCheckCredentialSubjectBuilder setIssuanceDate(String issuanceDate) {
        this.issuanceDate = issuanceDate;
        return this;
    }

    public BasicCheckCredentialSubjectBuilder setExpirationDate(String expirationDate) {
        this.expirationDate = expirationDate;
        return this;
    }

    public BasicCheckCredentialSubjectBuilder setName(List<Name> name) {
        this.name = name;
        return this;
    }

    public BasicCheckCredentialSubjectBuilder setBirthDate(List<BirthDate> birthDate) {
        this.birthDate = birthDate;
        return this;
    }

    public BasicCheckCredentialSubjectBuilder setAddress(List<Address> address) {
        this.address = address;
        return this;
    }

    public BasicCheckCredentialSubjectBuilder setBasicCheckRecord(
            List<BasicCheckRecord> basicCheckRecord) {
        this.basicCheckRecord = basicCheckRecord;
        return this;
    }

    public BasicCheckCredentialSubject build() {
        return new BasicCheckCredentialSubject(
                id, issuanceDate, expirationDate, name, birthDate, address, basicCheckRecord);
    }
}
