package uk.gov.di.mobile.wallet.cri.credential;

public class NinoDocument {
  private String title;
  private String givenName;
  private String familyName;
  private String nino;

  public String getTitle() {
    return title;
  }

  public void setTitle(String title) {
    this.title = title;
  }

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

  public String getNino() {
    return nino;
  }

  public void setNino(String nino) {
    this.nino = nino;
  }
}