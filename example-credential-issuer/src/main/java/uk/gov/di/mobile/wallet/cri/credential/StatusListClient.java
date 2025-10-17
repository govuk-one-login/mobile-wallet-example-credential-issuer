package uk.gov.di.mobile.wallet.cri.credential;

import jakarta.ws.rs.client.Client;
import jakarta.ws.rs.client.Entity;
import jakarta.ws.rs.core.MediaType;
import jakarta.ws.rs.core.Response;
import uk.gov.di.mobile.wallet.cri.services.ConfigurationService;
import uk.gov.di.mobile.wallet.cri.services.signing.SigningException;

import java.net.URI;

public class StatusListClient {

    public record StatusListInformation(Integer idx, String uri) {}

    private static final String ENDPOINT_ISSUE = "/issue";

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

    public StatusListInformation getIndex(long credentialExpiry)
            throws StatusListException, SigningException {
        String token = tokenBuilder.buildIssueToken(credentialExpiry);
        String url = buildUrl();

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
        return response.readEntity(StatusListInformation.class);
    }

    private String buildUrl() {
        URI baseUrl = configurationService.getStatusListUrl();
        return baseUrl + StatusListClient.ENDPOINT_ISSUE;
    }
}
