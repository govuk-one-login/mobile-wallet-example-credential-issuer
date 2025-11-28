package uk.gov.di.mobile.wallet.cri.credential.jwt.social_security_credential;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.MockedStatic;
import org.mockito.junit.jupiter.MockitoExtension;
import uk.gov.di.mobile.wallet.cri.credential.jwt.CredentialSubjectMapper;
import uk.gov.di.mobile.wallet.cri.credential.Document;
import uk.gov.di.mobile.wallet.cri.credential.StatusListClient;
import uk.gov.di.mobile.wallet.cri.credential.jwt.CredentialBuilder;
import uk.gov.di.mobile.wallet.cri.credential.proof.ProofJwtService;
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
import static uk.gov.di.mobile.wallet.cri.credential.CredentialType.SOCIAL_SECURITY_CREDENTIAL;

@ExtendWith(MockitoExtension.class)
class SocialSecurityCredentialHandlerTest {

    @Mock private CredentialBuilder<SocialSecurityCredentialSubject> mockCredentialBuilder;
    @Mock private Document mockDocument;
    @Mock private ProofJwtService.ProofJwtData mockProofData;
    @Mock private SocialSecurityDocument mockSocialSecurityDocument;
    @Mock private SocialSecurityCredentialSubject mockCredentialSubject;
    private SocialSecurityCredentialHandler handler;

    private static final String EXPECTED_CREDENTIAL = "signed-jwt-credential-string";
    private static final String DID_KEY = "did:key:test123";
    private static final long TTL_MINUTES = 1440L;
    private static final Optional<StatusListClient.StatusListInformation> STATUS_LIST_INFORMATION =
            Optional.empty();

    @BeforeEach
    void setUp() {
        handler = new SocialSecurityCredentialHandler(mockCredentialBuilder);
    }

    @Test
    void Should_ReturnSocialSecurityCredential() throws SigningException {
        Map<String, Object> documentData = new HashMap<>();
        when(mockDocument.getData()).thenReturn(documentData);
        when(mockProofData.didKey()).thenReturn(DID_KEY);
        when(mockSocialSecurityDocument.getCredentialTtlMinutes()).thenReturn(TTL_MINUTES);
        when(mockCredentialBuilder.buildCredential(
                        any(SocialSecurityCredentialSubject.class),
                        eq(SOCIAL_SECURITY_CREDENTIAL),
                        eq(TTL_MINUTES)))
                .thenReturn(EXPECTED_CREDENTIAL);
        SocialSecurityCredentialHandler spyHandler = spy(handler);
        ObjectMapper mockMapper = mock(ObjectMapper.class);
        when(mockMapper.convertValue(documentData, SocialSecurityDocument.class))
                .thenReturn(mockSocialSecurityDocument);
        setMapperField(spyHandler, mockMapper);
        try (MockedStatic<CredentialSubjectMapper> mockedMapper =
                mockStatic(CredentialSubjectMapper.class)) {
            mockedMapper
                    .when(
                            () ->
                                    CredentialSubjectMapper.buildSocialSecurityCredentialSubject(
                                            mockSocialSecurityDocument, DID_KEY))
                    .thenReturn(mockCredentialSubject);

            String credential =
                    spyHandler.buildCredential(
                            mockDocument, mockProofData, STATUS_LIST_INFORMATION);

            assertEquals(EXPECTED_CREDENTIAL, credential);
            verify(mockCredentialBuilder)
                    .buildCredential(
                            mockCredentialSubject, SOCIAL_SECURITY_CREDENTIAL, TTL_MINUTES);
        }
    }

    @Test
    void Should_PropagateException_When_CredentialBuilderThrowsSigningException()
            throws SigningException {
        Map<String, Object> documentData = new HashMap<>();
        when(mockDocument.getData()).thenReturn(documentData);
        when(mockProofData.didKey()).thenReturn(DID_KEY);
        when(mockSocialSecurityDocument.getCredentialTtlMinutes()).thenReturn(TTL_MINUTES);
        SigningException signingException =
                new SigningException("Some signing error", new RuntimeException());
        when(mockCredentialBuilder.buildCredential(
                        any(SocialSecurityCredentialSubject.class),
                        eq(SOCIAL_SECURITY_CREDENTIAL),
                        eq(TTL_MINUTES)))
                .thenThrow(signingException);
        SocialSecurityCredentialHandler spyHandler = spy(handler);
        ObjectMapper mockMapper = mock(ObjectMapper.class);
        when(mockMapper.convertValue(documentData, SocialSecurityDocument.class))
                .thenReturn(mockSocialSecurityDocument);
        setMapperField(spyHandler, mockMapper);
        try (MockedStatic<CredentialSubjectMapper> mockedMapper =
                mockStatic(CredentialSubjectMapper.class)) {
            mockedMapper
                    .when(
                            () ->
                                    CredentialSubjectMapper.buildSocialSecurityCredentialSubject(
                                            mockSocialSecurityDocument, DID_KEY))
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

    private void setMapperField(SocialSecurityCredentialHandler handler, ObjectMapper mapper) {
        try {
            var mapperField = SocialSecurityCredentialHandler.class.getDeclaredField("mapper");
            mapperField.setAccessible(true);
            mapperField.set(handler, mapper);
        } catch (Exception exception) {
            throw new RuntimeException("Failed to inject mocked ObjectMapper", exception);
        }
    }
}
