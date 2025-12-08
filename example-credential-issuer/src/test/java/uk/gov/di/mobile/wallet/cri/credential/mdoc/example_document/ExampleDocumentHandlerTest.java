package uk.gov.di.mobile.wallet.cri.credential.mdoc.example_document;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import uk.gov.di.mobile.wallet.cri.credential.DocumentStoreRecord;
import uk.gov.di.mobile.wallet.cri.credential.StatusListClient;
import uk.gov.di.mobile.wallet.cri.credential.mdoc.MdocCredentialBuilder;
import uk.gov.di.mobile.wallet.cri.credential.proof.ProofJwtService;
import uk.gov.di.mobile.wallet.cri.services.object_storage.ObjectStoreException;
import uk.gov.di.mobile.wallet.cri.services.signing.SigningException;

import java.security.cert.CertificateException;
import java.security.interfaces.ECPublicKey;
import java.util.HashMap;
import java.util.Map;
import java.util.NoSuchElementException;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyLong;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.spy;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoInteractions;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class ExampleDocumentHandlerTest {

    @Mock private MdocCredentialBuilder<ExampleDocument> mockMdocCredentialBuilder;
    @Mock private DocumentStoreRecord mockDocument;
    @Mock private ECPublicKey ecPublicKey;
    @Mock private ProofJwtService.ProofJwtData mockProofData;
    @Mock private ExampleDocument mockExampleDocument;
    private ExampleDocumentHandler handler;

    private static final StatusListClient.StatusListInformation STATUS_LIST_INFORMATION =
            new StatusListClient.StatusListInformation(
                    0, "https://test-status-list.gov.uk/t/3B0F3BD087A7");
    private static final String EXPECTED_CREDENTIAL = "signed-mdoc-credential-string";
    private static final long CREDENTIAL_TTL_MINUTES = 43200L;

    @BeforeEach
    void setUp() {
        handler = new ExampleDocumentHandler(mockMdocCredentialBuilder);
    }

    @Test
    void Should_ReturnExampleDocument()
            throws SigningException, ObjectStoreException, CertificateException {
        Map<String, Object> documentData = new HashMap<>();
        when(mockDocument.getData()).thenReturn(documentData);
        when(mockDocument.getCredentialTtlMinutes()).thenReturn(CREDENTIAL_TTL_MINUTES);
        when(mockProofData.publicKey()).thenReturn(ecPublicKey);
        when(mockMdocCredentialBuilder.buildCredential(
                        any(ExampleDocument.class),
                        any(ECPublicKey.class),
                        any(StatusListClient.StatusListInformation.class),
                        anyLong()))
                .thenReturn(EXPECTED_CREDENTIAL);
        ExampleDocumentHandler spyHandler = spy(handler);
        ObjectMapper mockMapper = mock(ObjectMapper.class);
        when(mockMapper.convertValue(documentData, ExampleDocument.class))
                .thenReturn(mockExampleDocument);

        setMapperField(spyHandler, mockMapper);

        String credential =
                spyHandler.buildCredential(
                        mockDocument, mockProofData, Optional.of(STATUS_LIST_INFORMATION));

        assertEquals(EXPECTED_CREDENTIAL, credential);
        verify(mockMapper).convertValue(documentData, ExampleDocument.class);
        verify(mockMdocCredentialBuilder)
                .buildCredential(
                        mockExampleDocument,
                        ecPublicKey,
                        STATUS_LIST_INFORMATION,
                        CREDENTIAL_TTL_MINUTES);
    }

    @Test
    void Should_ThrowNoSuchElementException_When_StatusListInformationIsEmpty() {
        Map<String, Object> documentData = new HashMap<>();
        when(mockDocument.getData()).thenReturn(documentData);
        when(mockProofData.publicKey()).thenReturn(ecPublicKey);
        ExampleDocumentHandler spyHandler = spy(handler);
        ObjectMapper mockMapper = mock(ObjectMapper.class);
        when(mockMapper.convertValue(documentData, ExampleDocument.class))
                .thenReturn(mockExampleDocument);
        setMapperField(spyHandler, mockMapper);
        Optional<StatusListClient.StatusListInformation> emptyStatusListInformation =
                Optional.empty();

        NoSuchElementException thrown =
                assertThrows(
                        NoSuchElementException.class,
                        () ->
                                spyHandler.buildCredential(
                                        mockDocument, mockProofData, emptyStatusListInformation));
        assertEquals("No value present", thrown.getMessage());
        verifyNoInteractions(mockMdocCredentialBuilder);
    }

    @Test
    void Should_PropagateException_When_MdocCredentialBuilderThrowsSigningException()
            throws SigningException, ObjectStoreException, CertificateException {
        Map<String, Object> documentData = new HashMap<>();
        when(mockDocument.getData()).thenReturn(documentData);
        when(mockDocument.getCredentialTtlMinutes()).thenReturn(CREDENTIAL_TTL_MINUTES);
        when(mockProofData.publicKey()).thenReturn(ecPublicKey);
        SigningException signingException =
                new SigningException("Some signing error", new RuntimeException());
        when(mockMdocCredentialBuilder.buildCredential(
                        any(ExampleDocument.class),
                        any(ECPublicKey.class),
                        any(StatusListClient.StatusListInformation.class),
                        anyLong()))
                .thenThrow(signingException);
        ExampleDocumentHandler spyHandler = spy(handler);
        ObjectMapper mockMapper = mock(ObjectMapper.class);
        when(mockMapper.convertValue(documentData, ExampleDocument.class))
                .thenReturn(mockExampleDocument);
        setMapperField(spyHandler, mockMapper);

        SigningException thrown =
                assertThrows(
                        SigningException.class,
                        () ->
                                spyHandler.buildCredential(
                                        mockDocument,
                                        mockProofData,
                                        Optional.of(STATUS_LIST_INFORMATION)));
        assertEquals("Some signing error", thrown.getMessage());
    }

    private void setMapperField(ExampleDocumentHandler handler, ObjectMapper mapper) {
        try {
            var mapperField = ExampleDocumentHandler.class.getDeclaredField("mapper");
            mapperField.setAccessible(true);
            mapperField.set(handler, mapper);
        } catch (Exception exception) {
            throw new RuntimeException("Failed to inject mocked ObjectMapper", exception);
        }
    }
}
