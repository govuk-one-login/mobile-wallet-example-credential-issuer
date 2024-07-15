package uk.gov.di.mobile.wallet.cri.did_document;

import com.fasterxml.jackson.annotation.JsonProperty;

import java.util.List;

public class DidDocumentBuilder {

    List<String> context;
    String id;
    List<Did> verificationMethod;
    List<String> assertionMethod;

    @JsonProperty("@context")
    public DidDocumentBuilder setContext(List<String> context) throws IllegalArgumentException {
        if (context == null) {
            throw new IllegalArgumentException("context must not be null");
        }
        this.context = context;
        return this;
    }

    public DidDocumentBuilder setId(String id) throws IllegalArgumentException {
        if (id == null) {
            throw new IllegalArgumentException("id must not be null");
        }
        this.id = id;
        return this;
    }

    public DidDocumentBuilder setVerificationMethod(List<Did> verificationMethod)
            throws IllegalArgumentException {
        if (verificationMethod == null) {
            throw new IllegalArgumentException("verificationMethod must not be null");
        }
        this.verificationMethod = verificationMethod;
        return this;
    }

    public DidDocumentBuilder setAssertionMethod(List<String> assertionMethod)
            throws IllegalArgumentException {
        if (assertionMethod == null) {
            throw new IllegalArgumentException("assertionMethod must not be null");
        }
        this.assertionMethod = assertionMethod;
        return this;
    }

    public DidDocument build() {
        return new DidDocument(context, id, verificationMethod, assertionMethod);
    }
}
