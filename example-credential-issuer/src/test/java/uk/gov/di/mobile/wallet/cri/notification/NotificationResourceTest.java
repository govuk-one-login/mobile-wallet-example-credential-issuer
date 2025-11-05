package uk.gov.di.mobile.wallet.cri.notification;

import com.nimbusds.jose.JOSEException;
import com.nimbusds.jose.crypto.ECDSASigner;
import com.nimbusds.jwt.SignedJWT;
import io.dropwizard.testing.junit5.DropwizardExtensionsSupport;
import io.dropwizard.testing.junit5.ResourceExtension;
import jakarta.ws.rs.client.Entity;
import jakarta.ws.rs.core.MediaType;
import jakarta.ws.rs.core.Response;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.TestInstance;
import org.junit.jupiter.api.extension.ExtendWith;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.MethodSource;
import org.junit.jupiter.params.provider.ValueSource;
import org.mockito.Mockito;
import org.mockito.junit.jupiter.MockitoExtension;
import testUtils.MockAccessTokenBuilder;
import uk.gov.di.mobile.wallet.cri.services.authentication.AccessTokenValidationException;
import uk.gov.di.mobile.wallet.cri.services.data_storage.DataStoreException;

import java.text.ParseException;
import java.util.stream.Stream;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.core.Is.is;
import static org.mockito.Mockito.*;
import static testUtils.EcKeyHelper.getEcKey;

@TestInstance(TestInstance.Lifecycle.PER_CLASS)
@ExtendWith(DropwizardExtensionsSupport.class)
@ExtendWith(MockitoExtension.class)
class NotificationResourceTest {

    private final NotificationService notificationService = mock(NotificationService.class);
    private final ResourceExtension resource =
            ResourceExtension.builder()
                    .addResource(new NotificationResource(notificationService))
                    .build();
    private SignedJWT mockAccessToken = new MockAccessTokenBuilder("ES256").build();

    @BeforeEach
    void setUp() throws ParseException, JOSEException {
        ECDSASigner ecSigner = new ECDSASigner(getEcKey());
        mockAccessToken = new MockAccessTokenBuilder("ES256").build();
        mockAccessToken.sign(ecSigner);
        Mockito.reset(notificationService);
    }

    @ParameterizedTest
    @ValueSource(
            strings = {
                "{\"event\":\"credential_accepted\"}", // 'notification_id' is missing
                "{\"notification_id\":\"\",\"event\":\"credential_accepted\"}", // 'notification_id'
                // is empty string
                "{\"notification_id\":\"123\",\"event\":\"credential_accepted\"}", // 'notification_id' is not UUID
            })
    void Should_Return400_When_NotificationIdIsInvalid(String requestBody) {
        final Response response =
                resource.target("/notification")
                        .request()
                        .header("Authorization", "Bearer " + mockAccessToken.serialize())
                        .post(Entity.entity(requestBody, MediaType.APPLICATION_JSON));

        assertThat(response.getStatus(), is(400));
        assertThat(
                response.readEntity(String.class), is("{\"error\":\"invalid_notification_id\"}"));
    }

    @ParameterizedTest
    @ValueSource(
            strings = {
                "", // no request body
                "{\"notification_id\":\"77368ca6-877b-4208-a397-99f1df890400\"}", // 'event' is
                // missing
                "{\"notification_id\":\"77368ca6-877b-4208-a397-99f1df890400\",\"event\":\"something_else\"}", // 'event' has an invalid value
                "{\"notification_id\":\"77368ca6-877b-4208-a397-99f1df890400\",\"event\":\"credential_accepted\",\"event_description\":\"Credential stored$%£\"}", // 'event_description' contains non-ASCII characters
            })
    void Should_Return400_When_RequestBodyIsInvalid(String requestBody) {
        final Response response =
                resource.target("/notification")
                        .request()
                        .header("Authorization", "Bearer " + mockAccessToken.serialize())
                        .post(Entity.entity(requestBody, MediaType.APPLICATION_JSON));

        assertThat(response.getStatus(), is(400));
        assertThat(
                response.readEntity(String.class),
                is("{\"error\":\"invalid_notification_request\"}"));
    }

    @Test
    void Should_Return401_When_AuthorizationHeaderIsMissing() {
        final Response response =
                resource.target("/notification")
                        .request()
                        .post(
                                Entity.entity(
                                        "{\"notification_id\":\"77368ca6-877b-4208-a397-99f1df890400\",\"event\":\"credential_accepted\",\"event_description\":\"Credential stored\"}",
                                        MediaType.APPLICATION_JSON));

        assertThat(response.getStatus(), is(401));
        assertThat(response.getHeaderString("WWW-Authenticate"), is("Bearer"));
    }

