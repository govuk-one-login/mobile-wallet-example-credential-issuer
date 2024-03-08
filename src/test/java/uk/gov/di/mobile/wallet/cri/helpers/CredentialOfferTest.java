package uk.gov.di.mobile.wallet.cri.helpers;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.HashMap;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.assertArrayEquals;
import static org.junit.jupiter.api.Assertions.assertEquals;

public class CredentialOfferTest {

    CredentialOffer credentialOffer;
    Map<String, Map<String, String>> grantsMap;

    @BeforeEach
    void setUp() {
        grantsMap = new HashMap<>();
        Map<String, String> preAuthorizedCodeMap = new HashMap<>();

        preAuthorizedCodeMap.put("pre-authorized_code", "signedJwtString");
        grantsMap.put("urn:ietf:params:oauth:grant-type:pre-authorized_code", preAuthorizedCodeMap);

        credentialOffer =
                new CredentialOffer(
                        "https://credential-issuer.example.com",
                        new String[] {"BasicDisclosure"},
                        grantsMap);
        ;
    }

    @Test
    @DisplayName("Creates a credential offer")
    public void testCredentialOffer() {
        assertEquals("https://credential-issuer.example.com", credentialOffer.credential_issuer);
        assertArrayEquals(new String[] {"BasicDisclosure"}, credentialOffer.credentials);
        assertEquals(grantsMap, credentialOffer.grants);
    }
}
