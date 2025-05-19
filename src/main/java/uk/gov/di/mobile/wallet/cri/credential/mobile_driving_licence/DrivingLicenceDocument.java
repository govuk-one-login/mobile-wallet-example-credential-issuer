package uk.gov.di.mobile.wallet.cri.credential.mobile_driving_licence;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Getter;
import lombok.Setter;
import uk.gov.di.mobile.wallet.cri.annotations.Namespace;

import java.time.LocalDate;
import java.time.format.DateTimeFormatter;

@Setter
@Getter
@JsonIgnoreProperties(ignoreUnknown = true)
public class DrivingLicenceDocument {
    @Namespace("iso")
    @JsonProperty("family_name")
    private String familyName;

    @Namespace("iso")
    @JsonProperty("given_name")
    private String givenName;

    @Namespace("iso")
    private String portrait;

    @Namespace("iso")
    @JsonProperty("birth_date")
    private LocalDate birthDate;

    @Namespace("iso")
    @JsonProperty("birth_place")
    private String birthPlace;

    @Namespace("iso")
    @JsonProperty("issue_date")
    private LocalDate issueDate;

    @Namespace("iso")
    @JsonProperty("expiry_date")
    private LocalDate expiryDate;

    @Namespace("iso")
    @JsonProperty("issuing_authority")
    private String issuingAuthority;

    @Namespace("iso")
    @JsonProperty("issuing_country")
    private String issuingCountry;

    @Namespace("iso")
    @JsonProperty("document_number")
    private String documentNumber;

    @Namespace("iso")
    @JsonProperty("resident_address")
    private String residentAddress;

    @Namespace("iso")
    @JsonProperty("resident_postal_code")
    private String residentPostalCode;

    @Namespace("iso")
    @JsonProperty("resident_city")
    private String residentCity;

    @Namespace("iso")
    @JsonProperty("un_distinguishing_sign")
    private String unDistinguishingSign;

    @Namespace("uk")
    @JsonProperty("title")
    private String title;

    public void setBirthDate(String birthDate) {
        this.birthDate = parseDate(birthDate);
    }

    public void setIssueDate(String issueDate) {
        this.issueDate = parseDate(issueDate);
    }

    public void setExpiryDate(String expiryDate) {
        this.expiryDate = parseDate(expiryDate);
    }

    private LocalDate parseDate(String dateString) {
        DateTimeFormatter dateFormat = DateTimeFormatter.ofPattern("dd-MM-yyyy");
        return LocalDate.parse(dateString, dateFormat);
    }

    public void setResidentAddress(String[] residentAddress) {
        this.residentAddress = String.join(", ", residentAddress);
    }
}
