package uk.gov.di.mobile.wallet.cri.credential.mobile_driving_licence;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Getter;
import lombok.Setter;
import uk.gov.di.mobile.wallet.cri.annotations.Namespace;
import uk.gov.di.mobile.wallet.cri.credential.mobile_driving_licence.mdoc.constants.NamespaceTypes;

import java.time.LocalDate;
import java.time.Period;
import java.time.format.DateTimeFormatter;
import java.util.Base64;
import java.util.Objects;
import java.util.Optional;

@Setter
@Getter
public class DrivingLicenceDocument {

    @Namespace(NamespaceTypes.ISO)
    private final String familyName;

    @Namespace(NamespaceTypes.ISO)
    private final String givenName;

    @Namespace(NamespaceTypes.UK)
    private final String title;

    @Namespace(NamespaceTypes.UK)
    private final boolean welshLicence;

    @Namespace(NamespaceTypes.ISO)
    private final byte[] portrait;

    @Namespace(NamespaceTypes.ISO)
    private final LocalDate birthDate;

    @Namespace(NamespaceTypes.ISO)
    private final Boolean ageOver18;

    @Namespace(NamespaceTypes.ISO)
    private final Boolean ageOver21;

    @Namespace(NamespaceTypes.ISO)
    private final Boolean ageOver25;

    @Namespace(NamespaceTypes.ISO)
    private final String birthPlace;

    @Namespace(NamespaceTypes.ISO)
    private final LocalDate issueDate;

    @Namespace(NamespaceTypes.ISO)
    private final LocalDate expiryDate;

    @Namespace(NamespaceTypes.ISO)
    private final String issuingAuthority;

    @Namespace(NamespaceTypes.ISO)
    private final String issuingCountry;

    @Namespace(NamespaceTypes.ISO)
    private final String documentNumber;

    @Namespace(NamespaceTypes.ISO)
    private final String residentAddress;

    @Namespace(NamespaceTypes.ISO)
    private final String residentPostalCode;

    @Namespace(NamespaceTypes.ISO)
    private final String residentCity;

    @Namespace(NamespaceTypes.ISO)
    private final DrivingPrivilege[] drivingPrivileges;

    @Namespace(NamespaceTypes.ISO)
    private final String unDistinguishingSign;

    @Namespace(NamespaceTypes.UK)
    private final Optional<DrivingPrivilege[]> provisionalDrivingPrivileges;

    @JsonCreator
    public DrivingLicenceDocument(
            @JsonProperty("family_name") String familyName,
            @JsonProperty("given_name") String givenName,
            @JsonProperty("title") String title,
            @JsonProperty("welsh_licence") boolean welshLicence,
            @JsonProperty("portrait") String portrait,
            @JsonProperty("birth_date") String birthDate,
            @JsonProperty("birth_place") String birthPlace,
            @JsonProperty("issue_date") String issueDate,
            @JsonProperty("expiry_date") String expiryDate,
            @JsonProperty("issuing_authority") String issuingAuthority,
            @JsonProperty("issuing_country") String issuingCountry,
            @JsonProperty("document_number") String documentNumber,
            @JsonProperty("resident_address") String[] residentAddress,
            @JsonProperty("resident_postal_code") String residentPostalCode,
            @JsonProperty("resident_city") String residentCity,
            @JsonProperty("driving_privileges") DrivingPrivilege[] drivingPrivileges,
            @JsonProperty("un_distinguishing_sign") String unDistinguishingSign,
            @JsonProperty("provisional_driving_privileges")
                    DrivingPrivilege[] provisionalDrivingPrivileges) {
        this.familyName = Objects.requireNonNull(familyName, "family_name is required");
        this.givenName = Objects.requireNonNull(givenName, "given_name is required");
        this.title = Objects.requireNonNull(title, "title is required");
        this.welshLicence = welshLicence;
        this.portrait =
                getBytesFromBase64(Objects.requireNonNull(portrait, "portrait is required"));
        this.birthDate = parseDate(Objects.requireNonNull(birthDate, "birth_date is required"));
        this.ageOver18 = getAge(this.birthDate) >= 18;
        this.ageOver21 = getAge(this.birthDate) >= 21;
        this.ageOver25 = getAge(this.birthDate) >= 25;
        this.birthPlace = Objects.requireNonNull(birthPlace, "birth_place is required");
        this.issueDate = parseDate(Objects.requireNonNull(issueDate, "issue_date is required"));
        this.expiryDate = parseDate(Objects.requireNonNull(expiryDate, "expiry_date is required"));
        this.issuingAuthority =
                Objects.requireNonNull(issuingAuthority, "issuing_authority is required");
        this.issuingCountry = Objects.requireNonNull(issuingCountry, "issuing_country is required");
        this.documentNumber = Objects.requireNonNull(documentNumber, "document_number is required");
        this.residentAddress =
                String.join(
                        ", ",
                        Objects.requireNonNull(residentAddress, "resident_address is required"));
        this.residentPostalCode =
                Objects.requireNonNull(residentPostalCode, "resident_postal_code is required");
        this.residentCity = Objects.requireNonNull(residentCity, "resident_city is required");
        this.drivingPrivileges =
                Objects.requireNonNull(drivingPrivileges, "driving_privileges is required");
        this.unDistinguishingSign =
                Objects.requireNonNull(unDistinguishingSign, "un_distinguishing_sign is required");
        this.provisionalDrivingPrivileges = Optional.ofNullable(provisionalDrivingPrivileges);
    }

    private LocalDate parseDate(String dateString) {
        DateTimeFormatter dateFormat = DateTimeFormatter.ofPattern("dd-MM-yyyy");
        return LocalDate.parse(dateString, dateFormat);
    }

    private int getAge(LocalDate birthDate) {
        return Period.between(birthDate, LocalDate.now()).getYears();
    }

    private byte[] getBytesFromBase64(String base64String) {
        return Base64.getDecoder().decode(base64String);
    }
}
