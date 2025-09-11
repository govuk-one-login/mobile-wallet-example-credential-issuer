package uk.gov.di.mobile.wallet.cri.credential;

import jakarta.ws.rs.client.Client;
import jakarta.ws.rs.client.Entity;
import jakarta.ws.rs.core.MediaType;
import jakarta.ws.rs.core.Response;
import uk.gov.di.mobile.wallet.cri.services.ConfigurationService;

public class StatusListClient {

    public static record IssueResponse(int idx, String uri) {
        public IssueResponse {
            if (idx < 0) {
                throw new IllegalArgumentException("Index must be non-negative, got: " + idx);
            }
            if (uri == null || uri.trim().isEmpty()) {
                throw new IllegalArgumentException("URI cannot be null or empty");
            }
            uri = uri.trim();
        }
    }

    private static final String ENDPOINT_GET_INDEX = "/get-index";
    private static final String ENDPOINT_REVOKE = "/revoke";

    private final ConfigurationService configurationService;
    private final Client httpClient;

    public StatusListClient(ConfigurationService configurationService, Client httpClient) {
        this.configurationService = configurationService;
        this.httpClient = httpClient;
    }

    public IssueResponse getIndex(String token) throws StatusListException {
        String url = buildUrl(ENDPOINT_GET_INDEX);

        Response response =
                httpClient
                        .target(url)
                        .request(MediaType.APPLICATION_JSON)
                        .post(Entity.entity(token, MediaType.APPLICATION_JSON));

        if (response.getStatus() != Response.Status.OK.getStatusCode()) {
            throw new StatusListException(
                    String.format(
                            "Request to get credential index failed with status code %s",
                            response.getStatus()));
        }
        return response.readEntity(IssueResponse.class);
    }

    public void revoke(String token) throws StatusListException {
        String url = buildUrl(ENDPOINT_REVOKE);

        Response response =
                httpClient
                        .target(url)
                        .request(MediaType.APPLICATION_JSON)
                        .post(Entity.entity(token, MediaType.APPLICATION_JSON));

        if (response.getStatus() != Response.Status.ACCEPTED.getStatusCode()) {
            throw new StatusListException(
                    String.format(
                            "Request to revoke credential failed with status code %s",
                            response.getStatus()));
        }
    }

    private String buildUrl(String endpoint) {
        String baseUrl = configurationService.getStatusListUrl(); // Fixed typo
        return baseUrl.endsWith("/") ? baseUrl + endpoint.substring(1) : baseUrl + endpoint;
    }
}
