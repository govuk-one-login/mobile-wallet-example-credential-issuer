package uk.gov.di.mobile.wallet.cri.credential.jwt.basic_check_credential;

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
