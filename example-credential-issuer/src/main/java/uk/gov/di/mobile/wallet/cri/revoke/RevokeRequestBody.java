package uk.gov.di.mobile.wallet.cri.revoke;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Getter;

@Getter
public class RevokeRequestBody {
    @JsonProperty("drivingLicenceNumber")
    private String drivingLicenceNumber;

    @JsonCreator
    public RevokeRequestBody(
            @JsonProperty(value = "drivingLicenceNumber", required = true)
                    String drivingLicenceNumber) {
        this.drivingLicenceNumber = drivingLicenceNumber;
    }
}
