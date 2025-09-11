package uk.gov.di.mobile.wallet.cri.credential;

import jakarta.ws.rs.client.Client;
import jakarta.ws.rs.client.Entity;
import jakarta.ws.rs.core.MediaType;
import jakarta.ws.rs.core.Response;
import uk.gov.di.mobile.wallet.cri.services.ConfigurationService;
import uk.gov.di.mobile.wallet.cri.services.signing.SigningException;

public class StatusListClient {

    public record IssueResponse(int idx, String uri) {}

    private static final String ENDPOINT_ISSUE = "/issue";
    private static final String ENDPOINT_REVOKE = "/revoke";

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

    public IssueResponse getIndex(long credentialExpiry)
            throws StatusListException, SigningException {
        String token = tokenBuilder.buildIssueToken(credentialExpiry);
        String url = buildUrl(ENDPOINT_ISSUE);

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

    public void revoke(String uri, int index) throws StatusListException, SigningException {
        String token = tokenBuilder.buildRevokeToken(uri, index);
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
        String baseUrl = configurationService.getStatusListUrl();
        return baseUrl + endpoint;
    }
}
