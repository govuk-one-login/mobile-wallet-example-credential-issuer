package uk.gov.di.mobile.wallet.cri.credential.jwt.basic_check_credential.domain;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Getter;
import lombok.Setter;

@Setter
@Getter
public class BasicCheckDocument {
    @JsonProperty("issuance-day")
    private String issuanceDay;

    @JsonProperty("issuance-month")
    private String issuanceMonth;

    @JsonProperty("issuance-year")
    private String issuanceYear;

    @JsonProperty("expiration-day")
    private String expirationDay;

    @JsonProperty("expiration-month")
    private String expirationMonth;

    @JsonProperty("expiration-year")
    private String expirationYear;

    @JsonProperty("birth-day")
    private String birthDay;

    @JsonProperty("birth-month")
    private String birthMonth;

    @JsonProperty("birth-year")
    private String birthYear;

    private String firstName;
    private String lastName;
    private String subBuildingName;
    private String buildingName;
    private String streetName;
    private String addressLocality;
    private String addressCountry;
    private String postalCode;
    private String certificateNumber;
    private String applicationNumber;
    private String certificateType;
    private String outcome;
    private String policeRecordsCheck;
    private long credentialTtlMinutes;
}
