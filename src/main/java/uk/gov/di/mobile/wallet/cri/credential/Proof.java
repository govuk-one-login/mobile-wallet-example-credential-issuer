package uk.gov.di.mobile.wallet.cri.credential;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Getter;

@Getter
public class Proof {

    @JsonProperty("proof_type")
    private String proofType;

    @JsonProperty("jwt")
    private String jwt;

    @JsonCreator
    public Proof(
            @JsonProperty(value = "proof_type", required = true) String proofType,
            @JsonProperty(value = "jwt", required = true) String jwt) {
        this.proofType = proofType;
        this.jwt = jwt;
    }
}
