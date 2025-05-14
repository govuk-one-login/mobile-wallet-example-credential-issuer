package uk.gov.di.mobile.wallet.cri.credential.mobile_driving_licence;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Getter;
import lombok.Setter;

import java.time.LocalDate;
import java.time.format.DateTimeFormatter;

@Setter
@Getter
public class DrivingPrivilege {
  @JsonProperty("vehicle_category_code")
  private String vehicleCategoryCode;

  @JsonProperty(value = "issue_date", required = false)
  private LocalDate issueDate;

  @JsonProperty(value = "expiry_date", required = false)
  private LocalDate expiryDate;

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
}