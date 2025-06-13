package uk.gov.di.mobile.wallet.cri.credential_offer;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Getter;

import java.util.Map;

/**
 * Represents a credential offer according to the OpenID for Verifiable Credentials specification.
 * This object contains information about the credential issuer, the types of credentials being
 * offered, and the authorization grants available to obtain those credentials.
 */
@Getter
public class CredentialOffer {

    /** The URL of the credential issuer */
    @JsonProperty("credential_issuer")
    private final String credentialIssuer;

    /** Array of credential types being offered */
    @JsonProperty("credential_configuration_ids")
    private final String[] credentialConfigurationIds;

    /** Map of authorization grants with their parameters */
    private final Map<String, Map<String, String>> grants;

    /**
     * Constructs a new CredentialOffer.
     *
     * @param credentialIssuer The URL of the credential issuer.
     * @param credentialType The type of credential being offered.
     * @param grants The authorization grants available for obtaining the credential.
     */
    public CredentialOffer(
            String credentialIssuer,
            String credentialType,
            Map<String, Map<String, String>> grants) {
        this.credentialIssuer = credentialIssuer;
        this.credentialConfigurationIds = new String[] {credentialType};
        this.grants = grants;
    }
}
