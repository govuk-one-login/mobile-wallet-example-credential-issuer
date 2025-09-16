package uk.gov.di.mobile.wallet.cri.credential;

import com.nimbusds.jwt.SignedJWT;
import org.jetbrains.annotations.NotNull;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Mock;
import org.mockito.MockedStatic;
import org.mockito.junit.jupiter.MockitoExtension;
import org.slf4j.Logger;
import testUtils.MockAccessTokenBuilder;
import testUtils.MockProofBuilder;
import uk.gov.di.mobile.wallet.cri.credential.mobile_driving_licence.MDLException;
import uk.gov.di.mobile.wallet.cri.credential.mobile_driving_licence.MobileDrivingLicenceHandler;
import uk.gov.di.mobile.wallet.cri.models.CachedCredentialOffer;
import uk.gov.di.mobile.wallet.cri.models.StoredCredential;
import uk.gov.di.mobile.wallet.cri.services.authentication.AccessTokenService;
import uk.gov.di.mobile.wallet.cri.services.authentication.AccessTokenValidationException;
import uk.gov.di.mobile.wallet.cri.services.data_storage.DataStoreException;
import uk.gov.di.mobile.wallet.cri.services.data_storage.DynamoDbService;
import uk.gov.di.mobile.wallet.cri.services.object_storage.ObjectStoreException;
import uk.gov.di.mobile.wallet.cri.services.signing.SigningException;

