package uk.gov.di.mobile.wallet.cri.credential;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonProperty;

public class RequestBody {

    @JsonProperty("proof")
    private Proof proof;

    @JsonCreator
    public RequestBody(@JsonProperty(value = "proof", required = true) Proof proof) {
        this.proof = proof;
    }

    public Proof getProof() {
        return proof;
    }
}
