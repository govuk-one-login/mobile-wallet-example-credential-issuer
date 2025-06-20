package uk.gov.di.mobile.wallet.cri.util;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Getter;

@Getter
public class Credential {
    @JsonProperty("credential")
    private final String credentialObj;

    @JsonCreator
    public Credential(@JsonProperty("credential") String credential) {
        this.credentialObj = credential;
    }
}
