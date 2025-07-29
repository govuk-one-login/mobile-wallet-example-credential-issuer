package uk.gov.di.mobile.wallet.cri.credential;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Getter;

@Getter
public class Credential {
    @JsonProperty("credential")
    private final CredentialResult credentialObj;

    @JsonCreator
    public Credential(@JsonProperty("credential") CredentialResult credential) {
        this.credentialObj = credential;
    }
}
