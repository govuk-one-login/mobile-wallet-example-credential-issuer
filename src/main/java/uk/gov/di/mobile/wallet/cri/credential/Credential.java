package uk.gov.di.mobile.wallet.cri.credential;

import com.fasterxml.jackson.annotation.JsonProperty;
import com.nimbusds.jwt.SignedJWT;

public class Credential {

    private final String credential; // NOSONAR

    public Credential(SignedJWT verifiableCredential) {

        this.credential = verifiableCredential.serialize();
    }

    @JsonProperty("credential")
    public String getCredential() {
        return credential;
    }
}
