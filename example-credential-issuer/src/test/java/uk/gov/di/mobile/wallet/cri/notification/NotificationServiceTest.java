package uk.gov.di.mobile.wallet.cri.notification;

import com.nimbusds.jose.JOSEException;
import com.nimbusds.jose.crypto.ECDSASigner;
import com.nimbusds.jwt.SignedJWT;
import io.dropwizard.testing.junit5.DropwizardExtensionsSupport;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.TestInstance;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.slf4j.Logger;
import testUtils.MockAccessTokenBuilder;
import uk.gov.di.mobile.wallet.cri.models.StoredCredential;
import uk.gov.di.mobile.wallet.cri.services.authentication.AccessTokenService;
import uk.gov.di.mobile.wallet.cri.services.authentication.AccessTokenValidationException;
import uk.gov.di.mobile.wallet.cri.services.data_storage.DataStoreException;
import uk.gov.di.mobile.wallet.cri.services.data_storage.DynamoDbService;

import java.text.ParseException;

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
    private static final Long TIME_TO_LIVE = 12345L;
    private static final String DOCUMENT_PRIMARY_IDENTIFIER =
            "cb2e831f-b2d9-4c7a-b42e-be5370ea4c77";

    @Mock private DynamoDbService mockDynamoDbService;
    @Mock private AccessTokenService mockAccessTokenService;
    @Mock private Logger mockLogger;

    private NotificationService notificationService;
    private SignedJWT accessToken;
    private NotificationRequestBody requestBody;

    @BeforeEach
    void setUp() throws ParseException, JOSEException, AccessTokenValidationException {
        ECDSASigner ecSigner = new ECDSASigner(getEcKey());
        accessToken = new MockAccessTokenBuilder("ES256").build();
        accessToken.sign(ecSigner);

        requestBody =
                new NotificationRequestBody(
                        NOTIFICATION_ID, EventType.credential_accepted, "Credential stored");

        notificationService =
                new NotificationService(mockDynamoDbService, mockAccessTokenService) {
                    @Override
                    protected Logger getLogger() {
                        return mockLogger;
                    }
                };

        AccessTokenService.AccessTokenData mockAccessTokenData =
                new AccessTokenService.AccessTokenData(
                        WALLET_SUBJECT_ID,
                        "134e0c41-a8b4-46d4-aec8-cd547e125589",
                        CREDENTIAL_IDENTIFIER);
        when(mockAccessTokenService.verifyAccessToken(any())).thenReturn(mockAccessTokenData);
    }

    @Test
    void Should_ThrowAccessTokenValidationException_When_WalletSubjectIDsDoNotMatch()
            throws DataStoreException {
        StoredCredential mockStoredCredential =
                StoredCredential.builder()
                        .credentialIdentifier(CREDENTIAL_IDENTIFIER)
                        .notificationId(NOTIFICATION_ID)
                        .walletSubjectId("not_the_same_wallet_subject_id")
                        .timeToLive(TIME_TO_LIVE)
                        .statusList(null)
                        .documentPrimaryIdentifier(DOCUMENT_PRIMARY_IDENTIFIER)
                        .build();
        when(mockDynamoDbService.getStoredCredential(anyString())).thenReturn(mockStoredCredential);

        AccessTokenValidationException exception =
                assertThrows(
                        AccessTokenValidationException.class,
                        () -> notificationService.processNotification(accessToken, requestBody));

        assertThat(
                exception.getMessage(),
                containsString("Access token 'sub' does not match credential 'walletSubjectId'"));
    }

    @Test
    void Should_ThrowAccessTokenValidationException_When_CredentialNotFound()
            throws DataStoreException {
        when(mockDynamoDbService.getStoredCredential(anyString())).thenReturn(null);

        AccessTokenValidationException exception =
                assertThrows(
                        AccessTokenValidationException.class,
                        () -> notificationService.processNotification(accessToken, requestBody));

        assertThat(
                exception.getMessage(),
                containsString("Credential efb52887-48d6-43b7-b14c-da7896fbf54d was not found"));
    }

    @Test
    void Should_ThrowInvalidNotificationIdException_When_NotificationIDsDoNotMatch()
            throws DataStoreException {
        StoredCredential mockStoredCredential =
                StoredCredential.builder()
                        .credentialIdentifier(CREDENTIAL_IDENTIFIER)
                        .notificationId(NOTIFICATION_ID)
                        .walletSubjectId(WALLET_SUBJECT_ID)
                        .timeToLive(TIME_TO_LIVE)
                        .statusList(null)
                        .documentPrimaryIdentifier(DOCUMENT_PRIMARY_IDENTIFIER)
                        .build();

        when(mockDynamoDbService.getStoredCredential(anyString())).thenReturn(mockStoredCredential);

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

//    @Test
//    void Should_LogNotification_When_RequestIsValid()
//            throws DataStoreException,
//                    AccessTokenValidationException,
//                    InvalidNotificationIdException {
//        StoredCredential mockStoredCredential =
//                StoredCredential.builder()
//                        .credentialIdentifier(CREDENTIAL_IDENTIFIER)
//                        .notificationId(NOTIFICATION_ID)
//                        .walletSubjectId(WALLET_SUBJECT_ID)
//                        .timeToLive(TIME_TO_LIVE)
//                        .statusList(null)
//                        .documentPrimaryIdentifier(DOCUMENT_PRIMARY_IDENTIFIER)
//                        .build();
//        when(mockDynamoDbService.getStoredCredential(anyString())).thenReturn(mockStoredCredential);
//
//        notificationService.processNotification(accessToken, requestBody);
//
//        verify(mockLogger)
//                .info(
//                        "Notification received - notification_id: {}, event: {}, event_description: {}",
//                        "77368ca6-877b-4208-a397-99f1df890400",
//                        EventType.credential_accepted,
//                        "Credential stored");
//
//        verify(mockAccessTokenService, times(1)).verifyAccessToken(accessToken);
//        verify(mockDynamoDbService, times(1)).getStoredCredential(CREDENTIAL_IDENTIFIER);
//    }
}
