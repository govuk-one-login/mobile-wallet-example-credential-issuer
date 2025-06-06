package uk.gov.di.mobile.wallet.cri.util;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Getter;

@Getter
public class Credential {
    private final String credential;

    @JsonCreator
    public Credential(@JsonProperty("credential") String credential) {
        this.credential = credential;
    }
}
