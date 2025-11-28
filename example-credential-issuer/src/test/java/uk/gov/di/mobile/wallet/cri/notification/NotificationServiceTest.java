package uk.gov.di.mobile.wallet.cri.notification;

import com.nimbusds.jose.JOSEException;
import com.nimbusds.jose.crypto.ECDSASigner;
import com.nimbusds.jwt.SignedJWT;
import io.dropwizard.testing.junit5.DropwizardExtensionsSupport;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.TestInstance;
import org.junit.jupiter.api.extension.ExtendWith;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.CsvSource;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.slf4j.Logger;
import testUtils.MockAccessTokenBuilder;
import uk.gov.di.mobile.wallet.cri.credential.StoredCredential;
import uk.gov.di.mobile.wallet.cri.services.authentication.AccessTokenService;
import uk.gov.di.mobile.wallet.cri.services.authentication.AccessTokenValidationException;
import uk.gov.di.mobile.wallet.cri.services.data_storage.DataStoreException;
import uk.gov.di.mobile.wallet.cri.services.data_storage.DynamoDbService;

import java.text.ParseException;
import java.util.Optional;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.containsString;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static testUtils.EcKeyHelper.getEcKey;

@TestInstance(TestInstance.Lifecycle.PER_CLASS)
@ExtendWith(DropwizardExtensionsSupport.class)
@ExtendWith(MockitoExtension.class)
class NotificationServiceTest {

    private static final String CREDENTIAL_IDENTIFIER = "efb52887-48d6-43b7-b14c-da7896fbf54d";
    private static final String WALLET_SUBJECT_ID =
            "urn:fdc:wallet.account.gov.uk:2024:DtPT8x-dp_73tnlY3KNTiCitziN9GEherD16bqxNt9i";
    private static final String NOTIFICATION_ID = "77368ca6-877b-4208-a397-99f1df890400";
    private static final String NONCE = "134e0c41-a8b4-46d4-aec8-cd547e125589";
    private static final String DOCUMENT_ID = "1234ABCDefg";
    private static final Long TIME_TO_LIVE = 43200L;

    @Mock private DynamoDbService mockDynamoDbService;
    @Mock private AccessTokenService mockAccessTokenService;
    @Mock private Logger mockLogger;

    private NotificationService notificationService;
    private SignedJWT accessToken;

    @BeforeEach
    void setUp() throws ParseException, JOSEException, AccessTokenValidationException {
        notificationService =
                new NotificationService(mockDynamoDbService, mockAccessTokenService) {
                    @Override
                    protected Logger getLogger() {
                        return mockLogger;
                    }
                };

        ECDSASigner ecSigner = new ECDSASigner(getEcKey());
        accessToken = new MockAccessTokenBuilder("ES256").build();
        accessToken.sign(ecSigner);

        AccessTokenService.AccessTokenData mockAccessTokenData =
                new AccessTokenService.AccessTokenData(
                        WALLET_SUBJECT_ID, NONCE, CREDENTIAL_IDENTIFIER);
        when(mockAccessTokenService.verifyAccessToken(any())).thenReturn(mockAccessTokenData);
    }

    @Test
    void Should_ThrowAccessTokenValidationException_When_CredentialNotFound()
            throws DataStoreException {
        when(mockDynamoDbService.getStoredCredential(anyString())).thenReturn(null);
        NotificationRequestBody requestBody =
                new NotificationRequestBody(
                        NOTIFICATION_ID, EventType.credential_accepted, "Credential stored");

        AccessTokenValidationException exception =
                assertThrows(
                        AccessTokenValidationException.class,
                        () -> notificationService.processNotification(accessToken, requestBody));
        assertThat(
                exception.getMessage(),
                containsString("Credential efb52887-48d6-43b7-b14c-da7896fbf54d was not found"));
    }

