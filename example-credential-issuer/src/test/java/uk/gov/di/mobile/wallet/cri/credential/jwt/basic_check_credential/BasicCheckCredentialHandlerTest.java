package uk.gov.di.mobile.wallet.cri.credential.jwt.basic_check_credential;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.MockedStatic;
import org.mockito.junit.jupiter.MockitoExtension;
import uk.gov.di.mobile.wallet.cri.credential.CredentialBuilder;
import uk.gov.di.mobile.wallet.cri.credential.CredentialSubjectMapper;
import uk.gov.di.mobile.wallet.cri.credential.Document;
import uk.gov.di.mobile.wallet.cri.credential.ProofJwtService;
import uk.gov.di.mobile.wallet.cri.credential.StatusListClient;
import uk.gov.di.mobile.wallet.cri.credential.jwt.basic_check_credential.BasicCheckCredentialHandler;
import uk.gov.di.mobile.wallet.cri.credential.jwt.basic_check_credential.BasicCheckCredentialSubject;
import uk.gov.di.mobile.wallet.cri.credential.jwt.basic_check_credential.BasicCheckDocument;
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
import static uk.gov.di.mobile.wallet.cri.credential.CredentialType.BASIC_DISCLOSURE_CREDENTIAL;

@ExtendWith(MockitoExtension.class)
class BasicCheckCredentialHandlerTest {

    @Mock private CredentialBuilder<BasicCheckCredentialSubject> mockCredentialBuilder;
    @Mock private Document mockDocument;
    @Mock private ProofJwtService.ProofJwtData mockProofData;
    @Mock private BasicCheckDocument mockBasicCheckDocument;
    @Mock private BasicCheckCredentialSubject mockCredentialSubject;
    private BasicCheckCredentialHandler handler;

    private static final String EXPECTED_CREDENTIAL = "signed-jwt-credential-string";
    private static final String DID_KEY = "did:key:test123";
    private static final long TTL_MINUTES = 1440L;
    private static final Optional<StatusListClient.StatusListInformation> STATUS_LIST_INFORMATION =
            Optional.empty();

    @BeforeEach
    void setUp() {
        handler = new BasicCheckCredentialHandler(mockCredentialBuilder);
    }

    @Test
    void Should_ReturnBasicCheckCredential() throws SigningException {
        Map<String, Object> documentData = new HashMap<>();
        when(mockDocument.getData()).thenReturn(documentData);
        when(mockProofData.didKey()).thenReturn(DID_KEY);
        when(mockBasicCheckDocument.getCredentialTtlMinutes()).thenReturn(TTL_MINUTES);
        when(mockCredentialBuilder.buildCredential(
                        any(BasicCheckCredentialSubject.class),
                        eq(BASIC_DISCLOSURE_CREDENTIAL),
                        eq(TTL_MINUTES)))
                .thenReturn(EXPECTED_CREDENTIAL);
        BasicCheckCredentialHandler spyHandler = spy(handler);
        ObjectMapper mockMapper = mock(ObjectMapper.class);
        when(mockMapper.convertValue((documentData), BasicCheckDocument.class))
                .thenReturn(mockBasicCheckDocument);
        setMapperField(spyHandler, mockMapper);
        try (MockedStatic<CredentialSubjectMapper> mockedMapper =
                mockStatic(CredentialSubjectMapper.class)) {
            mockedMapper
                    .when(
                            () ->
                                    CredentialSubjectMapper.buildBasicCheckCredentialSubject(
                                            mockBasicCheckDocument, DID_KEY))
                    .thenReturn(mockCredentialSubject);

            String credential =
                    spyHandler.buildCredential(
                            mockDocument, mockProofData, STATUS_LIST_INFORMATION);

            assertEquals(EXPECTED_CREDENTIAL, credential);
            verify(mockCredentialBuilder)
                    .buildCredential(
                            mockCredentialSubject, BASIC_DISCLOSURE_CREDENTIAL, TTL_MINUTES);
        }
    }

    @Test
    void Should_PropagateException_When_CredentialBuilderThrowsSigningException()
            throws SigningException {
        Map<String, Object> documentData = new HashMap<>();
        when(mockDocument.getData()).thenReturn(documentData);
        when(mockProofData.didKey()).thenReturn(DID_KEY);
        when(mockBasicCheckDocument.getCredentialTtlMinutes()).thenReturn(TTL_MINUTES);
        SigningException signingException =
                new SigningException("Some signing error", new RuntimeException());
        when(mockCredentialBuilder.buildCredential(
                        any(BasicCheckCredentialSubject.class),
                        eq(BASIC_DISCLOSURE_CREDENTIAL),
                        eq(TTL_MINUTES)))
                .thenThrow(signingException);
        BasicCheckCredentialHandler spyHandler = spy(handler);
        ObjectMapper mockMapper = mock(ObjectMapper.class);
        when(mockMapper.convertValue(documentData, BasicCheckDocument.class))
                .thenReturn(mockBasicCheckDocument);
        setMapperField(spyHandler, mockMapper);
        try (MockedStatic<CredentialSubjectMapper> mockedMapper =
                mockStatic(CredentialSubjectMapper.class)) {
            mockedMapper
                    .when(
                            () ->
                                    CredentialSubjectMapper.buildBasicCheckCredentialSubject(
                                            mockBasicCheckDocument, DID_KEY))
                    .thenReturn(mockCredentialSubject);

            SigningException thrown =
                    assertThrows(
                            SigningException.class,
                            () ->
                                    spyHandler.buildCredential(
                                            mockDocument, mockProofData, STATUS_LIST_INFORMATION));
            assertEquals("Some signing error", thrown.getMessage());
        }
    }

    private void setMapperField(BasicCheckCredentialHandler handler, ObjectMapper mapper) {
        try {
            var mapperField = BasicCheckCredentialHandler.class.getDeclaredField("mapper");
            mapperField.setAccessible(true);
            mapperField.set(handler, mapper);
        } catch (Exception exception) {
            throw new RuntimeException("Failed to inject mocked ObjectMapper", exception);
        }
    }
}
