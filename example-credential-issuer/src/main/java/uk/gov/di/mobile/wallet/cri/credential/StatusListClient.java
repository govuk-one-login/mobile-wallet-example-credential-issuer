package uk.gov.di.mobile.wallet.cri.credential;

import jakarta.ws.rs.client.Client;
import jakarta.ws.rs.client.Entity;
import jakarta.ws.rs.core.MediaType;
import jakarta.ws.rs.core.Response;
import uk.gov.di.mobile.wallet.cri.services.ConfigurationService;

import java.net.URI;

public class StatusListClient {

    public record StatusListInformation(Integer idx, String uri) {}

    public record RevokeResponse(String message, Long revokedAt) {}

    private static final String ISSUE_ENDPOINT = "/issue";
    private static final String REVOKE_ENDPOINT = "/revoke";

    private final ConfigurationService configurationService;
    private final Client httpClient;
    private final StatusListRequestTokenBuilder tokenBuilder;

    public StatusListClient(
            ConfigurationService configurationService,
            Client httpClient,
            StatusListRequestTokenBuilder tokenBuilder) {
        this.configurationService = configurationService;
        this.httpClient = httpClient;
        this.tokenBuilder = tokenBuilder;
    }

    public StatusListInformation getIndex(long credentialExpiry) throws StatusListClientException {
        try {
            String token = tokenBuilder.buildIssueToken(credentialExpiry);
            String url = buildUrl(ISSUE_ENDPOINT);

            Response response =
                    httpClient
                            .target(url)
                            .request(MediaType.APPLICATION_JSON)
                            .post(Entity.entity(token, MediaType.APPLICATION_JSON));

            if (response.getStatus() != Response.Status.OK.getStatusCode()) {
                throw new StatusListClientException(
                        String.format(
                                "Request to get credential index failed with status code %s",
                                response.getStatus()));
            }
            return response.readEntity(StatusListInformation.class);
        } catch (Exception exception) {
            if (exception instanceof StatusListClientException) {
                throw (StatusListClientException) exception;
            }
            throw new StatusListClientException("Failed to get credential index", exception);
        }
    }

    public RevokeResponse revokeCredential(int index, String uri) throws StatusListClientException {
        try {
            String token = tokenBuilder.buildRevokeToken(index, uri);
            String url = buildUrl(REVOKE_ENDPOINT);

            Response response =
                    httpClient
                            .target(url)
                            .request(MediaType.APPLICATION_JSON)
                            .post(Entity.entity(token, MediaType.APPLICATION_JSON));

            if (response.getStatus() != Response.Status.ACCEPTED.getStatusCode()) {
                throw new StatusListClientException(
                        String.format(
                                "Request to revoke credential failed with status code %s",
                                response.getStatus()));
            }
            return response.readEntity(RevokeResponse.class);
        } catch (Exception exception) {
            if (exception instanceof StatusListClientException) {
                throw (StatusListClientException) exception;
            }
            throw new StatusListClientException("Failed to revoke credential", exception);
        }
    }

    private String buildUrl(String endpoint) {
        URI baseUrl = configurationService.getStatusListUrl();
        return baseUrl + endpoint;
    }
}
