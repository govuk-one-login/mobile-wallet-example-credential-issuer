package uk.gov.di.mobile.wallet.cri.notification;

import com.nimbusds.jwt.SignedJWT;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import uk.gov.di.mobile.wallet.cri.credential.CredentialOfferException;
import uk.gov.di.mobile.wallet.cri.models.CachedCredentialOffer;
import uk.gov.di.mobile.wallet.cri.models.StoredCredential;
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
                    InvalidNotificationIdException,
                    CredentialOfferException {

        AccessTokenService.AccessTokenData accessTokenData =
                accessTokenService.verifyAccessToken(accessToken);
        String credentialOfferId = accessTokenData.credentialIdentifier();

        CachedCredentialOffer credentialOffer = dataStore.getCredentialOffer(credentialOfferId);

        if (credentialOffer == null) {
            throw new CredentialOfferException(
                    String.format("Credential offer %s was not found", credentialOfferId));
        }

        if (!credentialOffer.getWalletSubjectId().equals(accessTokenData.walletSubjectId())) {
            throw new AccessTokenValidationException(
                    "Access token 'sub' does not match cached 'walletSubjectId'");
        }

        StoredCredential storedCredential =
                dataStore.getStoredCredential(credentialOffer.getCredentialIdentifier());

        if (storedCredential == null) {
            throw new InvalidNotificationIdException(
                    String.format(
                            "Stored credential for credentialOfferId '%s' not found.",
                            credentialOfferId));
        }

        if (!storedCredential
                .getNotificationId()
                .equals(notificationRequestBody.getNotificationId())) {
            throw new InvalidNotificationIdException(
                    "Request 'notification_id' does not match cached 'notificationId'");
        }

        getLogger()
                .info(
                        "Notification received - notification_id: {}, event: {}, event_description: {}",
                        storedCredential.getNotificationId(),
                        notificationRequestBody.getEvent(),
                        notificationRequestBody.getEventDescription());
    }

    protected Logger getLogger() {
        return LOGGER;
    }
}
