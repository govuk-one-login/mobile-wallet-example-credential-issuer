package uk.gov.di.mobile.wallet.cri.credential.mdoc.example_document;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Getter;
import lombok.Setter;
import uk.gov.di.mobile.wallet.cri.annotations.Namespace;
import uk.gov.di.mobile.wallet.cri.credential.mdoc.constants.NamespaceTypes;

import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.Base64;
import java.util.Objects;

@Setter
@Getter
public class ExampleDocument {

    @Namespace(NamespaceTypes.ISO)
    private final String familyName;

    @Namespace(NamespaceTypes.ISO)
    private final String givenName;

    @Namespace(NamespaceTypes.ISO)
    private final byte[] portrait;

    @Namespace(NamespaceTypes.ISO)
    private final LocalDate birthDate;

    @Namespace(NamespaceTypes.ISO)
    private final LocalDate issueDate;

    @Namespace(NamespaceTypes.ISO)
    private final LocalDate expiryDate;

    @Namespace(NamespaceTypes.ISO)
    private final String issuingCountry;

    @Namespace(NamespaceTypes.ISO)
    private final String documentNumber;

    @Namespace(NamespaceTypes.EXAMPLE_MDOC)
    private final String typeOfFish;

    @Namespace(NamespaceTypes.EXAMPLE_MDOC)
    private final int numberOfFishingRods;

    @JsonCreator
    public ExampleDocument(
            @JsonProperty("family_name") String familyName,
            @JsonProperty("given_name") String givenName,
            @JsonProperty("portrait") String portrait,
            @JsonProperty("birth_date") String birthDate,
            @JsonProperty("issue_date") String issueDate,
            @JsonProperty("expiry_date") String expiryDate,
            @JsonProperty("issuing_country") String issuingCountry,
            @JsonProperty("document_number") String documentNumber,
            @JsonProperty("type_of_fish") String typeOfFish,
            @JsonProperty("number_of_fishing_rods") int numberOfFishingRods) {
        this.familyName = Objects.requireNonNull(familyName, "family_name is required");
        this.givenName = Objects.requireNonNull(givenName, "given_name is required");
        this.portrait =
                getBytesFromBase64(Objects.requireNonNull(portrait, "portrait is required"));
        this.birthDate = parseDate(Objects.requireNonNull(birthDate, "birth_date is required"));
        this.issueDate = parseDate(Objects.requireNonNull(issueDate, "issue_date is required"));
        this.expiryDate = parseDate(Objects.requireNonNull(expiryDate, "expiry_date is required"));
        this.issuingCountry = Objects.requireNonNull(issuingCountry, "issuing_country is required");
        this.documentNumber = Objects.requireNonNull(documentNumber, "document_number is required");
        this.typeOfFish = Objects.requireNonNull(typeOfFish, "type_of_fish is required");
        this.numberOfFishingRods = numberOfFishingRods;
    }

    private LocalDate parseDate(String dateString) {
        DateTimeFormatter dateFormat = DateTimeFormatter.ofPattern("dd-MM-yyyy");
        return LocalDate.parse(dateString, dateFormat);
    }

    private byte[] getBytesFromBase64(String base64String) {
        return Base64.getDecoder().decode(base64String);
    }
}
