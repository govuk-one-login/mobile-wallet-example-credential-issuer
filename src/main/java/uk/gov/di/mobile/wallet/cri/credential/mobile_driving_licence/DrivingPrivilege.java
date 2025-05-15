package uk.gov.di.mobile.wallet.cri.credential.mobile_driving_licence;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Getter;
import lombok.Setter;

import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.Optional;

@Setter
@Getter
public class DrivingPrivilege {
    @JsonProperty("vehicle_category_code")
    private String vehicleCategoryCode;

    @JsonProperty(value = "issue_date")
    private Optional<LocalDate> issueDate;

    @JsonProperty(value = "expiry_date")
    private Optional<LocalDate> expiryDate;

    public void setIssueDate(String issueDate) {
        this.issueDate = Optional.of(parseDate(issueDate));
    }

    public void setExpiryDate(String expiryDate) {
        this.expiryDate = Optional.of(parseDate(expiryDate));
    }

    private LocalDate parseDate(String dateString) {
        DateTimeFormatter dateFormat = DateTimeFormatter.ofPattern("dd-MM-yyyy");
        return LocalDate.parse(dateString, dateFormat);
    }
}
