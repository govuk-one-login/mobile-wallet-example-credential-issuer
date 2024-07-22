package uk.gov.di.mobile.wallet.cri.credential_offer;

import com.nimbusds.jwt.SignedJWT;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import uk.gov.di.mobile.wallet.cri.services.ConfigurationService;
import uk.gov.di.mobile.wallet.cri.services.signing.KmsService;
import uk.gov.di.mobile.wallet.cri.services.signing.SigningException;

import java.security.NoSuchAlgorithmException;
import java.util.HashMap;
import java.util.Map;

public class CredentialOfferService {

    private final ConfigurationService configurationService;
    private final KmsService kmsService;
    private static final Logger LOGGER = LoggerFactory.getLogger(CredentialOfferService.class);

    public CredentialOfferService(
            ConfigurationService configurationService, KmsService kmsService) {
        this.configurationService = configurationService;
        this.kmsService = kmsService;
    }

    public CredentialOffer buildCredentialOffer(String credentialIdentifier, String credentialType)
            throws SigningException, NoSuchAlgorithmException {
        SignedJWT preAuthorizedCode =
                new PreAuthorizedCodeBuilder(configurationService, kmsService)
                        .buildPreAuthorizedCode(credentialIdentifier);

        LOGGER.info(
                "Pre-authorized code created for credentialOfferId {} and credentialType {}",
                credentialIdentifier,
                credentialType);

        String signedJwtString = preAuthorizedCode.serialize();

        Map<String, Map<String, String>> grantsMap = new HashMap<>();
        Map<String, String> preAuthorizedCodeMap = new HashMap<>();

        preAuthorizedCodeMap.put("pre-authorized_code", signedJwtString);
        grantsMap.put("urn:ietf:params:oauth:grant-type:pre-authorized_code", preAuthorizedCodeMap);

        return new CredentialOffer(
                configurationService.getExampleCriUrl(), credentialType, grantsMap);
    }
}
