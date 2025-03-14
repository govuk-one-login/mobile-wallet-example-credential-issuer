package uk.gov.di.mobile.wallet.cri.notification;

import com.nimbusds.jose.JOSEException;
import com.nimbusds.jose.crypto.ECDSASigner;
import com.nimbusds.jose.jwk.ECKey;
import com.nimbusds.jwt.SignedJWT;
import io.dropwizard.testing.junit5.DropwizardExtensionsSupport;
import io.dropwizard.testing.junit5.ResourceExtension;
import jakarta.ws.rs.client.Entity;
import jakarta.ws.rs.core.MediaType;
import jakarta.ws.rs.core.Response;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.extension.ExtendWith;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ValueSource;
import org.mockito.junit.jupiter.MockitoExtension;
import testUtils.MockAccessTokenBuilder;
import uk.gov.di.mobile.wallet.cri.services.ConfigurationService;

import java.text.ParseException;
import java.util.Base64;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.core.Is.is;
import static org.mockito.Mockito.*;

@ExtendWith(DropwizardExtensionsSupport.class)
@ExtendWith(MockitoExtension.class)
class NotificationResourceTest {

    private final ConfigurationService configurationService = mock(ConfigurationService.class);
    private final NotificationService notificationService = mock(NotificationService.class);
    private final ResourceExtension resource =
            ResourceExtension.builder()
                    .addResource(
                            new NotificationResource(notificationService, configurationService))
                    .build();
    SignedJWT mockAccessToken;

    @BeforeEach
    void setUp() throws ParseException, JOSEException {
        ECDSASigner ecSigner = new ECDSASigner(getEsKey());
        mockAccessToken = new MockAccessTokenBuilder("ES256").build();
        mockAccessToken.sign(ecSigner);
        when(configurationService.getOneLoginAuthServerUrl())
                .thenReturn("https://test-authorization-server.gov.uk");
        when(configurationService.getSelfUrl()).thenReturn("https://test-credential-issuer.gov.uk");
    }

    @ParameterizedTest
    @ValueSource(
            strings = {
                "{\"event\":\"credential_accepted\"}", // 'notification_id' missing
                "{\"notification_id\":\"\",\"event\":\"credential_accepted\"}", // 'notification_id'
                // is falsy
                "{\"notification_id\":\"123\",\"event\":\"credential_accepted\"}", // 'notification_id' is not UUID
            })
    void Should_Return400_When_NotificationIdIsInvalid(String requestBody) throws JOSEException {
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

    private ECKey getEsKey() throws ParseException {
        String privateKeyJwkBase64 =
                "eyJrdHkiOiJFQyIsImQiOiI4SGJYN0xib1E1OEpJOGo3eHdfQXp0SlRVSDVpZTFtNktIQlVmX3JnakVrIiwidXNlIjoic2lnIiwiY3J2IjoiUC0yNTYiLCJraWQiOiJmMDYxMWY3Zi04YTI5LTQ3ZTEtYmVhYy1mNWVlNWJhNzQ3MmUiLCJ4IjoiSlpKeE83b2JSOElzdjU4NUVzaWcwYlAwQUdfb1N6MDhSMS11VXBiYl9JRSIsInkiOiJtNjBRMmtMMExiaEhTbHRjS1lyTG8wczE1M1hveF9tVDV2UlV6Z3g4TWtFIiwiaWF0IjoxNzEyMTQ2MTc5fQ==";
        return ECKey.parse(new String(Base64.getDecoder().decode(privateKeyJwkBase64)));
    }
}
