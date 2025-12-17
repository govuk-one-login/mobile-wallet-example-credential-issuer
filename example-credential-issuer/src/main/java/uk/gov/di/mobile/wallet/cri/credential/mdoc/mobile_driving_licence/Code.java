package uk.gov.di.mobile.wallet.cri.credential.mdoc.mobile_driving_licence;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonProperty;

public record Code(String code) {
    @JsonCreator
    public Code(@JsonProperty("code") String code) {
        this.code = code;
    }
}
