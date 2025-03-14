package uk.gov.di.mobile.wallet.cri.notification;

import com.nimbusds.jwt.SignedJWT;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import uk.gov.di.mobile.wallet.cri.credential.*;
import uk.gov.di.mobile.wallet.cri.credential.AccessTokenService;
import uk.gov.di.mobile.wallet.cri.services.data_storage.DataStore;
import uk.gov.di.mobile.wallet.cri.services.data_storage.DataStoreException;

public class NotificationService {

    private final DataStore dataStore;
    private final AccessTokenService accessTokenService;
    private static final Logger LOGGER = LoggerFactory.getLogger(NotificationService.class);

    public NotificationService(DataStore dataStore, AccessTokenService accessTokenService) {
        this.dataStore = dataStore;
        this.accessTokenService = accessTokenService;
    }

    public void processNotification(SignedJWT accessToken, RequestBody requestBody)
            throws DataStoreException,
                    AccessTokenValidationException,
                    CredentialOfferNotFoundException {

        // Verify access token
        // accessTokenService.verifyAccessToken(accessToken);
        // AccessTokenService.AccessTokenClaims accessTokenClaims =
        // accessTokenService.getAccessTokenClaims(accessToken);

        // Extract access token data
        // String credentialOfferId = accessTokenClaims.credentialIdentifier();
        // LOGGER.info("Access token for credentialOfferId {} verified", credentialOfferId);

        // Fetch credential offer from cache
        // CredentialOfferCacheItem credentialOffer =
        // dataStore.getCredentialOffer(credentialOfferId);

        //        if (credentialOffer == null) {
        //            throw new CredentialOfferNotFoundException(
        //                    String.format(
        //                            "Credential offer not found for credentialOfferId %s",
        //                            credentialOfferId));
        //        }
        //        LOGGER.info("Credential offer retrieved for credentialOfferId {}",
        // credentialOfferId);

        // Check if wallet subject ID's match
//        if (!credentialOffer.getWalletSubjectId().equals(accessTokenClaims.sub())) {
//            throw new AccessTokenValidationException(
//                    "Access token sub claim does not match cached walletSubjectId");
//        }

        // Delete credential offer
        //    dataStore.deleteCredentialOffer(
        //            credentialOfferId);

    }
}
