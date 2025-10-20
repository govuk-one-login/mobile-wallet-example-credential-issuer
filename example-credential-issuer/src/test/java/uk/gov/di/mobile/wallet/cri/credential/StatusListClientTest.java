package uk.gov.di.mobile.wallet.cri.credential;

import jakarta.ws.rs.client.Client;
import jakarta.ws.rs.client.Entity;
import jakarta.ws.rs.client.Invocation;
import jakarta.ws.rs.client.WebTarget;
import jakarta.ws.rs.core.MediaType;
import jakarta.ws.rs.core.Response;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import uk.gov.di.mobile.wallet.cri.services.ConfigurationService;
import uk.gov.di.mobile.wallet.cri.services.signing.SigningException;

import java.net.URI;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoInteractions;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class StatusListClientTest {

    @Mock private ConfigurationService configurationService;
    @Mock private Client httpClient;
    @Mock private StatusListRequestTokenBuilder tokenBuilder;
    @Mock private WebTarget webTarget;
    @Mock private Invocation.Builder requestBuilder;
    @Mock private Response response;

    private StatusListClient statusListClient;

    private static final URI BASE_URL = URI.create("https://status-list.test.com");
    private static final String MOCK_TOKEN = "mock-token";
    private static final long CREDENTIAL_EXPIRY = 1234567890L;
    private static final int INDEX = 5;
    private static final String STATUS_LIST_URI = "https://status-list.test.com/t/12345";

    @BeforeEach
    void setUp() {
        statusListClient = new StatusListClient(configurationService, httpClient, tokenBuilder);
    }

    @Nested
    class GetIndexTests {

        @Test
        void shouldReturnIssueResponseOnSuccess() throws Exception {
            when(tokenBuilder.buildIssueToken(CREDENTIAL_EXPIRY)).thenReturn(MOCK_TOKEN);
            when(configurationService.getStatusListUrl()).thenReturn(BASE_URL);
            String expectedUrl = BASE_URL + "/issue";
            when(httpClient.target(expectedUrl)).thenReturn(webTarget);
            when(webTarget.request(MediaType.APPLICATION_JSON)).thenReturn(requestBuilder);
            when(requestBuilder.post(Entity.entity(MOCK_TOKEN, MediaType.APPLICATION_JSON)))
                    .thenReturn(response);
            when(response.getStatus()).thenReturn(Response.Status.OK.getStatusCode());
            StatusListClient.StatusListInformation expectedResponse =
                    new StatusListClient.StatusListInformation(INDEX, STATUS_LIST_URI);
            when(response.readEntity(StatusListClient.StatusListInformation.class))
                    .thenReturn(expectedResponse);

            StatusListClient.StatusListInformation result =
                    statusListClient.getIndex(CREDENTIAL_EXPIRY);

            assertEquals(expectedResponse, result);
            verify(tokenBuilder).buildIssueToken(CREDENTIAL_EXPIRY);
            verify(configurationService).getStatusListUrl();
            verify(httpClient).target(expectedUrl);
            verify(webTarget).request(MediaType.APPLICATION_JSON);
            verify(requestBuilder).post(Entity.entity(MOCK_TOKEN, MediaType.APPLICATION_JSON));
        }

        @Test
        void shouldThrowExceptionOnNon200Response() throws Exception {
            when(tokenBuilder.buildIssueToken(CREDENTIAL_EXPIRY)).thenReturn(MOCK_TOKEN);
            when(configurationService.getStatusListUrl()).thenReturn(BASE_URL);
            when(httpClient.target(anyString())).thenReturn(webTarget);
            when(webTarget.request(MediaType.APPLICATION_JSON)).thenReturn(requestBuilder);
            when(requestBuilder.post(any())).thenReturn(response);
            when(response.getStatus()).thenReturn(500);

            StatusListClientException exception =
                    assertThrows(
                            StatusListClientException.class,
                            () -> statusListClient.getIndex(CREDENTIAL_EXPIRY));
            assertEquals(
                    "Request to get credential index failed with status code 500",
                    exception.getMessage());
            verify(response, never()).readEntity((Class<Object>) any());
        }

        @Test
        void shouldThrowStatusListClientExceptionOnSigningException() throws Exception {
            SigningException signingException =
                    new SigningException("Signing error", new RuntimeException());
            when(tokenBuilder.buildIssueToken(CREDENTIAL_EXPIRY)).thenThrow(signingException);

            StatusListClientException exception =
                    assertThrows(
                            StatusListClientException.class,
                            () -> statusListClient.getIndex(CREDENTIAL_EXPIRY));

            assertEquals("Failed to get credential index", exception.getMessage());
            assertEquals(SigningException.class, exception.getCause().getClass());
            assertEquals("Signing error", exception.getCause().getMessage());
            verifyNoInteractions(httpClient);
        }
    }

    @Nested
    class RevokeCredentialTests {

        @Test
        void shouldReturnRevokeResponseOnSuccess() throws Exception {
            when(tokenBuilder.buildRevokeToken(INDEX, STATUS_LIST_URI)).thenReturn(MOCK_TOKEN);
            when(configurationService.getStatusListUrl()).thenReturn(BASE_URL);
            String expectedUrl = BASE_URL + "/revoke";
            when(httpClient.target(expectedUrl)).thenReturn(webTarget);
            when(webTarget.request(MediaType.APPLICATION_JSON)).thenReturn(requestBuilder);
            when(requestBuilder.post(Entity.entity(MOCK_TOKEN, MediaType.APPLICATION_JSON)))
                    .thenReturn(response);
            when(response.getStatus()).thenReturn(Response.Status.ACCEPTED.getStatusCode());
            StatusListClient.RevokeResponse expectedResponse =
                    new StatusListClient.RevokeResponse("Credential revoked", 1760550306L);
            when(response.readEntity(StatusListClient.RevokeResponse.class))
                    .thenReturn(expectedResponse);

            StatusListClient.RevokeResponse result =
                    statusListClient.revokeCredential(INDEX, STATUS_LIST_URI);

            assertEquals(expectedResponse, result);
            verify(tokenBuilder).buildRevokeToken(INDEX, STATUS_LIST_URI);
            verify(configurationService).getStatusListUrl();
            verify(httpClient).target(expectedUrl);
            verify(webTarget).request(MediaType.APPLICATION_JSON);
            verify(requestBuilder).post(Entity.entity(MOCK_TOKEN, MediaType.APPLICATION_JSON));
        }

        @Test
        void ShouldThrowExceptionOnNon202Response() throws Exception {
            when(tokenBuilder.buildRevokeToken(INDEX, STATUS_LIST_URI)).thenReturn(MOCK_TOKEN);
            when(configurationService.getStatusListUrl()).thenReturn(BASE_URL);
            when(httpClient.target(anyString())).thenReturn(webTarget);
            when(webTarget.request(MediaType.APPLICATION_JSON)).thenReturn(requestBuilder);
            when(requestBuilder.post(any())).thenReturn(response);
            when(response.getStatus()).thenReturn(500);

            StatusListClientException exception =
                    assertThrows(
                            StatusListClientException.class,
                            () -> statusListClient.revokeCredential(INDEX, STATUS_LIST_URI));
            assertEquals(
                    "Request to revoke credential failed with status code 500",
                    exception.getMessage());
            verify(response, never()).readEntity((Class<Object>) any());
        }

        @Test
        void shouldThrowStatusListClientExceptionOnSigningException() throws Exception {
            SigningException signingException =
                    new SigningException("Signing error", new RuntimeException());
            when(tokenBuilder.buildRevokeToken(INDEX, STATUS_LIST_URI)).thenThrow(signingException);

            StatusListClientException exception =
                    assertThrows(
                            StatusListClientException.class,
                            () -> statusListClient.revokeCredential(INDEX, STATUS_LIST_URI));

            assertEquals("Failed to revoke credential", exception.getMessage());
            assertEquals(SigningException.class, exception.getCause().getClass());
            assertEquals("Signing error", exception.getCause().getMessage());
            verifyNoInteractions(httpClient);
        }
    }
}
