package uk.gov.di.mobile.wallet.cri.credential_offer;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.containsString;
import static org.junit.jupiter.api.Assertions.assertEquals;

class CredentialOfferUriTest {

    private static final String WALLET_RUL = "https://mobile.test.account.gov.uk/wallet";
    private static final String PATH = "/add?credential_offer=";
    private static final String CREDENTIAL_OFFER = "uriEncodedCredentialOffer";

    @Test
    @DisplayName(
            "Should create the credential offer URI with the right property name (credential_offer_uri)")
    void testItCreatesCredentialOfferUri() throws JsonProcessingException {
        ObjectMapper mapper = new ObjectMapper();
        CredentialOfferUri credentialOfferUriObject =
                new CredentialOfferUri(WALLET_RUL, PATH, CREDENTIAL_OFFER);

        String credentialOfferUriObjectAsString =
                mapper.writeValueAsString(credentialOfferUriObject);

        assertThat(credentialOfferUriObjectAsString, containsString("credential_offer_uri"));
        assertEquals(
                "https://mobile.test.account.gov.uk/wallet/add?credential_offer=uriEncodedCredentialOffer",
                credentialOfferUriObject.getCredential());
    }
}
