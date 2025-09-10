package uk.gov.di.mobile.wallet.cri.credential;

import com.nimbusds.jwt.SignedJWT;
import org.jetbrains.annotations.NotNull;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.slf4j.Logger;
import testUtils.MockAccessTokenBuilder;
import testUtils.MockProofBuilder;
import uk.gov.di.mobile.wallet.cri.models.CachedCredentialOffer;
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

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.containsString;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@SuppressWarnings("unchecked")
@ExtendWith(MockitoExtension.class)
class CredentialServiceTest {

    @Mock private CredentialHandlerFactory mockCredentialHandlerFactory;
    @Mock private CredentialExpiryCalculator mockExpiryCalculator;
    @Mock private StatusListRequestTokenBuilder mockStatusListRequestTokenBuilder;

    @Mock private Logger mockLogger;
    @Mock private ECPublicKey mockEcPublicKey;

    private final DynamoDbService mockDynamoDbService = mock(DynamoDbService.class);
    private final AccessTokenService mockAccessTokenService = mock(AccessTokenService.class);
    private final ProofJwtService mockProofJwtService = mock(ProofJwtService.class);
    private final DocumentStoreClient mockDocumentStoreClient = mock(DocumentStoreClient.class);

    private CredentialService credentialService;
    private CachedCredentialOffer mockCachedCredentialOffer;
    private SignedJWT mockProofJwt;
    private SignedJWT mockAccessToken;
    private ProofJwtService.ProofJwtData mockAccessProofJwtData;
    private String mockCredentialJwt;

    private static final String DOCUMENT_ID = "de9cbf02-2fbc-4d61-a627-f97851f6840b";
    private static final String NOTIFICATION_ID = "3fwe98js";
    private static final String CREDENTIAL_IDENTIFIER = "efb52887-48d6-43b7-b14c-da7896fbf54d";
    private static final String NONCE = "134e0c41-a8b4-46d4-aec8-cd547e125589";
    private static final String WALLET_SUBJECT_ID =
            "urn:fdc:wallet.account.gov.uk:2024:DtPT8x-dp_73tnlY3KNTiCitziN9GEherD16bqxNt9i";
    private static final String DID_KEY =
            "did:key:MFkwEwYHKoZIzj0CAQYIKoZIzj0DAQcDQgAEaUItVYrAvVK+1efrBvWDXtmapkl1PHqXUHytuK5/F7lfIXprXHD9zIdAinRrWSFeh28OJJzoSH1zqzOJ+ZhFOA==";

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
                        mockStatusListRequestTokenBuilder) {
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
    }

    @Test
    void Should_ThrowNonceValidationException_When_NonceValuesDoNotMatch()
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
                .thenReturn(getMockSocialSecurityDocument(DOCUMENT_ID));
        CredentialHandler mockHandler = mock(CredentialHandler.class);
        when(mockCredentialHandlerFactory.createHandler("SocialSecurityCredential"))
                .thenReturn(mockHandler);
        when(mockHandler.buildCredential(any(), any()))
                .thenThrow(new SigningException("Some signing error", new RuntimeException()));
        CredentialServiceException exception =
                assertThrows(
                        CredentialServiceException.class,
                        () -> credentialService.getCredential(mockAccessToken, mockProofJwt));
        assertThat(
                exception.getMessage(),
                containsString("Failed to issue credential due to an internal error"));
    }

    @Test
    void Should_ReturnCredentialResponse()
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
        Document mockDocument = getMockSocialSecurityDocument(DOCUMENT_ID);
        when(mockDynamoDbService.getCredentialOffer(anyString()))
                .thenReturn(mockCachedCredentialOffer);
        when(mockDocumentStoreClient.getDocument(anyString())).thenReturn(mockDocument);
        CredentialHandler mockHandler = mock(CredentialHandler.class);
        when(mockCredentialHandlerFactory.createHandler("SocialSecurityCredential"))
                .thenReturn(mockHandler);
        when(mockHandler.buildCredential(any(), any())).thenReturn(mockCredentialJwt);

        CredentialResponse credentialServiceReturnValue =
                credentialService.getCredential(mockAccessToken, mockProofJwt);

        assertEquals(mockCredentialJwt, credentialServiceReturnValue.getCredential());
        assertEquals("3fwe98js", NOTIFICATION_ID);
        verify(mockAccessTokenService).verifyAccessToken(mockAccessToken);
        verify(mockProofJwtService).verifyProofJwt(mockProofJwt);
        verify(mockDynamoDbService, times(1)).getCredentialOffer(CREDENTIAL_IDENTIFIER);
        verify(mockDynamoDbService, times(1)).deleteCredentialOffer(CREDENTIAL_IDENTIFIER);
        verify(mockHandler, times(1)).buildCredential(mockDocument, mockAccessProofJwtData);
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

    public static @NotNull Document getMockSocialSecurityDocument(String documentId) {
        HashMap<String, Object> data = new HashMap<>();
        data.put("familyName", "Edwards Green");
        data.put("givenName", "Sarah Elizabeth");
        data.put("nino", "QQ123456C");
        data.put("title", "Miss");
        data.put("credentialTtlMinutes", "525600");
        return new Document(documentId, data, "SocialSecurityCredential");
    }
}
