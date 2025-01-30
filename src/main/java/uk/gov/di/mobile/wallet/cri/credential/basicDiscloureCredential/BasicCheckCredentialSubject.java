package uk.gov.di.mobile.wallet.cri.credential.basicDiscloureCredential;

import uk.gov.di.mobile.wallet.cri.credential.BirthDate;
import uk.gov.di.mobile.wallet.cri.credential.CredentialSubject;
import uk.gov.di.mobile.wallet.cri.credential.Name;

import java.util.List;

public class BasicCheckCredentialSubject implements CredentialSubject {
    private final String id;
    private final String issuanceDate;
    private final String expirationDate;
    private final List<Name> name;
    private final List<BirthDate> birthDate;
    private final List<Address> address;
    private final List<BasicCheckRecord> basicCheckRecord;

    BasicCheckCredentialSubject(
            String id,
            String issuanceDate,
            String expirationDate,
            List<Name> name,
            List<BirthDate> birthDate,
            List<Address> address,
            List<BasicCheckRecord> basicCheckRecord) {
        this.id = id;
        this.issuanceDate = issuanceDate;
        this.expirationDate = expirationDate;
        this.name = name;
        this.birthDate = birthDate;
        this.address = address;
        this.basicCheckRecord = basicCheckRecord;
    }

    public String getId() {
        return id;
    }

    public String getIssuanceDate() {
        return issuanceDate;
    }

    public String getExpirationDate() {
        return expirationDate;
    }

    public List<Name> getName() {
        return name;
    }

    public List<BirthDate> getBirthDate() {
        return birthDate;
    }

    public List<Address> getAddress() {
        return address;
    }

    public List<BasicCheckRecord> getBasicCheckRecord() {
        return basicCheckRecord;
    }
}
