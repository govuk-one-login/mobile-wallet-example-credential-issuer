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
    @Namespace(Namespaces.ISO)
    @JsonProperty("family_name")
    private String familyName;

    @Namespace(Namespaces.ISO)
    @JsonProperty("given_name")
    private String givenName;

    @Namespace(Namespaces.ISO)
    private String portrait;

    @Namespace(Namespaces.ISO)
    @JsonProperty("birth_date")
    private LocalDate birthDate;

    @Namespace(Namespaces.ISO)
    @JsonProperty("birth_place")
    private String birthPlace;

    @Namespace(Namespaces.ISO)
    @JsonProperty("issue_date")
    private LocalDate issueDate;

    @Namespace(Namespaces.ISO)
    @JsonProperty("expiry_date")
    private LocalDate expiryDate;

    @Namespace(Namespaces.ISO)
    @JsonProperty("issuing_authority")
    private String issuingAuthority;

    @Namespace(Namespaces.ISO)
    @JsonProperty("issuing_country")
    private String issuingCountry;

    @Namespace(Namespaces.ISO)
    @JsonProperty("document_number")
    private String documentNumber;

    @Namespace(Namespaces.ISO)
    @JsonProperty("resident_address")
    private String residentAddress;

    @Namespace(Namespaces.ISO)
    @JsonProperty("resident_postal_code")
    private String residentPostalCode;

    @Namespace(Namespaces.ISO)
    @JsonProperty("resident_city")
    private String residentCity;

    @Namespace(Namespaces.ISO)
    @JsonProperty("un_distinguishing_sign")
    private String unDistinguishingSign;

    @Namespace(Namespaces.UK)
    @JsonProperty("title")
    private String title;

    @Namespace(Namespaces.UK)
    @JsonProperty("welsh_licence")
    private boolean welshLicence;

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
