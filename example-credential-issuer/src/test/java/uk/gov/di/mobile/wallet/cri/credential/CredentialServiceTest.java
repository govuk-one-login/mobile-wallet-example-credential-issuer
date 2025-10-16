package uk.gov.di.mobile.wallet.cri.credential;

import com.nimbusds.jwt.SignedJWT;
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
import uk.gov.di.mobile.wallet.cri.credential.util.CredentialExpiryCalculator;
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
import java.util.Optional;
import java.util.UUID;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.containsString;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyLong;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.mockStatic;
import static org.mockito.Mockito.never;
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

    private CachedCredentialOffer mockCachedCredentialOffer;
    private SignedJWT mockProofJwt;
    private SignedJWT mockAccessToken;
    private ProofJwtService.ProofJwtData mockProofJwtData;

    private static final String MDL_VC_TYPE = "org.iso.18013.5.1.mDL";
    private static final String SOCIAL_SECURITY_VC_TYPE = "SocialSecurityCredential";
    private static final String CREDENTIAL_IDENTIFIER = "efb52887-48d6-43b7-b14c-da7896fbf54d";
    private static final String ITEM_ID = "de9cbf02-2fbc-4d61-a627-f97851f6840b";
    private static final String WALLET_SUBJECT_ID =
            "urn:fdc:wallet.account.gov.uk:2024:DtPT8x-dp_73tnlY3KNTiCitziN9GEherD16bqxNt9i";
    public static final UUID NOTIFICATION_ID =
            UUID.fromString("123e4567-e89b-12d3-a456-426655440000");
    public static final String NINO = "QQ123456C";
    public static final String DOCUMENT_NUMBER = "EDWAR583720SE5RO";
    private static final String NONCE = "134e0c41-a8b4-46d4-aec8-cd547e125589";
    private static final StatusListClient.StatusListInformation STATUS_LIST_ISSUE_RESPONSE =
            new StatusListClient.StatusListInformation(3, "https://example.com/status-list");
    private static final long EXPIRY_TIME = 1234567890L;
    private static final String CREDENTIAL = "test-credential";

    @BeforeEach
    void setUp() throws AccessTokenValidationException, ProofJwtValidationException {
        mockCachedCredentialOffer = getMockCredentialOfferCacheItem(WALLET_SUBJECT_ID, "300");
        mockProofJwt = new MockProofBuilder("ES256").build();
        mockProofJwtData = getMockProofJwtData(NONCE);
        when(mockProofJwtService.verifyProofJwt(mockProofJwt)).thenReturn(mockProofJwtData);
        mockAccessToken = new MockAccessTokenBuilder("ES256").build();
        when(mockAccessTokenService.verifyAccessToken(mockAccessToken))
                .thenReturn(getMockAccessTokenData());

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
    }

    @Test
    void Should_ThrowNonceValidationException_When_NonceValuesDontMatch()
            throws ProofJwtValidationException {
        mockProofJwtData = getMockProofJwtData("not_the_same_nonce");
        when(mockProofJwtService.verifyProofJwt(mockProofJwt)).thenReturn(mockProofJwtData);

        NonceValidationException exception =
                assertThrows(
                        NonceValidationException.class,
                        () -> credentialService.getCredential(mockAccessToken, mockProofJwt));
        assertEquals(
                "Access token c_nonce claim does not match Proof JWT nonce claim",
                exception.getMessage());
    }

    @Test
    void Should_ThrowCredentialServiceException_When_DataStoreExceptionIsThrown()
            throws DataStoreException {
        when(mockDynamoDbService.getCredentialOffer(CREDENTIAL_IDENTIFIER))
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
    void Should_ThrowCredentialOfferValidationException_When_CredentialOfferNotFound()
            throws DataStoreException {
        when(mockDynamoDbService.getCredentialOffer(CREDENTIAL_IDENTIFIER)).thenReturn(null);

        CredentialOfferException exception =
                assertThrows(
                        CredentialOfferException.class,
                        () -> credentialService.getCredential(mockAccessToken, mockProofJwt));
        assertEquals("Credential offer validation failed", exception.getMessage());
        verify(mockLogger)
                .error("Credential offer {} was not found", "efb52887-48d6-43b7-b14c-da7896fbf54d");
    }

    @Test
    void Should_ThrowCredentialOfferException_When_CredentialOfferIsExpired()
            throws DataStoreException {
        mockCachedCredentialOffer = getMockCredentialOfferCacheItem(WALLET_SUBJECT_ID, "-1");
        when(mockDynamoDbService.getCredentialOffer(CREDENTIAL_IDENTIFIER))
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
    void Should_ThrowAccessTokenValidationException_When_WalletSubjectIDsDontMatch()
            throws DataStoreException {
        mockCachedCredentialOffer =
                getMockCredentialOfferCacheItem("not_the_same_wallet_subject_id", "300");
        when(mockDynamoDbService.getCredentialOffer(CREDENTIAL_IDENTIFIER))
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
    void Should_DeleteCredentialOffer() throws Exception {
        Document mockDocument = getMockSocialSecurityDocument();
        when(mockDynamoDbService.getCredentialOffer(CREDENTIAL_IDENTIFIER))
                .thenReturn(mockCachedCredentialOffer);
        when(mockDocumentStoreClient.getDocument(ITEM_ID)).thenReturn(mockDocument);
        CredentialHandler mockHandler = mock(CredentialHandler.class);
        when(mockCredentialHandlerFactory.createHandler(SOCIAL_SECURITY_VC_TYPE))
                .thenReturn(mockHandler);
        when(mockHandler.buildCredential(mockDocument, mockProofJwtData, Optional.empty()))
                .thenReturn(CREDENTIAL);

        credentialService.getCredential(mockAccessToken, mockProofJwt);

        verify(mockDynamoDbService).deleteCredentialOffer(CREDENTIAL_IDENTIFIER);
    }

    @Test
    void Should_CallExpiryCalculator_To_CalculateCredentialExpiry() throws Exception {
        Document mockDocument = getMockSocialSecurityDocument();
        when(mockDynamoDbService.getCredentialOffer(CREDENTIAL_IDENTIFIER))
                .thenReturn(mockCachedCredentialOffer);
        when(mockDocumentStoreClient.getDocument(ITEM_ID)).thenReturn(mockDocument);
        CredentialHandler mockHandler = mock(CredentialHandler.class);
        when(mockCredentialHandlerFactory.createHandler(SOCIAL_SECURITY_VC_TYPE))
                .thenReturn(mockHandler);
        when(mockHandler.buildCredential(mockDocument, mockProofJwtData, Optional.empty()))
                .thenReturn(CREDENTIAL);
        when(mockExpiryCalculator.calculateExpiry(mockDocument)).thenReturn(EXPIRY_TIME);
        try (MockedStatic<UUID> mockedUUID = mockStatic(UUID.class)) {
            mockedUUID.when(UUID::randomUUID).thenReturn(NOTIFICATION_ID);

            credentialService.getCredential(mockAccessToken, mockProofJwt);

            verify(mockExpiryCalculator).calculateExpiry(mockDocument);
        }
    }

    @Test
    void Should_NotCallStatusListClient_When_IssuingJWTCredentials() throws Exception {
        Document mockDocument = getMockSocialSecurityDocument();
        when(mockDynamoDbService.getCredentialOffer(CREDENTIAL_IDENTIFIER))
                .thenReturn(mockCachedCredentialOffer);
        when(mockDocumentStoreClient.getDocument(ITEM_ID)).thenReturn(mockDocument);
        CredentialHandler mockHandler = mock(CredentialHandler.class);
        when(mockCredentialHandlerFactory.createHandler(SOCIAL_SECURITY_VC_TYPE))
                .thenReturn(mockHandler);
        when(mockHandler.buildCredential(mockDocument, mockProofJwtData, Optional.empty()))
                .thenReturn(CREDENTIAL);
        when(mockExpiryCalculator.calculateExpiry(mockDocument)).thenReturn(EXPIRY_TIME);

        credentialService.getCredential(mockAccessToken, mockProofJwt);

        verify(mockStatusListClient, never()).getIndex(anyLong());
    }

    @Test
    void Should_CallStatusListClient_When_IssuingMDLCredentials() throws Exception {
        Document mockDocument = getMockMobileDrivingLicenceDocument();
        when(mockDynamoDbService.getCredentialOffer(CREDENTIAL_IDENTIFIER))
                .thenReturn(mockCachedCredentialOffer);
        when(mockDocumentStoreClient.getDocument(ITEM_ID)).thenReturn(mockDocument);
        when(mockStatusListClient.getIndex(EXPIRY_TIME)).thenReturn(STATUS_LIST_ISSUE_RESPONSE);
        CredentialHandler mockHandler = mock(CredentialHandler.class);
        when(mockCredentialHandlerFactory.createHandler(MDL_VC_TYPE)).thenReturn(mockHandler);
        when(mockHandler.buildCredential(
                        mockDocument, mockProofJwtData, Optional.of(STATUS_LIST_ISSUE_RESPONSE)))
                .thenReturn(CREDENTIAL);
        when(mockExpiryCalculator.calculateExpiry(mockDocument)).thenReturn(EXPIRY_TIME);

        credentialService.getCredential(mockAccessToken, mockProofJwt);

        verify(mockStatusListClient).getIndex(EXPIRY_TIME);
    }

    @Test
    void Should_ThrowCredentialServiceException_When_SigningExceptionIsThrown()
            throws DataStoreException,
                    SigningException,
                    DocumentStoreException,
                    ObjectStoreException,
                    CertificateException {
        when(mockDynamoDbService.getCredentialOffer(CREDENTIAL_IDENTIFIER))
                .thenReturn(mockCachedCredentialOffer);
        Document mockDocument = getMockSocialSecurityDocument();
        when(mockDocumentStoreClient.getDocument(ITEM_ID)).thenReturn(mockDocument);
        CredentialHandler mockHandler = mock(CredentialHandler.class);
        when(mockCredentialHandlerFactory.createHandler(SOCIAL_SECURITY_VC_TYPE))
                .thenReturn(mockHandler);
        when(mockHandler.buildCredential(mockDocument, mockProofJwtData, Optional.empty()))
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
        Document mockDocument = getMockMobileDrivingLicenceDocument();
        when(mockDynamoDbService.getCredentialOffer(CREDENTIAL_IDENTIFIER))
                .thenReturn(mockCachedCredentialOffer);
        when(mockDocumentStoreClient.getDocument(ITEM_ID)).thenReturn(mockDocument);
        when(mockExpiryCalculator.calculateExpiry(mockDocument)).thenReturn(EXPIRY_TIME);
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
        Document mockDocument = getMockMobileDrivingLicenceDocument();
        when(mockDynamoDbService.getCredentialOffer(CREDENTIAL_IDENTIFIER))
                .thenReturn(mockCachedCredentialOffer);
        when(mockDocumentStoreClient.getDocument(ITEM_ID)).thenReturn(mockDocument);
        CredentialHandler mockHandler = mock(CredentialHandler.class);
        when(mockCredentialHandlerFactory.createHandler(MDL_VC_TYPE)).thenReturn(mockHandler);
        when(mockExpiryCalculator.calculateExpiry(mockDocument)).thenReturn(EXPIRY_TIME);
        when(mockStatusListClient.getIndex(EXPIRY_TIME)).thenReturn(STATUS_LIST_ISSUE_RESPONSE);
        when(mockHandler.buildCredential(
                        eq(mockDocument), eq(mockProofJwtData), any(Optional.class)))
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
    void Should_ReturnCredentialResponse_When_IssuingSocialSecurityCredential() throws Exception {
        Document mockDocument = getMockSocialSecurityDocument();
        when(mockDynamoDbService.getCredentialOffer(CREDENTIAL_IDENTIFIER))
                .thenReturn(mockCachedCredentialOffer);
        when(mockDocumentStoreClient.getDocument(ITEM_ID)).thenReturn(mockDocument);
        CredentialHandler mockHandler = mock(CredentialHandler.class);
        when(mockCredentialHandlerFactory.createHandler(SOCIAL_SECURITY_VC_TYPE))
                .thenReturn(mockHandler);
        when(mockExpiryCalculator.calculateExpiry(mockDocument)).thenReturn(EXPIRY_TIME);
        when(mockHandler.buildCredential(mockDocument, mockProofJwtData, Optional.empty()))
                .thenReturn(CREDENTIAL);
        try (MockedStatic<UUID> mockedUUID = mockStatic(UUID.class)) {
            mockedUUID.when(UUID::randomUUID).thenReturn(NOTIFICATION_ID);

            CredentialResponse result =
                    credentialService.getCredential(mockAccessToken, mockProofJwt);

            assertEquals(CREDENTIAL, result.getCredentials().get(0).getCredentialObj());
            assertEquals(NOTIFICATION_ID.toString(), result.getNotificationId());
            verify(mockHandler).buildCredential(mockDocument, mockProofJwtData, Optional.empty());
            ArgumentCaptor<StoredCredential> storedCredentialCaptor =
                    ArgumentCaptor.forClass(StoredCredential.class);
            verify(mockDynamoDbService).saveStoredCredential(storedCredentialCaptor.capture());
            StoredCredential storedCredential = storedCredentialCaptor.getValue();
            assertEquals(CREDENTIAL_IDENTIFIER, storedCredential.getCredentialIdentifier());
            assertEquals(NOTIFICATION_ID.toString(), storedCredential.getNotificationId());
            assertEquals(WALLET_SUBJECT_ID, storedCredential.getWalletSubjectId());
            assertNull(
                    storedCredential.getStatusListUri(), "Should be null for non-MDL credentials");
            assertNull(
                    storedCredential.getStatusListIndex(),
                    "Should be null for non-MDL credentials");
            assertEquals(EXPIRY_TIME, storedCredential.getTimeToLive());
            assertEquals(NINO, storedCredential.getDocumentId());
        }
    }

    @Test
    void Should_ReturnCredentialResponse_When_IssuingMobileDrivingLicence() throws Exception {
        Document mockMobileDrivingLicenceDocument = getMockMobileDrivingLicenceDocument();
        when(mockDynamoDbService.getCredentialOffer(CREDENTIAL_IDENTIFIER))
                .thenReturn(mockCachedCredentialOffer);
        when(mockDocumentStoreClient.getDocument(ITEM_ID))
                .thenReturn(mockMobileDrivingLicenceDocument);
        CredentialHandler mockHandler = mock(CredentialHandler.class);
        when(mockCredentialHandlerFactory.createHandler(MDL_VC_TYPE)).thenReturn(mockHandler);
        when(mockExpiryCalculator.calculateExpiry(mockMobileDrivingLicenceDocument))
                .thenReturn(EXPIRY_TIME);
        when(mockStatusListClient.getIndex(EXPIRY_TIME)).thenReturn(STATUS_LIST_ISSUE_RESPONSE);
        when(mockHandler.buildCredential(
                        mockMobileDrivingLicenceDocument,
                        mockProofJwtData,
                        Optional.of(STATUS_LIST_ISSUE_RESPONSE)))
                .thenReturn(CREDENTIAL);
        try (MockedStatic<UUID> mockedUUID = mockStatic(UUID.class)) {
            mockedUUID.when(UUID::randomUUID).thenReturn(NOTIFICATION_ID);

            CredentialResponse result =
                    credentialService.getCredential(mockAccessToken, mockProofJwt);

            assertEquals(CREDENTIAL, result.getCredentials().get(0).getCredentialObj());
            assertEquals(NOTIFICATION_ID.toString(), result.getNotificationId());
            verify(mockStatusListClient).getIndex(EXPIRY_TIME);
            verify(mockHandler)
                    .buildCredential(
                            mockMobileDrivingLicenceDocument,
                            mockProofJwtData,
                            Optional.of(STATUS_LIST_ISSUE_RESPONSE));
            ArgumentCaptor<StoredCredential> storedCredentialCaptor =
                    ArgumentCaptor.forClass(StoredCredential.class);
            verify(mockDynamoDbService).saveStoredCredential(storedCredentialCaptor.capture());
            StoredCredential storedCredential = storedCredentialCaptor.getValue();
            assertEquals(CREDENTIAL_IDENTIFIER, storedCredential.getCredentialIdentifier());
            assertEquals(NOTIFICATION_ID.toString(), storedCredential.getNotificationId());
            assertEquals(WALLET_SUBJECT_ID, storedCredential.getWalletSubjectId());
            assertEquals(STATUS_LIST_ISSUE_RESPONSE.idx(), storedCredential.getStatusListIndex());
            assertEquals(STATUS_LIST_ISSUE_RESPONSE.uri(), storedCredential.getStatusListUri());
            assertEquals(EXPIRY_TIME, storedCredential.getTimeToLive());
            assertEquals(DOCUMENT_NUMBER, storedCredential.getDocumentId());
        }
    }

    private CachedCredentialOffer getMockCredentialOfferCacheItem(
            String walletSubjectId, String expiresInSeconds) {
        Long ttl = Instant.now().plusSeconds(Long.parseLong(expiresInSeconds)).getEpochSecond();
        return new CachedCredentialOffer(CREDENTIAL_IDENTIFIER, ITEM_ID, walletSubjectId, ttl);
    }

    private AccessTokenService.AccessTokenData getMockAccessTokenData() {
        return new AccessTokenService.AccessTokenData(
                CredentialServiceTest.WALLET_SUBJECT_ID,
                CredentialServiceTest.NONCE,
                CredentialServiceTest.CREDENTIAL_IDENTIFIER);
    }

    private ProofJwtService.ProofJwtData getMockProofJwtData(String nonce) {
        return new ProofJwtService.ProofJwtData(
                "did:key:MFkwEwYHKoZIzj0CAQYIKoZIzj0DAQcDQgAEaUItVYrAvVK+1efrBvWDXtmapkl1PHqXUHytuK5/F7lfIXprXHD9zIdAinRrWSFeh28OJJzoSH1zqzOJ+ZhFOA==",
                nonce,
                mockEcPublicKey);
    }

    public static Document getMockSocialSecurityDocument() {
        HashMap<String, Object> data = new HashMap<>();
        data.put("familyName", "Edwards Green");
        data.put("givenName", "Sarah Elizabeth");
        data.put("nino", NINO);
        data.put("title", "Miss");
        data.put("credentialTtlMinutes", "43200");
        return new Document(ITEM_ID, NINO, data, "SocialSecurityCredential");
    }

    public static Document getMockMobileDrivingLicenceDocument() {
        HashMap<String, Object> data = new HashMap<>();
        data.put("family_name", "Edwards Green");
        data.put("given_name", "Sarah Elizabeth");
        data.put("document_number", DOCUMENT_NUMBER);
        data.put("credentialTtlMinutes", 43200L);
        return new Document(ITEM_ID, DOCUMENT_NUMBER, data, MDL_VC_TYPE);
    }
}
