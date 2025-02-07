package uk.gov.di.mobile.wallet.cri.credential.basic_check_credential;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Getter;
import uk.gov.di.mobile.wallet.cri.credential.BirthDate;
import uk.gov.di.mobile.wallet.cri.credential.CredentialSubject;
import uk.gov.di.mobile.wallet.cri.credential.Name;

import java.util.List;

@Getter
public class BasicCheckCredentialSubject implements CredentialSubject {
    private final String id;
    private final String issuanceDate;
    private final String expirationDate;
    private final List<Name> name;
    private final List<BirthDate> birthDate;
    private final List<Address> address;
    private final List<BasicCheckRecord> basicCheckRecord;

    @JsonCreator
    BasicCheckCredentialSubject(
            @JsonProperty("id") String id,
            @JsonProperty("issuanceDate") String issuanceDate,
            @JsonProperty("expiryDate") String expirationDate,
            @JsonProperty("name") List<Name> name,
            @JsonProperty("birthDate") List<BirthDate> birthDate,
            @JsonProperty("address") List<Address> address,
            @JsonProperty("basicCheckRecord") List<BasicCheckRecord> basicCheckRecord) {
        this.id = id;
        this.issuanceDate = issuanceDate;
        this.expirationDate = expirationDate;
        this.name = name;
        this.birthDate = birthDate;
        this.address = address;
        this.basicCheckRecord = basicCheckRecord;
    }
}
