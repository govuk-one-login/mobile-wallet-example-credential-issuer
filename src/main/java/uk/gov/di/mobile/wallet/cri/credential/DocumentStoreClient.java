package uk.gov.di.mobile.wallet.cri.credential;

import jakarta.ws.rs.client.Client;
import jakarta.ws.rs.core.MediaType;
import jakarta.ws.rs.core.Response;
import uk.gov.di.mobile.wallet.cri.services.ConfigurationService;

import java.net.URI;
import java.net.URISyntaxException;

public class DocumentStoreClient {

    private final ConfigurationService configurationService;
    private final Client httpClient;

    public DocumentStoreClient(ConfigurationService configurationService, Client httpClient) {
        this.configurationService = configurationService;
        this.httpClient = httpClient;
    }

    public Document getDocument(String documentId) throws DocumentStoreException {
        try {
            URI uri = buildDocumentUri(documentId);
            Response response = httpClient.target(uri).request(MediaType.APPLICATION_JSON).get();
            if (response.getStatus() != Response.Status.OK.getStatusCode()) {
                throw new DocumentStoreException(
                        String.format(
                                "Request to fetch document %s failed with status code %s",
                                documentId, response.getStatus()));
            }
            return response.readEntity(Document.class);
        } catch (URISyntaxException exception) {
            String errorMessage =
                    String.format("Invalid URI constructed for document: %s", documentId);
            throw new DocumentStoreException(errorMessage, exception);
        }
    }

    private URI buildDocumentUri(String documentId) throws URISyntaxException {
        URI credentialStoreUrl = configurationService.getCredentialStoreUrl();
        String documentEndpoint = configurationService.getDocumentEndpoint();
        return new URI(credentialStoreUrl + documentEndpoint + documentId);
    }
}