    @ParameterizedTest
    @MethodSource("provideAuthorizationHeaders")
    void Should_Return401_When_AuthorizationHeaderIsInvalid(String authorizationHeader) {
        final Response response =
                resource.target("/notification")
                        .request()
                        .header("Authorization", authorizationHeader)
                        .post(
                                Entity.entity(
                                        "{\"notification_id\":\"77368ca6-877b-4208-a397-99f1df890400\",\"event\":\"credential_accepted\",\"event_description\":\"Credential stored\"}",
                                        MediaType.APPLICATION_JSON));

        assertThat(response.getStatus(), is(401));
        assertThat(
                response.getHeaderString("WWW-Authenticate"), is("Bearer error=\"invalid_token\""));
    }

    private Stream<String> provideAuthorizationHeaders() throws ParseException, JOSEException {
        ECDSASigner ecSigner = new ECDSASigner(getEcKey());
        mockAccessToken = new MockAccessTokenBuilder("ES256").build();
        mockAccessToken.sign(ecSigner);

        return Stream.of(
                "Bearer"
                        + this.mockAccessToken
                                .serialize(), // no space between 'Bearer' and access token
                this.mockAccessToken.serialize(), // no 'Bearer ' before access token
                "Bearer invalid token" // invalid access token
                );
    }

    @Test
    void Should_Return401_When_NotificationServiceThrowsAccessTokenValidationException()
            throws DataStoreException,
                    AccessTokenValidationException,
                    InvalidNotificationIdException {
        doThrow(new AccessTokenValidationException("Invalid access token"))
                .when(notificationService)
                .processNotification(any(), any());

        final Response response =
                resource.target("/notification")
                        .request()
                        .header("Authorization", "Bearer " + mockAccessToken.serialize())
                        .post(
                                Entity.entity(
                                        "{\"notification_id\":\"77368ca6-877b-4208-a397-99f1df890400\",\"event\":\"credential_accepted\",\"event_description\":\"Credential stored\"}",
                                        MediaType.APPLICATION_JSON));

        assertThat(response.getStatus(), is(401));
        assertThat(
                response.getHeaderString("WWW-Authenticate"), is("Bearer error=\"invalid_token\""));
    }

    @Test
    void Should_Return400_When_NotificationServiceThrowsInvalidNotificationIdException()
            throws DataStoreException,
                    AccessTokenValidationException,
                    InvalidNotificationIdException {
        doThrow(new InvalidNotificationIdException("Invalid notification_id"))
                .when(notificationService)
                .processNotification(any(), any());

        final Response response =
                resource.target("/notification")
                        .request()
                        .header("Authorization", "Bearer " + mockAccessToken.serialize())
                        .post(
                                Entity.entity(
                                        "{\"notification_id\":\"77368ca6-877b-4208-a397-99f1df890400\",\"event\":\"credential_accepted\",\"event_description\":\"Credential stored\"}",
                                        MediaType.APPLICATION_JSON));

        assertThat(response.getStatus(), is(400));
    }

    @Test
    void Should_Return500_When_NotificationServiceThrowsDataStoreException()
            throws DataStoreException,
                    AccessTokenValidationException,
                    InvalidNotificationIdException {
        doThrow(new DataStoreException("Some error"))
                .when(notificationService)
                .processNotification(any(), any());

        final Response response =
                resource.target("/notification")
                        .request()
                        .header("Authorization", "Bearer " + mockAccessToken.serialize())
                        .post(
                                Entity.entity(
                                        "{\"notification_id\":\"77368ca6-877b-4208-a397-99f1df890400\",\"event\":\"credential_accepted\",\"event_description\":\"Credential stored\"}",
                                        MediaType.APPLICATION_JSON));

        assertThat(response.getStatus(), is(500));
        assertThat(response.readEntity(String.class), is(""));
    }

    @Test
    void Should_Return204_When_RequestIsValid() {
        final Response response =
                resource.target("/notification")
                        .request()
                        .header("Authorization", "Bearer " + mockAccessToken.serialize())
                        .post(
                                Entity.entity(
                                        "{\"notification_id\":\"77368ca6-877b-4208-a397-99f1df890400\",\"event\":\"credential_accepted\",\"event_description\":\"Credential stored\"}",
                                        MediaType.APPLICATION_JSON));

        assertThat(response.getStatus(), is(204));
    }
}
