package uk.gov.di.mobile.wallet.cri.credential;

import jakarta.ws.rs.client.Client;
import jakarta.ws.rs.client.Entity;
import jakarta.ws.rs.client.Invocation;
import jakarta.ws.rs.client.WebTarget;
import jakarta.ws.rs.core.MediaType;
import jakarta.ws.rs.core.Response;
import org.junit.jupiter.api.BeforeEach;
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

    @BeforeEach
    void setUp() {
        statusListClient = new StatusListClient(configurationService, httpClient, tokenBuilder);
    }

    @Test
    void Should_ReturnStatusListInformation_On_SuccessfulRequest() throws Exception {
        long credentialExpiry = 1234567890L;
        String token = "mock-issue-token";
        URI baseUrl = new URI("https://status-list.test.com");
        String expectedUrl = baseUrl + "/issue";
        StatusListClient.StatusListInformation expectedResponse =
                new StatusListClient.StatusListInformation(
                        0, "https://test-status-list.gov.uk/t/3B0F3BD087A7");

        when(tokenBuilder.buildIssueToken(credentialExpiry)).thenReturn(token);
        when(configurationService.getStatusListUrl()).thenReturn(baseUrl);
        when(httpClient.target(expectedUrl)).thenReturn(webTarget);
        when(webTarget.request(MediaType.APPLICATION_JSON)).thenReturn(requestBuilder);
        when(requestBuilder.post(Entity.entity(token, MediaType.APPLICATION_JSON)))
                .thenReturn(response);
        when(response.getStatus()).thenReturn(Response.Status.OK.getStatusCode());
        when(response.readEntity(StatusListClient.StatusListInformation.class))
                .thenReturn(expectedResponse);

        StatusListClient.StatusListInformation result = statusListClient.getIndex(credentialExpiry);

        assertEquals(expectedResponse, result);
        verify(tokenBuilder).buildIssueToken(credentialExpiry);
        verify(configurationService).getStatusListUrl();
        verify(httpClient).target(expectedUrl);
        verify(webTarget).request(MediaType.APPLICATION_JSON);
        verify(requestBuilder).post(Entity.entity(token, MediaType.APPLICATION_JSON));
    }

    @Test
    void Should_ThrowStatusListException_On_Non200Response() throws Exception {
        long credentialExpiry = 1234567890L;
        String token = "mock-issue-token";
        URI baseUrl = new URI("https://status-list.test.com");

        when(tokenBuilder.buildIssueToken(credentialExpiry)).thenReturn(token);
        when(configurationService.getStatusListUrl()).thenReturn(baseUrl);
        when(httpClient.target(anyString())).thenReturn(webTarget);
        when(webTarget.request(MediaType.APPLICATION_JSON)).thenReturn(requestBuilder);
        when(requestBuilder.post(any())).thenReturn(response);
        when(response.getStatus()).thenReturn(500);

        StatusListException exception =
                assertThrows(
                        StatusListException.class,
                        () -> statusListClient.getIndex(credentialExpiry));

        assertEquals(
                "Request to get credential index failed with status code 500",
                exception.getMessage());
        verify(response, never()).readEntity((Class<Object>) any());
    }

    @Test
    void Should_PropagateSigningException() throws Exception {
        long credentialExpiry = 1234567890L;
        SigningException expectedException =
                new SigningException("Some signing error", new RuntimeException());

        when(tokenBuilder.buildIssueToken(credentialExpiry)).thenThrow(expectedException);

        SigningException exception =
                assertThrows(
                        SigningException.class, () -> statusListClient.getIndex(credentialExpiry));

        assertEquals(expectedException, exception);
        verifyNoInteractions(httpClient);
    }
}
