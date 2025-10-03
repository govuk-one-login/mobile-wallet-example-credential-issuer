package uk.gov.di.mobile.wallet.cri.credential.digital_veteran_card;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.MockedStatic;
import org.mockito.junit.jupiter.MockitoExtension;
import uk.gov.di.mobile.wallet.cri.credential.BuildCredentialResult;
import uk.gov.di.mobile.wallet.cri.credential.CredentialBuilder;
import uk.gov.di.mobile.wallet.cri.credential.CredentialSubjectMapper;
import uk.gov.di.mobile.wallet.cri.credential.Document;
import uk.gov.di.mobile.wallet.cri.credential.ProofJwtService;
import uk.gov.di.mobile.wallet.cri.credential.StatusListClient;
import uk.gov.di.mobile.wallet.cri.services.signing.SigningException;

import java.util.HashMap;
import java.util.Map;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.mockStatic;
import static org.mockito.Mockito.spy;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static uk.gov.di.mobile.wallet.cri.credential.CredentialType.DIGITAL_VETERAN_CARD;

@ExtendWith(MockitoExtension.class)
class DigitalVeteranCardHandlerTest {

    @Mock private CredentialBuilder<VeteranCardCredentialSubject> mockCredentialBuilder;
    @Mock private Document mockDocument;
    @Mock private ProofJwtService.ProofJwtData mockProofData;
    @Mock private VeteranCardDocument mockVeteranCardDocument;
    @Mock private VeteranCardCredentialSubject mockCredentialSubject;
    private DigitalVeteranCardHandler handler;

    private static final String EXPECTED_CREDENTIAL = "signed-jwt-credential-string";
    private static final String EXPECTED_DOCUMENT_NUMBER = "1234567890";
    private static final String DID_KEY = "did:key:test123";
    private static final long TTL_MINUTES = 1440L;
    private static final Optional<StatusListClient.IssueResponse> STATUS_LIST_ISSUE_RESPONSE =
            Optional.empty();

    @BeforeEach
    void setUp() {
        handler = new DigitalVeteranCardHandler(mockCredentialBuilder);
    }

    @Test
    void Should_ReturnDigitalVeteranCard() throws SigningException {
        Map<String, Object> documentData = new HashMap<>();
        when(mockDocument.getData()).thenReturn(documentData);
        when(mockProofData.didKey()).thenReturn(DID_KEY);
        when(mockVeteranCardDocument.getCredentialTtlMinutes()).thenReturn(TTL_MINUTES);
        when(mockVeteranCardDocument.getServiceNumber()).thenReturn(EXPECTED_DOCUMENT_NUMBER);
        when(mockCredentialBuilder.buildCredential(
                        any(VeteranCardCredentialSubject.class),
                        eq(DIGITAL_VETERAN_CARD),
                        eq(TTL_MINUTES)))
                .thenReturn(EXPECTED_CREDENTIAL);
        DigitalVeteranCardHandler spyHandler = spy(handler);
        ObjectMapper mockMapper = mock(ObjectMapper.class);
        when(mockMapper.convertValue(documentData, VeteranCardDocument.class))
                .thenReturn(mockVeteranCardDocument);
        setMapperField(spyHandler, mockMapper);
        try (MockedStatic<CredentialSubjectMapper> mockedMapper =
                mockStatic(CredentialSubjectMapper.class)) {
            mockedMapper
                    .when(
                            () ->
                                    CredentialSubjectMapper.buildVeteranCardCredentialSubject(
                                            mockVeteranCardDocument, DID_KEY))
                    .thenReturn(mockCredentialSubject);

            BuildCredentialResult result =
                    spyHandler.buildCredential(
                            mockDocument, mockProofData, STATUS_LIST_ISSUE_RESPONSE);

            assertEquals(EXPECTED_CREDENTIAL, result.credential());
            assertEquals(EXPECTED_DOCUMENT_NUMBER, result.documentPrimaryIdentifier());
            verify(mockCredentialBuilder)
                    .buildCredential(mockCredentialSubject, DIGITAL_VETERAN_CARD, TTL_MINUTES);
        }
    }

    @Test
    void Should_PropagateException_When_CredentialBuilderThrowsSigningException()
            throws SigningException {
        Map<String, Object> documentData = new HashMap<>();
        when(mockDocument.getData()).thenReturn(documentData);
        when(mockProofData.didKey()).thenReturn(DID_KEY);
        when(mockVeteranCardDocument.getCredentialTtlMinutes()).thenReturn(TTL_MINUTES);
        SigningException signingException =
                new SigningException("Some signing error", new RuntimeException());
        when(mockCredentialBuilder.buildCredential(
                        any(VeteranCardCredentialSubject.class),
                        eq(DIGITAL_VETERAN_CARD),
                        eq(TTL_MINUTES)))
                .thenThrow(signingException);
        DigitalVeteranCardHandler spyHandler = spy(handler);
        ObjectMapper mockMapper = mock(ObjectMapper.class);
        when(mockMapper.convertValue(documentData, VeteranCardDocument.class))
                .thenReturn(mockVeteranCardDocument);
        setMapperField(spyHandler, mockMapper);
        try (MockedStatic<CredentialSubjectMapper> mockedMapper =
                mockStatic(CredentialSubjectMapper.class)) {
            mockedMapper
                    .when(
                            () ->
                                    CredentialSubjectMapper.buildVeteranCardCredentialSubject(
                                            mockVeteranCardDocument, DID_KEY))
                    .thenReturn(mockCredentialSubject);

            SigningException thrown =
                    assertThrows(
                            SigningException.class,
                            () ->
                                    spyHandler.buildCredential(
                                            mockDocument,
                                            mockProofData,
                                            STATUS_LIST_ISSUE_RESPONSE));
            assertEquals("Some signing error", thrown.getMessage());
        }
    }

    private void setMapperField(DigitalVeteranCardHandler handler, ObjectMapper mapper) {
        try {
            var mapperField = DigitalVeteranCardHandler.class.getDeclaredField("mapper");
            mapperField.setAccessible(true);
            mapperField.set(handler, mapper);
        } catch (Exception exception) {
            throw new RuntimeException("Failed to inject mocked ObjectMapper", exception);
        }
    }
}
