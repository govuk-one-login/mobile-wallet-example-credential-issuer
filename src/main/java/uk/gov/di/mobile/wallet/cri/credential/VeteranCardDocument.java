package uk.gov.di.mobile.wallet.cri.credential;
import com.fasterxml.jackson.annotation.JsonProperty;

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

  public String getGivenName() {
    return givenName;
  }

  public void setGivenName(String givenName) {
    this.givenName = givenName;
  }

  public String getFamilyName() {
    return familyName;
  }

  public void setFamilyName(String familyName) {
    this.familyName = familyName;
  }

  public String getDateOfBirthDay() {
    return dateOfBirthDay;
  }

  public void setDateOfBirthDay(String dateOfBirthDay) {
    this.dateOfBirthDay = dateOfBirthDay;
  }

  public String getDateOfBirthMonth() {
    return dateOfBirthMonth;
  }

  public void setDateOfBirthMonth(String dateOfBirthMonth) {
    this.dateOfBirthMonth = dateOfBirthMonth;
  }

  public String getDateOfBirthYear() {
    return dateOfBirthYear;
  }

  public void setDateOfBirthYear(String dateOfBirthYear) {
    this.dateOfBirthYear = dateOfBirthYear;
  }

  public String getCardExpiryDateDay() {
    return cardExpiryDateDay;
  }

  public void setCardExpiryDateDay(String cardExpiryDateDay) {
    this.cardExpiryDateDay = cardExpiryDateDay;
  }

  public String getCardExpiryDateMonth() {
    return cardExpiryDateMonth;
  }

  public void setCardExpiryDateMonth(String cardExpiryDateMonth) {
    this.cardExpiryDateMonth = cardExpiryDateMonth;
  }

  public String getCardExpiryDateYear() {
    return cardExpiryDateYear;
  }

  public void setCardExpiryDateYear(String lastName) {
    this.cardExpiryDateYear = cardExpiryDateYear;
  }

  public String getServiceNumber() {
    return serviceNumber;
  }

  public void setServiceNumber(String serviceNumber) {
    this.serviceNumber = serviceNumber;
  }

  public String getServiceBranch() {
    return serviceBranch;
  }

  public void setServiceBranch(String serviceBranch) {
    this.serviceBranch = serviceBranch;
  }
}