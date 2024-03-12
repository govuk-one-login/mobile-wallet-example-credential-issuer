package uk.gov.di.mobile.wallet.cri.credential_offer;

public class CredentialOfferUri {
    private final String credential_offer_uri; // NOSONAR

    public CredentialOfferUri(String walletUrl, String path, String credentialOffer) {

        this.credential_offer_uri = walletUrl + path + credentialOffer;
    }

    public String getCredential() {
        return credential_offer_uri;
    }
}
