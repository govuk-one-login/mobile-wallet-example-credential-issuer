package uk.gov.di.mobile.wallet.cri.credential_offer;

import com.nimbusds.jwt.SignedJWT;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import uk.gov.di.mobile.wallet.cri.services.ConfigurationService;
import uk.gov.di.mobile.wallet.cri.services.signing.SigningException;

import java.security.NoSuchAlgorithmException;
import java.util.Map;

/**
 * Service responsible for building credential offers for the OpenID for Verifiable Credentials
 * flow.
 */
public class CredentialOfferService {

    /** OAuth 2.0 grant type for pre-authorized code flow. */
    private static final String PRE_AUTHORIZED_CODE_GRANT_TYPE =
            "urn:ietf:params:oauth:grant-type:pre-authorized_code";

    /** Parameter name for the pre-authorized code value. */
    private static final String PRE_AUTHORIZED_CODE_PARAM = "pre-authorized_code";

    private final ConfigurationService configurationService;
    private final PreAuthorizedCodeBuilder preAuthorizedCodeBuilder;
    private static final Logger LOGGER = LoggerFactory.getLogger(CredentialOfferService.class);

    public CredentialOfferService(
            ConfigurationService configurationService,
            PreAuthorizedCodeBuilder preAuthorizedCodeBuilder) {
        this.configurationService = configurationService;
        this.preAuthorizedCodeBuilder = preAuthorizedCodeBuilder;
    }

    /**
     * Builds a credential offer with a pre-authorized code for the specified credential.
     *
     * @param credentialIdentifier Unique identifier for the credential being offered.
     * @param credentialType The type/format of the credential (e.g., "VerifiableCredential").
     * @return A CredentialOffer containing the issuer URL, credential type, and pre-authorized code.
     * @throws SigningException If there's an error signing the pre-authorized code JWT.
     * @throws NoSuchAlgorithmException If the required cryptographic algorithm is not available.
     */
    public CredentialOffer buildCredentialOffer(String credentialIdentifier, String credentialType)
            throws SigningException, NoSuchAlgorithmException {

        SignedJWT preAuthorizedCode =
                preAuthorizedCodeBuilder.buildPreAuthorizedCode(credentialIdentifier);
        LOGGER.info(
                "Pre-authorized code created for credentialOfferId {} and credentialType {}",
                credentialIdentifier,
                credentialType);

        String signedJwtString = preAuthorizedCode.serialize();

        Map<String, Map<String, String>> grants =
                Map.of(
                        PRE_AUTHORIZED_CODE_GRANT_TYPE,
                        Map.of(PRE_AUTHORIZED_CODE_PARAM, signedJwtString));

        return new CredentialOffer(configurationService.getSelfUrl(), credentialType, grants);
    }
}
