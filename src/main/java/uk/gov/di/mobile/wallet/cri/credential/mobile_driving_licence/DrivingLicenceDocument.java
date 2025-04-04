package uk.gov.di.mobile.wallet.cri.credential.mobile_driving_licence;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Getter;
import lombok.Setter;

import java.time.LocalDate;

@Setter
@Getter
public class DrivingLicenceDocument {
    @JsonProperty("family_name")
    private String familyName;

    @JsonProperty("given_name")
    private String givenName;

    private String portrait;

    @JsonProperty("birth_date")
    private LocalDate birthDate;

    @JsonProperty("birth_place")
    private String birthPlace;

    @JsonProperty("issue_date")
    private LocalDate issueDate;

    @JsonProperty("expiry_date")
    private LocalDate expiryDate;

    @JsonProperty("issuing_authority")
    private String issuingAuthority;

    @JsonProperty("issuing_country")
    private String issuingCountry;

    @JsonProperty("document_number")
    private String documentNumber;

    @JsonProperty("resident_address")
    private String residentAddress;

    @JsonProperty("resident_postal_code")
    private String residentPostalCode;

    @JsonProperty("resident_city")
    private String residentCity;
}
