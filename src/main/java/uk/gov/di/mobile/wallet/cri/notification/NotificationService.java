package uk.gov.di.mobile.wallet.cri.notification;

import com.nimbusds.jwt.SignedJWT;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import uk.gov.di.mobile.wallet.cri.models.CredentialOfferCacheItem;
import uk.gov.di.mobile.wallet.cri.services.authentication.AccessTokenService;
import uk.gov.di.mobile.wallet.cri.services.authentication.AccessTokenValidationException;
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

    public void processNotification(
            SignedJWT accessToken, NotificationRequestBody notificationRequestBody)
            throws DataStoreException,
                    AccessTokenValidationException,
                    InvalidNotificationIdException {

        AccessTokenService.AccessTokenData accessTokenData =
                accessTokenService.verifyAccessToken(accessToken);

        String credentialOfferId = accessTokenData.credentialIdentifier();

        CredentialOfferCacheItem credentialOffer = dataStore.getCredentialOffer(credentialOfferId);

        if (credentialOffer == null) {
            throw new AccessTokenValidationException(
                    String.format(
                            "Credential offer with credentialOfferId %s not found",
                            credentialOfferId));
        }

        if (!credentialOffer.getWalletSubjectId().equals(accessTokenData.walletSubjectId())) {
            throw new AccessTokenValidationException(
                    "Access token 'sub' does not match cached 'walletSubjectId'");
        }

        if (!credentialOffer
                .getNotificationId()
                .equals(notificationRequestBody.getNotificationId())) {
            throw new InvalidNotificationIdException(
                    "Request 'notification_id' does not match cached 'notificationId'");
        }

        getLogger()
                .info(
                        "Notification received - notification_id {}",
                        notificationRequestBody.getNotificationId());
    }

    protected Logger getLogger() {
        return LOGGER;
    }
}