import java.security.cert.CertificateException;
import java.security.interfaces.ECPublicKey;
import java.time.Instant;
import java.util.HashMap;
import java.util.UUID;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.containsString;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyLong;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.mockStatic;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class CredentialServiceTest {

    private CredentialService credentialService;

    @Mock private CredentialHandlerFactory mockCredentialHandlerFactory;
    @Mock private CredentialExpiryCalculator mockExpiryCalculator;
    @Mock private Logger mockLogger;
    @Mock private ECPublicKey mockEcPublicKey;
    @Mock private DynamoDbService mockDynamoDbService;
    @Mock private AccessTokenService mockAccessTokenService;
    @Mock private ProofJwtService mockProofJwtService;
    @Mock private DocumentStoreClient mockDocumentStoreClient;
    @Mock private StatusListClient mockStatusListClient;
    @Mock private MobileDrivingLicenceHandler mockMdlHandler;
    @Mock private StatusListClient.IssueResponse mockIssueResponse;

    private CachedCredentialOffer mockCachedCredentialOffer;
    private SignedJWT mockProofJwt;
    private SignedJWT mockAccessToken;
    private ProofJwtService.ProofJwtData mockAccessProofJwtData;
    private String mockCredentialJwt;
    private BuildCredentialResult mockBuilderResult;

    private static final String DOCUMENT_ID = "de9cbf02-2fbc-4d61-a627-f97851f6840b";
    private static final String CREDENTIAL_IDENTIFIER = "efb52887-48d6-43b7-b14c-da7896fbf54d";
    private static final String NONCE = "134e0c41-a8b4-46d4-aec8-cd547e125589";
    private static final String WALLET_SUBJECT_ID =
            "urn:fdc:wallet.account.gov.uk:2024:DtPT8x-dp_73tnlY3KNTiCitziN9GEherD16bqxNt9i";
    private static final String DID_KEY =
            "did:key:MFkwEwYHKoZIzj0CAQYIKoZIzj0DAQcDQgAEaUItVYrAvVK+1efrBvWDXtmapkl1PHqXUHytuK5/F7lfIXprXHD9zIdAinRrWSFeh28OJJzoSH1zqzOJ+ZhFOA==";
    public static final String DOCUMENT_NUMBER = "doc123";
    public static final UUID NOTIFICATION_ID =
            UUID.fromString("123e4567-e89b-12d3-a456-426655440000");
    private static final Integer STATUS_LIST_INDEX = 42;
    private static final String STATUS_LIST_URI = "https://example.com/status-list";
    private static final long EXPIRY_TIME = 1234567890L;
    private static final String MDL_VC_TYPE = "org.iso.18013.5.1.mDL";
    private static final String SOCIAL_SECURITY_VC_TYPE = "SocialSecurityCredential";

    @BeforeEach
    void setUp() throws AccessTokenValidationException, ProofJwtValidationException {
        mockCachedCredentialOffer = getMockCredentialOfferCacheItem(WALLET_SUBJECT_ID, "300");
        mockProofJwt = new MockProofBuilder("ES256").build();
        mockAccessToken = new MockAccessTokenBuilder("ES256").build();
        credentialService =
                new CredentialService(
                        mockDynamoDbService,
                        mockAccessTokenService,
                        mockProofJwtService,
                        mockDocumentStoreClient,
                        mockCredentialHandlerFactory,
                        mockExpiryCalculator,
                        mockStatusListClient) {
                    @Override
                    protected Logger getLogger() {
                        return mockLogger;
                    }
                };
        mockAccessProofJwtData = getMockProofJwtData(NONCE);
        when(mockProofJwtService.verifyProofJwt(any())).thenReturn(mockAccessProofJwtData);
        when(mockAccessTokenService.verifyAccessToken(any())).thenReturn(getMockAccessTokenData());
        mockCredentialJwt =
                "eyJraWQiOiJkaWQ6d2ViOmV4YW1wbGUtY3JlZGVudGlhbC1pc3N1ZXIubW9iaWxlLmJ1aWxkLmFjY291bnQuZ292LnVrIzVkY2JlZTg2M2I1ZDdjYzMwYzliYTFmNzM5M2RhY2M2YzE2NjEwNzgyZTRiNmExOTFmOTRhN2U4YjFlMTUxMGYiLCJjdHkiOiJ2YyIsInR5cCI6InZjK2p3dCIsImFsZyI6IkVTMjU2In0.eyJzdWIiOiJkaWQ6a2V5OnpEbmFlU0dmU1FNWXZuTGJMV0V1YmhoR0RQb3E3cEE5TU1OdnVtdmJzbU1DWm92VVIiLCJjcmVkZW50aWFsU3ViamVjdCI6eyJpZCI6ImRpZDprZXk6ekRuYWVTR2ZTUU1Zdm5MYkxXRXViaGhHRFBvcTdwQTlNTU52dW12YnNtTUNab3ZVUiIsIm5hbWUiOlt7Im5hbWVQYXJ0cyI6W3sidHlwZSI6IlRpdGxlIiwidmFsdWUiOiJNciJ9LHsidHlwZSI6IkdpdmVuTmFtZSIsInZhbHVlIjoiU2FyYWgifSx7InR5cGUiOiJHaXZlbk5hbWUiLCJ2YWx1ZSI6IkVsaXphYmV0aCJ9LHsidHlwZSI6IkZhbWlseU5hbWUiLCJ2YWx1ZSI6IkVkd2FyZHMifV19XSwic29jaWFsU2VjdXJpdHlSZWNvcmQiOlt7InBlcnNvbmFsTnVtYmVyIjoiUVExMjM0NTZDIn1dfSwiaXNzIjoiaHR0cHM6Ly9leGFtcGxlLWNyZWRlbnRpYWwtaXNzdWVyLm1vYmlsZS5idWlsZC5hY2NvdW50Lmdvdi51ayIsImRlc2NyaXB0aW9uIjoiTmF0aW9uYWwgSW5zdXJhbmNlIG51bWJlciIsInZhbGlkRnJvbSI6IjIwMjUtMDctMzFUMTU6MzM6MDBaIiwidHlwZSI6WyJWZXJpZmlhYmxlQ3JlZGVudGlhbCIsIlNvY2lhbFNlY3VyaXR5Q3JlZGVudGlhbCJdLCJAY29udGV4dCI6WyJodHRwczovL3d3dy53My5vcmcvbnMvY3JlZGVudGlhbHMvdjIiXSwiaXNzdWVyIjoiaHR0cHM6Ly9leGFtcGxlLWNyZWRlbnRpYWwtaXNzdWVyLm1vYmlsZS5idWlsZC5hY2NvdW50Lmdvdi51ayIsIm5iZiI6MTc1Mzk3NTk4MCwibmFtZSI6Ik5hdGlvbmFsIEluc3VyYW5jZSBudW1iZXIiLCJ2YWxpZFVudGlsIjoiMjAyNi0wNy0zMVQxNTozMzowMFoiLCJleHAiOjE3ODU1MTE5ODAsImlhdCI6MTc1Mzk3NTk4MH0.pxcRhjMZA6bCzHsyXVyygGpw0xk3VCVGS15LmTPM-TaUtBnSfG99rZylYcbDvojQJkzUqY66cr5mHx3lHpenkw";
        mockBuilderResult = new BuildCredentialResult(mockCredentialJwt, DOCUMENT_NUMBER);
    }

    @Test
    void Should_ThrowNonceValidationException_When_NonceValuesDontMatch()
            throws ProofJwtValidationException {
        mockAccessProofJwtData = getMockProofJwtData("not_the_same_nonce");
        when(mockProofJwtService.verifyProofJwt(any())).thenReturn(mockAccessProofJwtData);

        NonceValidationException exception =
                assertThrows(
                        NonceValidationException.class,
                        () -> credentialService.getCredential(mockAccessToken, mockProofJwt));
        assertEquals(
                "Access token c_nonce claim does not match Proof JWT nonce claim",
                exception.getMessage());
    }

    @Test
    void Should_ThrowCredentialOfferValidationException_When_CredentialOfferNotFound()
            throws DataStoreException {
        when(mockDynamoDbService.getCredentialOffer(anyString())).thenReturn(null);

        CredentialOfferException exception =
                assertThrows(
                        CredentialOfferException.class,
                        () -> credentialService.getCredential(mockAccessToken, mockProofJwt));
        assertEquals("Credential offer validation failed", exception.getMessage());
        verify(mockLogger)
                .error("Credential offer {} was not found", "efb52887-48d6-43b7-b14c-da7896fbf54d");
    }

    @Test
    void Should_ThrowAccessTokenValidationException_When_WalletSubjectIDsDontMatch()
            throws DataStoreException {
        mockCachedCredentialOffer =
                getMockCredentialOfferCacheItem("not_the_same_wallet_subject_id", "300");
        when(mockDynamoDbService.getCredentialOffer(anyString()))
                .thenReturn(mockCachedCredentialOffer);

        AccessTokenValidationException exception =
                assertThrows(
                        AccessTokenValidationException.class,
                        () -> credentialService.getCredential(mockAccessToken, mockProofJwt));
        assertThat(
                exception.getMessage(),
                containsString("Access token sub claim does not match cached walletSubjectId"));
    }

    @Test
    void Should_ThrowCredentialOfferException_When_CredentialOfferIsExpired()
            throws DataStoreException {
        mockCachedCredentialOffer = getMockCredentialOfferCacheItem(WALLET_SUBJECT_ID, "-1");
        when(mockDynamoDbService.getCredentialOffer(anyString()))
                .thenReturn(mockCachedCredentialOffer);

        CredentialOfferException exception =
                assertThrows(
                        CredentialOfferException.class,
                        () -> credentialService.getCredential(mockAccessToken, mockProofJwt));
        assertEquals("Credential offer validation failed", exception.getMessage());
        verify(mockLogger)
                .error("Credential offer {} is expired", "efb52887-48d6-43b7-b14c-da7896fbf54d");
    }

    @Test
    void Should_ThrowCredentialServiceException_When_DataStoreExceptionIsThrown()
            throws DataStoreException {
        when(mockDynamoDbService.getCredentialOffer(anyString()))
                .thenThrow(new DataStoreException("Some database error"));

        CredentialServiceException exception =
                assertThrows(
                        CredentialServiceException.class,
                        () -> credentialService.getCredential(mockAccessToken, mockProofJwt));
        assertEquals("Failed to issue credential due to an internal error", exception.getMessage());
        assertEquals(DataStoreException.class, exception.getCause().getClass());
        assertEquals("Some database error", exception.getCause().getMessage());
    }

    @Test
    void Should_ThrowCredentialServiceException_When_SigningExceptionIsThrown()
            throws DataStoreException,
                    SigningException,
                    DocumentStoreException,
                    ObjectStoreException,
                    CertificateException {
        when(mockDynamoDbService.getCredentialOffer(anyString()))
                .thenReturn(mockCachedCredentialOffer);
        when(mockDocumentStoreClient.getDocument(anyString()))
                .thenReturn(getMockSocialSecurityDocument());
        CredentialHandler mockHandler = mock(CredentialHandler.class);
        when(mockCredentialHandlerFactory.createHandler(SOCIAL_SECURITY_VC_TYPE))
                .thenReturn(mockHandler);
        when(mockHandler.buildCredential(any(), any()))
                .thenThrow(new SigningException("Some signing error", new RuntimeException()));

        CredentialServiceException exception =
                assertThrows(
                        CredentialServiceException.class,
                        () -> credentialService.getCredential(mockAccessToken, mockProofJwt));
        assertEquals("Failed to issue credential due to an internal error", exception.getMessage());
        assertEquals(SigningException.class, exception.getCause().getClass());
        assertEquals("Some signing error", exception.getCause().getMessage());
    }

    @Test
    void Should_ThrowCredentialServiceException_When_StatusListExceptionIsThrown()
            throws DataStoreException,
                    DocumentStoreException,
                    StatusListException,
                    SigningException {

        Document mockMdlDocument = getMockMobileDrivingLicenceDocument();
        when(mockDynamoDbService.getCredentialOffer(anyString()))
                .thenReturn(mockCachedCredentialOffer);
        when(mockDocumentStoreClient.getDocument(anyString())).thenReturn(mockMdlDocument);
        when(mockCredentialHandlerFactory.createHandler(MDL_VC_TYPE)).thenReturn(mockMdlHandler);
        when(mockExpiryCalculator.calculateExpiry(mockMdlDocument)).thenReturn(EXPIRY_TIME);
        when(mockStatusListClient.getIndex(EXPIRY_TIME))
                .thenThrow(new StatusListException("Some status list error"));

        CredentialServiceException exception =
                assertThrows(
                        CredentialServiceException.class,
                        () -> credentialService.getCredential(mockAccessToken, mockProofJwt));
        assertEquals("Failed to issue credential due to an internal error", exception.getMessage());
        assertEquals(StatusListException.class, exception.getCause().getClass());
        assertEquals("Some status list error", exception.getCause().getMessage());
    }

    @Test
    void Should_ThrowCredentialServiceException_When_MDLExceptionIsThrown()
            throws DataStoreException,
                    DocumentStoreException,
                    StatusListException,
                    SigningException,
                    ObjectStoreException,
                    CertificateException {

        Document mockMdlDocument = getMockMobileDrivingLicenceDocument();
        when(mockDynamoDbService.getCredentialOffer(anyString()))
                .thenReturn(mockCachedCredentialOffer);
        when(mockDocumentStoreClient.getDocument(anyString())).thenReturn(mockMdlDocument);
        when(mockCredentialHandlerFactory.createHandler(MDL_VC_TYPE)).thenReturn(mockMdlHandler);
        when(mockExpiryCalculator.calculateExpiry(mockMdlDocument)).thenReturn(EXPIRY_TIME);
        when(mockIssueResponse.idx()).thenReturn(STATUS_LIST_INDEX);
        when(mockIssueResponse.uri()).thenReturn(STATUS_LIST_URI);
        when(mockStatusListClient.getIndex(EXPIRY_TIME)).thenReturn(mockIssueResponse);
        when(mockMdlHandler.buildCredential(
                        mockMdlDocument,
                        mockAccessProofJwtData,
                        STATUS_LIST_INDEX,
                        STATUS_LIST_URI))
                .thenThrow(new MDLException("Some mDL error", new RuntimeException()));

        CredentialServiceException exception =
                assertThrows(
                        CredentialServiceException.class,
                        () -> credentialService.getCredential(mockAccessToken, mockProofJwt));
        assertEquals("Failed to issue credential due to an internal error", exception.getMessage());
        assertEquals(MDLException.class, exception.getCause().getClass());
        assertEquals("Some mDL error", exception.getCause().getMessage());
    }

    @Test
    void Should_SaveStoredCredentialWithNullStatusListData_When_ProcessingNonMDLCredential()
            throws DataStoreException,
                    ObjectStoreException,
                    SigningException,
                    CertificateException,
                    NonceValidationException,
                    CredentialOfferException,
                    AccessTokenValidationException,
                    CredentialServiceException,
                    ProofJwtValidationException,
                    DocumentStoreException,
                    StatusListException {

        Document mockDocument = getMockSocialSecurityDocument();
        when(mockDynamoDbService.getCredentialOffer(anyString()))
                .thenReturn(mockCachedCredentialOffer);
        when(mockDocumentStoreClient.getDocument(anyString())).thenReturn(mockDocument);
        CredentialHandler mockHandler = mock(CredentialHandler.class);
        when(mockCredentialHandlerFactory.createHandler(SOCIAL_SECURITY_VC_TYPE))
                .thenReturn(mockHandler);
        when(mockHandler.buildCredential(any(), any())).thenReturn(mockBuilderResult);
        when(mockExpiryCalculator.calculateExpiry(mockDocument)).thenReturn(EXPIRY_TIME);
        try (MockedStatic<UUID> mockedUUID = mockStatic(UUID.class)) {
            mockedUUID.when(UUID::randomUUID).thenReturn(NOTIFICATION_ID);

            credentialService.getCredential(mockAccessToken, mockProofJwt);

            ArgumentCaptor<StoredCredential> storedCredentialCaptor =
                    ArgumentCaptor.forClass(StoredCredential.class);
            verify(mockDynamoDbService).saveStoredCredential(storedCredentialCaptor.capture());
            StoredCredential savedCredential = storedCredentialCaptor.getValue();
            assertEquals(CREDENTIAL_IDENTIFIER, savedCredential.getCredentialIdentifier());
            assertEquals(NOTIFICATION_ID.toString(), savedCredential.getNotificationId());
            assertEquals(WALLET_SUBJECT_ID, savedCredential.getWalletSubjectId());
            assertEquals(DOCUMENT_NUMBER, savedCredential.getDocumentNumber());
            assertNull(
                    savedCredential.getStatusListIndex(), "Should be null for non-MDL credentials");
            assertNull(
                    savedCredential.getStatusListUri(), "Should be null for non-MDL credentials");
            assertEquals(EXPIRY_TIME, savedCredential.getTimeToLive());
            verify(mockStatusListClient, never()).getIndex(anyLong());
        }
    }

    @Test
    void Should_CallExpiryCalculator_When_ProcessingCredential()
            throws DataStoreException,
                    ObjectStoreException,
                    SigningException,
                    CertificateException,
                    NonceValidationException,
                    CredentialOfferException,
                    AccessTokenValidationException,
                    CredentialServiceException,
                    ProofJwtValidationException,
                    DocumentStoreException {

        Document mockDocument = getMockSocialSecurityDocument();
        when(mockDynamoDbService.getCredentialOffer(anyString()))
                .thenReturn(mockCachedCredentialOffer);
        when(mockDocumentStoreClient.getDocument(anyString())).thenReturn(mockDocument);
        CredentialHandler mockHandler = mock(CredentialHandler.class);
        when(mockCredentialHandlerFactory.createHandler(SOCIAL_SECURITY_VC_TYPE))
                .thenReturn(mockHandler);
        when(mockHandler.buildCredential(any(), any())).thenReturn(mockBuilderResult);
        when(mockExpiryCalculator.calculateExpiry(mockDocument)).thenReturn(EXPIRY_TIME);
        try (MockedStatic<UUID> mockedUUID = mockStatic(UUID.class)) {
            mockedUUID.when(UUID::randomUUID).thenReturn(NOTIFICATION_ID);

            credentialService.getCredential(mockAccessToken, mockProofJwt);

            verify(mockExpiryCalculator).calculateExpiry(mockDocument);
        }
    }

    @Test
    void Should_ReturnCredentialResponse_When_ProcessingSocialSecurityCredential()
            throws DataStoreException,
                    ObjectStoreException,
                    SigningException,
                    CertificateException,
                    NonceValidationException,
                    CredentialOfferException,
                    AccessTokenValidationException,
                    CredentialServiceException,
                    ProofJwtValidationException,
                    DocumentStoreException {
        Document mockDocument = getMockSocialSecurityDocument();
        when(mockDynamoDbService.getCredentialOffer(anyString()))
                .thenReturn(mockCachedCredentialOffer);
        when(mockDocumentStoreClient.getDocument(anyString())).thenReturn(mockDocument);
        CredentialHandler mockHandler = mock(CredentialHandler.class);
        when(mockCredentialHandlerFactory.createHandler(SOCIAL_SECURITY_VC_TYPE))
                .thenReturn(mockHandler);
        when(mockHandler.buildCredential(any(), any())).thenReturn(mockBuilderResult);
        try (MockedStatic<UUID> mockedUUID = mockStatic(UUID.class)) {
            mockedUUID.when(UUID::randomUUID).thenReturn(NOTIFICATION_ID);

            CredentialResponse credentialServiceReturnValue =
                    credentialService.getCredential(mockAccessToken, mockProofJwt);

            assertEquals(mockCredentialJwt, credentialServiceReturnValue.getCredential());
            assertEquals(
                    NOTIFICATION_ID.toString(), credentialServiceReturnValue.getNotificationId());
            verify(mockAccessTokenService).verifyAccessToken(mockAccessToken);
            verify(mockProofJwtService).verifyProofJwt(mockProofJwt);
            verify(mockDynamoDbService, times(1)).getCredentialOffer(CREDENTIAL_IDENTIFIER);
            verify(mockDynamoDbService, times(1)).deleteCredentialOffer(CREDENTIAL_IDENTIFIER);
            verify(mockHandler, times(1)).buildCredential(mockDocument, mockAccessProofJwtData);
        }
    }

    @Test
    void Should_ReturnCredentialResponse_When_ProcessingMobileDrivingLicence()
            throws DataStoreException,
                    ObjectStoreException,
                    SigningException,
                    CertificateException,
                    NonceValidationException,
                    CredentialOfferException,
                    AccessTokenValidationException,
                    CredentialServiceException,
                    ProofJwtValidationException,
                    DocumentStoreException,
                    StatusListException {

        Document mockMdlDocument = getMockMobileDrivingLicenceDocument();
        when(mockDynamoDbService.getCredentialOffer(anyString()))
                .thenReturn(mockCachedCredentialOffer);
        when(mockDocumentStoreClient.getDocument(anyString())).thenReturn(mockMdlDocument);
        when(mockCredentialHandlerFactory.createHandler(MDL_VC_TYPE)).thenReturn(mockMdlHandler);
        when(mockExpiryCalculator.calculateExpiry(mockMdlDocument)).thenReturn(EXPIRY_TIME);
        when(mockIssueResponse.idx()).thenReturn(STATUS_LIST_INDEX);
        when(mockIssueResponse.uri()).thenReturn(STATUS_LIST_URI);
        when(mockStatusListClient.getIndex(EXPIRY_TIME)).thenReturn(mockIssueResponse);
        when(mockMdlHandler.buildCredential(
                        mockMdlDocument,
                        mockAccessProofJwtData,
                        STATUS_LIST_INDEX,
                        STATUS_LIST_URI))
                .thenReturn(mockBuilderResult);
        try (MockedStatic<UUID> mockedUUID = mockStatic(UUID.class)) {
            mockedUUID.when(UUID::randomUUID).thenReturn(NOTIFICATION_ID);

            CredentialResponse result =
                    credentialService.getCredential(mockAccessToken, mockProofJwt);

            assertEquals(mockCredentialJwt, result.getCredential());
            assertEquals(NOTIFICATION_ID.toString(), result.getNotificationId());
            verify(mockStatusListClient).getIndex(EXPIRY_TIME);
            verify(mockMdlHandler)
                    .buildCredential(
                            mockMdlDocument,
                            mockAccessProofJwtData,
                            STATUS_LIST_INDEX,
                            STATUS_LIST_URI);
            ArgumentCaptor<StoredCredential> storedCredentialCaptor =
                    ArgumentCaptor.forClass(StoredCredential.class);
            verify(mockDynamoDbService).saveStoredCredential(storedCredentialCaptor.capture());
            StoredCredential savedCredential = storedCredentialCaptor.getValue();
            assertEquals(CREDENTIAL_IDENTIFIER, savedCredential.getCredentialIdentifier());
            assertEquals(NOTIFICATION_ID.toString(), savedCredential.getNotificationId());
            assertEquals(WALLET_SUBJECT_ID, savedCredential.getWalletSubjectId());
            assertEquals(DOCUMENT_NUMBER, savedCredential.getDocumentNumber());
            assertEquals(STATUS_LIST_INDEX, savedCredential.getStatusListIndex());
            assertEquals(STATUS_LIST_URI, savedCredential.getStatusListUri());
            assertEquals(EXPIRY_TIME, savedCredential.getTimeToLive());
        }
    }

    private CachedCredentialOffer getMockCredentialOfferCacheItem(
            String walletSubjectId, String expiresInSeconds) {
        Long ttl = Instant.now().plusSeconds(Long.parseLong(expiresInSeconds)).getEpochSecond();
        return new CachedCredentialOffer(CREDENTIAL_IDENTIFIER, DOCUMENT_ID, walletSubjectId, ttl);
    }

    private AccessTokenService.AccessTokenData getMockAccessTokenData() {
        return new AccessTokenService.AccessTokenData(
                CredentialServiceTest.WALLET_SUBJECT_ID,
                CredentialServiceTest.NONCE,
                CredentialServiceTest.CREDENTIAL_IDENTIFIER);
    }

    private ProofJwtService.ProofJwtData getMockProofJwtData(String nonce) {
        return new ProofJwtService.ProofJwtData(
                CredentialServiceTest.DID_KEY, nonce, mockEcPublicKey);
    }

    private static @NotNull Document getMockSocialSecurityDocument() {
        HashMap<String, Object> data = new HashMap<>();
        data.put("familyName", "Edwards Green");
        data.put("givenName", "Sarah Elizabeth");
        data.put("nino", "QQ123456C");
        data.put("title", "Miss");
        data.put("credentialTtlMinutes", "525600");
        return new Document(DOCUMENT_ID, data, SOCIAL_SECURITY_VC_TYPE);
    }

    private static Document getMockMobileDrivingLicenceDocument() {
        HashMap<String, Object> data = new HashMap<>();
        data.put("familyName", "Smith");
        data.put("givenName", "John");
        data.put("licenceNumber", "ABC123456");
        data.put("dateOfBirth", "1990-01-01");
        data.put("credentialTtlMinutes", "525600");
        return new Document(DOCUMENT_ID, data, MDL_VC_TYPE);
    }
}
