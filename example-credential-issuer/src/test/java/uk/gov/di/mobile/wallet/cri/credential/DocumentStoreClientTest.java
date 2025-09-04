package uk.gov.di.mobile.wallet.cri.credential;

import jakarta.ws.rs.client.Client;
import jakarta.ws.rs.client.Invocation;
import jakarta.ws.rs.client.WebTarget;
import jakarta.ws.rs.core.MediaType;
import jakarta.ws.rs.core.Response;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import uk.gov.di.mobile.wallet.cri.credential.domain.Document;
import uk.gov.di.mobile.wallet.cri.credential.exceptions.DocumentStoreException;
import uk.gov.di.mobile.wallet.cri.services.ConfigurationService;

import java.net.URI;
import java.net.URISyntaxException;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class DocumentStoreClientTest {
    @Mock private Client mockHttpClient;
    @Mock private WebTarget mockWebTarget;
    @Mock private Invocation.Builder mockInvocationBuilder;
    @Mock private Response mockResponse;
    @Mock
    Document mockDocument;
    @Mock private ConfigurationService mockConfigurationService;
    private DocumentStoreClient documentStoreClient;

    @BeforeEach
    void setUp() {
        documentStoreClient = new DocumentStoreClient(mockConfigurationService, mockHttpClient);
    }

    @Test
    void Should_ReturnDocument_When_DocumentAPIReturns200()
            throws URISyntaxException, DocumentStoreException {
        String documentId = "672ca5d0-818a-46b6-946a-f9481023e803";
        URI credentialStoreUrl = new URI("https://test-example-cri.com");
        String documentEndpoint = "/documents/";
        URI expectedUri = new URI(credentialStoreUrl + documentEndpoint + documentId);
        when(mockConfigurationService.getCredentialStoreUrl()).thenReturn(credentialStoreUrl);
        when(mockConfigurationService.getDocumentEndpoint()).thenReturn(documentEndpoint);
        when(mockHttpClient.target(any(URI.class))).thenReturn(mockWebTarget);
        when(mockWebTarget.request(MediaType.APPLICATION_JSON)).thenReturn(mockInvocationBuilder);
        when(mockInvocationBuilder.get()).thenReturn(mockResponse);
        when(mockResponse.getStatus()).thenReturn(200);
        when(mockResponse.readEntity(Document.class)).thenReturn(mockDocument);

        Document document = documentStoreClient.getDocument(documentId);

        assertEquals(mockDocument, document);
        verify(mockHttpClient).target(expectedUri);
    }

    @Test
    void Should_ThrowDocumentStoreException_When_DocumentAPIReturns500() throws URISyntaxException {
        String documentId = "672ca5d0-818a-46b6-946a-f9481023e803";
        URI credentialStoreUrl = new URI("https://test-example-cri.com");
        String documentEndpoint = "/documents/";
        when(mockConfigurationService.getCredentialStoreUrl()).thenReturn(credentialStoreUrl);
        when(mockConfigurationService.getDocumentEndpoint()).thenReturn(documentEndpoint);
        when(mockHttpClient.target(any(URI.class))).thenReturn(mockWebTarget);
        when(mockWebTarget.request(MediaType.APPLICATION_JSON)).thenReturn(mockInvocationBuilder);
        when(mockInvocationBuilder.get()).thenReturn(mockResponse);
        when(mockResponse.getStatus()).thenReturn(500);

        assertThrows(
                DocumentStoreException.class, () -> documentStoreClient.getDocument(documentId));
    }

    @Test
    void Should_ThrowDocumentStoreException_When_URLConstructionFails() {
        String documentId = "672ca5d0-818a-46b6-946a-f9481023e803";
        when(mockConfigurationService.getDocumentEndpoint()).thenReturn(null);
        when(mockHttpClient.target(any(URI.class))).thenReturn(mockWebTarget);
        when(mockWebTarget.request(MediaType.APPLICATION_JSON)).thenReturn(mockInvocationBuilder);
        when(mockInvocationBuilder.get()).thenReturn(mockResponse);

        assertThrows(
                DocumentStoreException.class, () -> documentStoreClient.getDocument(documentId));
    }
}
