package uk.gov.di.mobile.wallet.cri.credential_offer;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.HashMap;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.assertArrayEquals;
import static org.junit.jupiter.api.Assertions.assertEquals;

class CredentialOfferTest {

    private CredentialOffer credentialOffer;
    private static final String credentialIssuer = "https://credential-issuer.example.com";
    private static final String[] credentials = {"BasicDisclosure"};
    private static final Map<String, Map<String, String>> grantsMap = new HashMap<>();

    @BeforeEach
    void setUp() {
        Map<String, String> preAuthorizedCodeMap = new HashMap<>();
        preAuthorizedCodeMap.put("pre-authorized_code", "signedJwtString");
        grantsMap.put("urn:ietf:params:oauth:grant-type:pre-authorized_code", preAuthorizedCodeMap);

        credentialOffer = new CredentialOffer(credentialIssuer, credentials, grantsMap);
    }

    @Test
    @DisplayName("Should create a credential offer")
    void testItCreatesCredentialOffer() {
        assertEquals(
                "https://credential-issuer.example.com", credentialOffer.getCredentialIssuer());
        assertArrayEquals(new String[] {"BasicDisclosure"}, credentialOffer.getCredentials());
        assertEquals(grantsMap, credentialOffer.getGrants());
    }
}
