package uk.gov.di.mobile.wallet.cri.credential_offer;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Getter;

import java.util.Map;

public class CredentialOffer {

    private final String credential_issuer; // NOSONAR
    @Getter private final String[] credentials;
    @Getter private final Map<String, Map<String, String>> grants;

    public CredentialOffer(
            String credentialIssuer,
            String credentialType,
            Map<String, Map<String, String>> grants) {
        this.credential_issuer = credentialIssuer;
        this.credentials = new String[] {credentialType};
        this.grants = grants;
    }

    @JsonProperty("credential_issuer")
    public String getCredentialIssuer() {
        return credential_issuer;
    }
}
