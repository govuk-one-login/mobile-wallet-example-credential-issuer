package uk.gov.di.mobile.wallet.cri.credential.digital_veteran_card;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Getter;
import lombok.Setter;

@Setter
@Getter
public class VeteranCardDocument {

    private String givenName;
    private String familyName;

    @JsonProperty("dateOfBirth-day")
    private String dateOfBirthDay;

    @JsonProperty("dateOfBirth-month")
    private String dateOfBirthMonth;

    @JsonProperty("dateOfBirth-year")
    private String dateOfBirthYear;

    @JsonProperty("cardExpiryDate-day")
    private String cardExpiryDateDay;

    @JsonProperty("cardExpiryDate-month")
    private String cardExpiryDateMonth;

    @JsonProperty("cardExpiryDate-year")
    private String cardExpiryDateYear;

    private String serviceNumber;
    private String serviceBranch;
    private long credentialTtlMinutes;
    private String photo;
}
