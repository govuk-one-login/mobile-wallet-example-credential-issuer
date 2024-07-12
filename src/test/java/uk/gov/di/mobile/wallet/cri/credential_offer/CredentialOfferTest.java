package uk.gov.di.mobile.wallet.cri.credential_offer;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.HashMap;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.assertArrayEquals;
import static org.junit.jupiter.api.Assertions.assertEquals;

class CredentialOfferTest {

    @Test
    @DisplayName("Should create a credential offer")
    void shouldCreateCredentialOffer() {
        Map<String, Map<String, String>> grantsMap = getGrantsMap();

        CredentialOffer credentialOffer =
                new CredentialOffer(
                        "https://credential-issuer.example.com", "TestCredentialType", grantsMap);
        assertEquals(
                "https://credential-issuer.example.com", credentialOffer.getCredentialIssuer());
        assertEquals(
                "https://credential-issuer.example.com",
                credentialOffer.getCredentialIssuerTemporary());
        assertArrayEquals(new String[] {"TestCredentialType"}, credentialOffer.getCredentials());
        assertEquals(grantsMap, credentialOffer.getGrants());
    }

    private static Map<String, Map<String, String>> getGrantsMap() {
        Map<String, Map<String, String>> grantsMap = new HashMap<>();
        Map<String, String> preAuthorizedCodeMap = new HashMap<>();
        preAuthorizedCodeMap.put("pre-authorized_code", "signedJwtString");
        grantsMap.put("urn:ietf:params:oauth:grant-type:pre-authorized_code", preAuthorizedCodeMap);
        return grantsMap;
    }
}
