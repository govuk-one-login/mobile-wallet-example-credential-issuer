package uk.gov.di.mobile.wallet.cri.credential_offer;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;

class CredentialOfferUriTest {

    private static final String WALLET_RUL = "https://mobile.test.account.gov.uk/wallet";
    private static final String PATH = "/add?credential_offer=";
    private static final String CREDENTIAL_OFFER = "uriEncodedCredentialOffer";

    @Test
    @DisplayName("Should create the credential offer URI")
    void testItCreatesCredentialOfferUri() {
        CredentialOfferUri credentialOffer =
                new CredentialOfferUri(WALLET_RUL, PATH, CREDENTIAL_OFFER);
        assertEquals(
                "https://mobile.test.account.gov.uk/wallet/add?credential_offer=uriEncodedCredentialOffer",
                credentialOffer.getCredential());
    }
}