    @Test
    void Should_ThrowAccessTokenValidationException_When_WalletSubjectIDsDoNotMatch()
            throws DataStoreException {
        StoredCredential mockStoredCredential =
                createMockStoredCredential(NOTIFICATION_ID, "not_the_same_wallet_subject_id");
        when(mockDynamoDbService.getStoredCredential(anyString())).thenReturn(mockStoredCredential);
        NotificationRequestBody requestBody =
                new NotificationRequestBody(
                        NOTIFICATION_ID, EventType.credential_accepted, "Credential stored");

        AccessTokenValidationException exception =
                assertThrows(
                        AccessTokenValidationException.class,
                        () -> notificationService.processNotification(accessToken, requestBody));
        assertThat(
                exception.getMessage(),
                containsString("Access token 'sub' does not match credential 'walletSubjectId'"));
    }

    @Test
    void Should_ThrowInvalidNotificationIdException_When_NotificationIDsDoNotMatch()
            throws DataStoreException {
        StoredCredential mockStoredCredential =
                createMockStoredCredential("not_the_same_notification_id", WALLET_SUBJECT_ID);
        when(mockDynamoDbService.getStoredCredential(anyString())).thenReturn(mockStoredCredential);
        NotificationRequestBody requestBody =
                new NotificationRequestBody(
                        NOTIFICATION_ID, EventType.credential_accepted, "Credential stored");

        InvalidNotificationIdException exception =
                assertThrows(
                        InvalidNotificationIdException.class,
                        () -> notificationService.processNotification(accessToken, requestBody));
        assertThat(
                exception.getMessage(),
                containsString("Request 'notification_id' does not match cached 'notificationId'"));
    }

    @Test
    void Should_MatchNotificationIDs_When_TheyAreOfDifferentCasing()
            throws DataStoreException,
                    AccessTokenValidationException,
                    InvalidNotificationIdException {
        StoredCredential mockStoredCredential =
                createMockStoredCredential(
                        "6a6bb0dc-c6cb-4fd1-8c03-08423e38802a", WALLET_SUBJECT_ID);
        when(mockDynamoDbService.getStoredCredential(anyString())).thenReturn(mockStoredCredential);
        NotificationRequestBody requestBody =
                new NotificationRequestBody(
                        "6A6BB0DC-C6CB-4FD1-8C03-08423E38802A",
                        EventType.credential_accepted,
                        "Credential stored");

        notificationService.processNotification(accessToken, requestBody);

        verify(mockLogger)
                .info(
                        "Notification received - notification_id: {}, event: {}, event_description: {}",
                        "6A6BB0DC-C6CB-4FD1-8C03-08423E38802A",
                        EventType.credential_accepted,
                        "Credential stored");
        verify(mockAccessTokenService, times(1)).verifyAccessToken(accessToken);
        verify(mockDynamoDbService, times(1)).getStoredCredential(CREDENTIAL_IDENTIFIER);
    }

    @ParameterizedTest
    @CsvSource({
        "credential_accepted, Credential stored",
        "credential_failure, Invalid credential",
        "credential_deleted, Credential deleted"
    })
    void Should_LogNotification_When_NotificationIsValid(
            EventType eventType, String eventDescription)
            throws DataStoreException,
                    AccessTokenValidationException,
                    InvalidNotificationIdException {
        StoredCredential mockStoredCredential =
                createMockStoredCredential(NOTIFICATION_ID, WALLET_SUBJECT_ID);
        when(mockDynamoDbService.getStoredCredential(anyString())).thenReturn(mockStoredCredential);
        NotificationRequestBody requestBody =
                new NotificationRequestBody(NOTIFICATION_ID, eventType, eventDescription);

        notificationService.processNotification(accessToken, requestBody);

        verify(mockLogger)
                .info(
                        "Notification received - notification_id: {}, event: {}, event_description: {}",
                        "77368ca6-877b-4208-a397-99f1df890400",
                        eventType,
                        eventDescription);
        verify(mockAccessTokenService, times(1)).verifyAccessToken(accessToken);
        verify(mockDynamoDbService, times(1)).getStoredCredential(CREDENTIAL_IDENTIFIER);
    }

    private StoredCredential createMockStoredCredential(
            String notificationId, String walletSubjectId) {
        return StoredCredential.builder()
                .credentialIdentifier(CREDENTIAL_IDENTIFIER)
                .notificationId(notificationId)
                .walletSubjectId(walletSubjectId)
                .timeToLive(TIME_TO_LIVE)
                .statusList(Optional.empty())
                .documentId(DOCUMENT_ID)
                .build();
    }
}
