package uk.gov.di.mobile.wallet.cri.did_document;

import com.fasterxml.jackson.annotation.JsonProperty;

import java.util.List;

public class DidDocument {

    public DidDocument(
            List<String> context,
            String id,
            List<Did> verificationMethod,
            List<String> assertionMethod) {
        this.context = context;
        this.id = id;
        this.verificationMethod = verificationMethod;
        this.assertionMethod = assertionMethod;
    }

    List<String> context;
    String id;
    List<Did> verificationMethod;
    List<String> assertionMethod;

    @JsonProperty("@context")
    public List<String> getContext() {
        return context;
    }

    @JsonProperty("id")
    public String getId() {
        return id;
    }

    @JsonProperty("verificationMethod")
    public List<Did> getVerificationMethod() {
        return verificationMethod;
    }

    @JsonProperty("assertionMethod")
    public List<String> getAssertionMethod() {
        return assertionMethod;
    }
}
