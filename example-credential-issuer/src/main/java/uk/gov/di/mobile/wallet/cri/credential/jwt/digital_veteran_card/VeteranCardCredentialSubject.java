package uk.gov.di.mobile.wallet.cri.credential.jwt.digital_veteran_card;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Getter;
import uk.gov.di.mobile.wallet.cri.credential.jwt.BirthDate;
import uk.gov.di.mobile.wallet.cri.credential.jwt.CredentialSubject;
import uk.gov.di.mobile.wallet.cri.credential.jwt.Name;

import java.util.List;

@Getter
public class VeteranCardCredentialSubject implements CredentialSubject {

    private final String id;
    private final List<Name> name;
    private final List<BirthDate> birthDate;
    private final List<VeteranCard> veteranCard;

    @JsonCreator
    VeteranCardCredentialSubject(
            @JsonProperty("id") String id,
            @JsonProperty("name") List<Name> name,
            @JsonProperty("birthDate") List<BirthDate> birthDate,
            @JsonProperty("veteranCard") List<VeteranCard> veteranCard) {
        this.id = id;
        this.name = name;
        this.birthDate = birthDate;
        this.veteranCard = veteranCard;
    }
}
