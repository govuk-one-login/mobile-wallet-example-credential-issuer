package uk.gov.di.mobile.wallet.cri.notification;

import com.nimbusds.jose.JOSEException;
import com.nimbusds.jose.crypto.ECDSASigner;
import com.nimbusds.jwt.SignedJWT;
import io.dropwizard.testing.junit5.DropwizardExtensionsSupport;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.TestInstance;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.junit.jupiter.MockitoExtension;
import org.slf4j.Logger;
import testUtils.MockAccessTokenBuilder;
import uk.gov.di.mobile.wallet.cri.models.CredentialOfferCacheItem;
import uk.gov.di.mobile.wallet.cri.services.authentication.AccessTokenService;
import uk.gov.di.mobile.wallet.cri.services.authentication.AccessTokenValidationException;
import uk.gov.di.mobile.wallet.cri.services.data_storage.DataStoreException;
import uk.gov.di.mobile.wallet.cri.services.data_storage.DynamoDbService;

import java.text.ParseException;
import java.time.Instant;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.containsString;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.Mockito.*;
import static testUtils.EcKeyHelper.getEcKey;

@TestInstance(TestInstance.Lifecycle.PER_CLASS)
@ExtendWith(DropwizardExtensionsSupport.class)
@ExtendWith(MockitoExtension.class)
class NotificationServiceTest {

    private static final String DOCUMENT_ID = "de9cbf02-2fbc-4d61-a627-f97851f6840b";
    private static final String CREDENTIAL_IDENTIFIER = "efb52887-48d6-43b7-b14c-da7896fbf54d";
    private static final String NONCE = "134e0c41-a8b4-46d4-aec8-cd547e125589";
    private static final String WALLET_SUBJECT_ID =
            "urn:fdc:wallet.account.gov.uk:2024:DtPT8x-dp_73tnlY3KNTiCitziN9GEherD16bqxNt9i";
    private static final String NOTIFICATION_ID = "77368ca6-877b-4208-a397-99f1df890400";

    private final DynamoDbService mockDynamoDbService = mock(DynamoDbService.class);
    private final AccessTokenService mockAccessTokenService = mock(AccessTokenService.class);

    private Logger mockLogger;
    private CredentialOfferCacheItem mockCredentialOfferCacheItem;
    private NotificationService notificationService;
    private SignedJWT accessToken;
    private NotificationRequestBody requestBody;

    @BeforeEach
    void setUp()
            throws ParseException,
                    JOSEException,
                    AccessTokenValidationException,
                    DataStoreException {
        ECDSASigner ecSigner = new ECDSASigner(getEcKey());
        accessToken = new MockAccessTokenBuilder("ES256").build();
        accessToken.sign(ecSigner);
        requestBody =
                new NotificationRequestBody(
                        "77368ca6-877b-4208-a397-99f1df890400",
                        EventType.credential_accepted,
                        "Credential stored");

        mockLogger = mock(Logger.class);
        notificationService =
                new NotificationService(mockDynamoDbService, mockAccessTokenService) {
                    @Override
                    protected Logger getLogger() {
                        return mockLogger;
                    }
                };

        AccessTokenService.AccessTokenData mockAccessTokenData =
                new AccessTokenService.AccessTokenData(
                        WALLET_SUBJECT_ID, NONCE, CREDENTIAL_IDENTIFIER);
        when(mockAccessTokenService.verifyAccessToken(any())).thenReturn(mockAccessTokenData);
        mockCredentialOfferCacheItem = getMockCredentialOfferCacheItem(WALLET_SUBJECT_ID);
        when(mockDynamoDbService.getCredentialOffer(anyString()))
                .thenReturn(mockCredentialOfferCacheItem);
    }

    @Test
    void Should_ThrowAccessTokenValidationException_When_CredentialOfferNotFound()
            throws DataStoreException {
        when(mockDynamoDbService.getCredentialOffer(anyString())).thenReturn(null);

        AccessTokenValidationException exception =
                assertThrows(
                        AccessTokenValidationException.class,
                        () -> notificationService.processNotification(accessToken, requestBody));

        assertEquals(
                "Credential offer with credentialOfferId efb52887-48d6-43b7-b14c-da7896fbf54d not found",
                exception.getMessage());
    }

    @Test
    void Should_ThrowAccessTokenValidationException_When_WalletSubjectIDsDoNotMatch()
            throws DataStoreException {
        mockCredentialOfferCacheItem =
                getMockCredentialOfferCacheItem("not_the_same_wallet_subject_id");
        when(mockDynamoDbService.getCredentialOffer(anyString()))
                .thenReturn(mockCredentialOfferCacheItem);

        AccessTokenValidationException exception =
                assertThrows(
                        AccessTokenValidationException.class,
                        () -> notificationService.processNotification(accessToken, requestBody));

        assertThat(
                exception.getMessage(),
                containsString("Access token 'sub' does not match cached 'walletSubjectId'"));
    }

    @Test
    void Should_ThrowInvalidNotificationIdException_When_NotificationIDsDoNotMatch()
            throws DataStoreException {
        when(mockDynamoDbService.getCredentialOffer(anyString()))
                .thenReturn(mockCredentialOfferCacheItem);

        requestBody =
                new NotificationRequestBody(
                        "not_the_same_notification_id",
                        EventType.credential_accepted,
                        "Credential stored");

        InvalidNotificationIdException exception =
                assertThrows(
                        InvalidNotificationIdException.class,
                        () -> notificationService.processNotification(accessToken, requestBody));

        assertThat(
                exception.getMessage(),
                containsString("Request 'notification_id' does not match cached 'notificationId'"));
    }

    @Test
    void Should_LogNotification_When_RequestIsValid()
            throws DataStoreException,
                    AccessTokenValidationException,
                    InvalidNotificationIdException {
        notificationService.processNotification(accessToken, requestBody);

        verify(mockLogger)
                .info(
                        "Notification received - notification_id: {}, event: {}, event_description: {}",
                        "77368ca6-877b-4208-a397-99f1df890400",
                        EventType.credential_accepted,
                        "Credential stored");
        verify(mockAccessTokenService, times(1)).verifyAccessToken(accessToken);
        verify(mockDynamoDbService, times(1)).getCredentialOffer(CREDENTIAL_IDENTIFIER);
    }

    private CredentialOfferCacheItem getMockCredentialOfferCacheItem(String walletSubjectId) {
        Long timeToLiveValue = Instant.now().plusSeconds(Long.parseLong("300")).getEpochSecond();

        return new CredentialOfferCacheItem(
                CREDENTIAL_IDENTIFIER,
                DOCUMENT_ID,
                walletSubjectId,
                NOTIFICATION_ID,
                timeToLiveValue);
    }
}
