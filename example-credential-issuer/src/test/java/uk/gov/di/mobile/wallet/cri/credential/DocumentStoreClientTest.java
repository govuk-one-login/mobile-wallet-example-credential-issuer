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

    private DocumentStoreClient documentStoreClient;

    @Mock private Client mockHttpClient;
    @Mock private WebTarget mockWebTarget;
    @Mock private Invocation.Builder mockInvocationBuilder;
    @Mock private Response mockResponse;
    @Mock private DocumentStoreRecord mockDocument;
    @Mock private ConfigurationService mockConfigurationService;

    private static final String ITEM_ID = "672ca5d0-818a-46b6-946a-f9481023e803";
    private static final String DOCUMENT_PATH = "/documents/";

    private static URI documentBuilderUrl;

    @BeforeEach
    void setUp() throws URISyntaxException {
        documentBuilderUrl = new URI("https://test-example-cri.com");
        documentStoreClient = new DocumentStoreClient(mockConfigurationService, mockHttpClient);
    }

    @Test
    void Should_ReturnDocument_When_DocumentAPIReturns200()
            throws URISyntaxException, DocumentStoreException {
        URI expectedUri = new URI(documentBuilderUrl + DOCUMENT_PATH + ITEM_ID);
        when(mockConfigurationService.getCredentialStoreUrl()).thenReturn(documentBuilderUrl);
        when(mockConfigurationService.getDocumentEndpoint()).thenReturn(DOCUMENT_PATH);
        when(mockHttpClient.target(any(URI.class))).thenReturn(mockWebTarget);
        when(mockWebTarget.request(MediaType.APPLICATION_JSON)).thenReturn(mockInvocationBuilder);
        when(mockInvocationBuilder.get()).thenReturn(mockResponse);
        when(mockResponse.getStatus()).thenReturn(200);
        when(mockResponse.readEntity(DocumentStoreRecord.class)).thenReturn(mockDocument);

        DocumentStoreRecord document = documentStoreClient.getDocument(ITEM_ID);

        assertEquals(mockDocument, document);
        verify(mockHttpClient).target(expectedUri);
    }

    @Test
    void Should_ThrowDocumentStoreException_When_DocumentAPIReturns500() {
        when(mockConfigurationService.getCredentialStoreUrl()).thenReturn(documentBuilderUrl);
        when(mockConfigurationService.getDocumentEndpoint()).thenReturn(DOCUMENT_PATH);
        when(mockHttpClient.target(any(URI.class))).thenReturn(mockWebTarget);
        when(mockWebTarget.request(MediaType.APPLICATION_JSON)).thenReturn(mockInvocationBuilder);
        when(mockInvocationBuilder.get()).thenReturn(mockResponse);
        when(mockResponse.getStatus()).thenReturn(500);

        assertThrows(DocumentStoreException.class, () -> documentStoreClient.getDocument(ITEM_ID));
    }

    @Test
    void Should_ThrowDocumentStoreException_When_URLConstructionFails() {
        when(mockConfigurationService.getDocumentEndpoint()).thenReturn(null);
        when(mockHttpClient.target(any(URI.class))).thenReturn(mockWebTarget);
        when(mockWebTarget.request(MediaType.APPLICATION_JSON)).thenReturn(mockInvocationBuilder);
        when(mockInvocationBuilder.get()).thenReturn(mockResponse);

        assertThrows(DocumentStoreException.class, () -> documentStoreClient.getDocument(ITEM_ID));
    }
}
