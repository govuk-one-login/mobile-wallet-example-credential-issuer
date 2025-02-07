package uk.gov.di.mobile.wallet.cri.credential.BasicCheckCredential;

import lombok.Getter;
import lombok.Setter;

@Setter
@Getter
public class Address {
    private String subBuildingName;
    private String buildingName;
    private String streetName;
    private String addressLocality;
    private String postalCode;
    private String addressCountry;
}
