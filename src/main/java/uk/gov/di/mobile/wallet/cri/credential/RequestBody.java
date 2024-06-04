package uk.gov.di.mobile.wallet.cri.credential;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonProperty;

public class RequestBody {

    @JsonProperty("proof")
    private Proof proof;

    @JsonProperty("credential_identifier")
    private String credential_identifier;

    @JsonCreator
    public RequestBody(
            @JsonProperty(value = "proof", required = true) Proof proof,
            @JsonProperty(value = "credential_identifier", required = true)
                    String credentialIdentifier) {
        this.proof = proof;
        this.credential_identifier = credentialIdentifier;
    }

    public Proof getProof() {
        return proof;
    }

    public String getCredentialIdentifier() {
        return credential_identifier;
    }
}
