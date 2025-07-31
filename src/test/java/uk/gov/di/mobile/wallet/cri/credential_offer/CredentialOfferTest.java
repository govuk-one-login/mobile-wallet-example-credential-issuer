package uk.gov.di.mobile.wallet.cri.credential_offer;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.Test;

import java.util.Map;

import static org.junit.jupiter.api.Assertions.*;

class CredentialOfferTest {

    private final ObjectMapper objectMapper = new ObjectMapper();

    @Test
    void Should_CreateCredentialOffer() {
        String issuerUrl = "https://credential-issuer.example.com";
        String credentialType = "TestCredentialType";
        Map<String, Map<String, String>> grants =
                Map.of(
                        "urn:ietf:params:oauth:grant-type:pre-authorized_code",
                        Map.of("pre-authorized_code", "signedJwtString"));

        CredentialOffer credentialOffer = new CredentialOffer(issuerUrl, credentialType, grants);

        assertEquals(issuerUrl, credentialOffer.getCredentialIssuer());
        assertArrayEquals(
                new String[] {credentialType}, credentialOffer.getCredentialConfigurationIds());
        assertEquals(grants, credentialOffer.getGrants());
    }

    @Test
    void Should_SerializeCredentialIssuerWithCorrectPropertyName() throws Exception {
        CredentialOffer credentialOffer =
                new CredentialOffer(
                        "https://credential-issuer.example.com", "TestCredentialType", Map.of());

        JsonNode jsonNode = objectMapper.readTree(objectMapper.writeValueAsString(credentialOffer));
        assertTrue(jsonNode.has("credential_issuer"));
        assertFalse(jsonNode.has("credentialIssuer"));
        assertEquals(
                "https://credential-issuer.example.com",
                jsonNode.get("credential_issuer").asText());
    }

    @Test
    void Should_SerializeCrdentialConfigurationIdsWithCorrectPropertyName() throws Exception {
        CredentialOffer credentialOffer =
                new CredentialOffer(
                        "https://credential-issuer.example.com", "TestCredentialType", Map.of());

        JsonNode jsonNode = objectMapper.readTree(objectMapper.writeValueAsString(credentialOffer));
        assertTrue(jsonNode.has("credential_configuration_ids"));
        assertFalse(jsonNode.has("credentialConfigurationIds"));
        assertEquals(
                "TestCredentialType", jsonNode.get("credential_configuration_ids").get(0).asText());
    }
}
