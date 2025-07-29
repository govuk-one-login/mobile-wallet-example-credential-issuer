package uk.gov.di.mobile.wallet.cri.credential;

import com.nimbusds.jwt.SignedJWT;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.slf4j.Logger;
import testUtils.MockAccessTokenBuilder;
import testUtils.MockProofBuilder;
import uk.gov.di.mobile.wallet.cri.credential.basic_check_credential.BasicCheckCredentialSubject;
import uk.gov.di.mobile.wallet.cri.credential.digital_veteran_card.VeteranCardCredentialSubject;
import uk.gov.di.mobile.wallet.cri.credential.mobile_driving_licence.DrivingLicenceDocument;
import uk.gov.di.mobile.wallet.cri.credential.mobile_driving_licence.MobileDrivingLicenceService;
import uk.gov.di.mobile.wallet.cri.credential.social_security_credential.SocialSecurityCredentialSubject;
import uk.gov.di.mobile.wallet.cri.models.CachedCredentialOffer;
import uk.gov.di.mobile.wallet.cri.models.StoredCredential;
import uk.gov.di.mobile.wallet.cri.services.authentication.AccessTokenService;
import uk.gov.di.mobile.wallet.cri.services.authentication.AccessTokenValidationException;
import uk.gov.di.mobile.wallet.cri.services.data_storage.DataStoreException;
import uk.gov.di.mobile.wallet.cri.services.data_storage.DynamoDbService;
import uk.gov.di.mobile.wallet.cri.services.signing.SigningException;

import java.time.Clock;
import java.time.Instant;
import java.time.ZoneOffset;
import java.time.temporal.ChronoUnit;
import java.util.Date;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.containsString;
import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyLong;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.*;
import static testUtils.MockDocuments.getMockBasicCheckDocument;
import static testUtils.MockDocuments.getMockDocumentWithInvalidVcType;
import static testUtils.MockDocuments.getMockMobileDrivingLicence;
import static testUtils.MockDocuments.getMockSocialSecurityDocument;
import static testUtils.MockDocuments.getMockVeteranCardDocument;

@SuppressWarnings("unchecked")
@ExtendWith(MockitoExtension.class)
class CredentialServiceTest {

    @Mock private CredentialBuilder<?> mockCredentialBuilder;
    @Mock private MobileDrivingLicenceService mockMobileDrivingLicenceService;

    @Mock private Logger mockLogger;

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
    private final Clock clock = Clock.fixed(Instant.now(), ZoneOffset.UTC);

    private static final String DOCUMENT_ID = "de9cbf02-2fbc-4d61-a627-f97851f6840b";
    private static final String NOTIFICATION_ID = "3fwe98js";
    private static final String CREDENTIAL_IDENTIFIER = "efb52887-48d6-43b7-b14c-da7896fbf54d";
    private static final String NONCE = "134e0c41-a8b4-46d4-aec8-cd547e125589";
    private static final String WALLET_SUBJECT_ID =
            "urn:fdc:wallet.account.gov.uk:2024:DtPT8x-dp_73tnlY3KNTiCitziN9GEherD16bqxNt9i";
    private static final String DID_KEY =
            "did:key:MFkwEwYHKoZIzj0CAQYIKoZIzj0DAQcDQgAEaUItVYrAvVK+1efrBvWDXtmapkl1PHqXUHytuK5/F7lfIXprXHD9zIdAinRrWSFeh28OJJzoSH1zqzOJ+ZhFOA==";
    private static final long TTL_SECONDS = 525600L;

    @BeforeEach
    void setUp() throws AccessTokenValidationException, ProofJwtValidationException {
        mockCachedCredentialOffer =
                getMockCredentialOfferCacheItem(WALLET_SUBJECT_ID, false, "300");
        mockProofJwt = new MockProofBuilder("ES256").build();
        mockAccessToken = new MockAccessTokenBuilder("ES256").build();
        credentialService =
                new CredentialService(
                        mockDynamoDbService,
                        mockAccessTokenService,
                        mockProofJwtService,
                        mockDocumentStoreClient,
                        mockCredentialBuilder,
                        mockMobileDrivingLicenceService,
                        clock) {
                    @Override
                    protected Logger getLogger() {
                        return mockLogger;
                    }
                };
        mockAccessProofJwtData = getMockProofJwtData(NONCE);
        when(mockProofJwtService.verifyProofJwt(any())).thenReturn(mockAccessProofJwtData);
        when(mockAccessTokenService.verifyAccessToken(any())).thenReturn(getMockAccessTokenData());

        mockCredentialJwt =
                "eyJhbGciOiJIUzI1NiIsInR5cCI6IkpXVCJ9.eyJzdWIiOiIxMjM0NTY3ODkwIiwibmFtZSI6IkpvaG4gRG9lIiwiaWF0IjoxNTE2MjM5MDIyfQ.SflKxwRJSMeKKF2QT4fwpMeJf36POk6yJV_adQssw5c";
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
    void Should_ThrowProofJwtValidationException_When_CredentialOfferNotFound()
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
    void Should_ThrowDataStoreException_When_CallToDatabaseThrowsError() throws DataStoreException {
        when(mockDynamoDbService.getCredentialOffer(anyString()))
                .thenThrow(new DataStoreException("Some database error"));

        DataStoreException exception =
                assertThrows(
                        DataStoreException.class,
                        () -> credentialService.getCredential(mockAccessToken, mockProofJwt));

        assertEquals("Some database error", exception.getMessage());
    }

    @Test
    void Should_ThrowAccessTokenValidationException_When_WalletSubjectIDsDontMatch()
            throws DataStoreException {
        mockCachedCredentialOffer =
                getMockCredentialOfferCacheItem("not_the_same_wallet_subject_id", false, "300");
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
        mockCachedCredentialOffer = getMockCredentialOfferCacheItem(WALLET_SUBJECT_ID, false, "-1");
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
    void Should_ThrowCredentialOfferException_When_CredentialOfferHasBeenRedeemed()
            throws DataStoreException {
        mockCachedCredentialOffer = getMockCredentialOfferCacheItem(WALLET_SUBJECT_ID, true, "300");
        when(mockDynamoDbService.getCredentialOffer(anyString()))
                .thenReturn(mockCachedCredentialOffer);

        CredentialOfferException exception =
                assertThrows(
                        CredentialOfferException.class,
                        () -> credentialService.getCredential(mockAccessToken, mockProofJwt));
        assertEquals("Credential offer validation failed", exception.getMessage());
        verify(mockLogger)
                .error(
                        "Credential offer {} has already been redeemed",
                        "efb52887-48d6-43b7-b14c-da7896fbf54d");
    }

    @Test
    void Should_Throw_CredentialServiceException_When_CredentialBuilderThrowsSigningException()
            throws DataStoreException, SigningException, DocumentStoreException {
        when(mockDynamoDbService.getCredentialOffer(anyString()))
                .thenReturn(mockCachedCredentialOffer);
        when(mockDocumentStoreClient.getDocument(anyString()))
                .thenReturn(getMockSocialSecurityDocument(DOCUMENT_ID));
        when(mockCredentialBuilder.buildCredential(any(), any(), anyLong()))
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
    void Should_BuildSocialSecurityCredentialSubject_And_SaveStoredCredential()
            throws AccessTokenValidationException,
                    ProofJwtValidationException,
                    NonceValidationException,
                    DataStoreException,
                    CredentialServiceException,
                    CredentialOfferException,
                    SigningException,
                    DocumentStoreException {
        when(mockDynamoDbService.getCredentialOffer(anyString()))
                .thenReturn(mockCachedCredentialOffer);
        when(mockDocumentStoreClient.getDocument(anyString()))
                .thenReturn(getMockSocialSecurityDocument(DOCUMENT_ID));
        when(mockCredentialBuilder.buildCredential(any(), any(), anyLong()))
                .thenReturn(mockCredentialJwt);

        Instant now = clock.instant();
        Instant expiry = now.plus(TTL_SECONDS, ChronoUnit.MINUTES);
        Date expirationTime = Date.from(expiry);

        doNothing().when(mockDynamoDbService).saveStoredCredential(any(StoredCredential.class));
        assertEquals(
                expirationTime,
                credentialService
                        .getCredential(mockAccessToken, mockProofJwt)
                        .getCredential()
                        .expirationTime());

        verify((CredentialBuilder<SocialSecurityCredentialSubject>) mockCredentialBuilder, times(1))
                .buildCredential(
                        any(SocialSecurityCredentialSubject.class),
                        eq(CredentialType.SOCIAL_SECURITY_CREDENTIAL),
                        eq(TTL_SECONDS));
        verify(mockDynamoDbService).saveStoredCredential(any(StoredCredential.class));
    }

    @Test
    void Should_BuildBasicCheckCredentialSubject_And_SaveStoredCredential()
            throws AccessTokenValidationException,
                    ProofJwtValidationException,
                    NonceValidationException,
                    DataStoreException,
                    CredentialServiceException,
                    CredentialOfferException,
                    SigningException,
                    DocumentStoreException {
        when(mockDynamoDbService.getCredentialOffer(anyString()))
                .thenReturn(mockCachedCredentialOffer);
        when(mockDocumentStoreClient.getDocument(anyString()))
                .thenReturn(getMockBasicCheckDocument(DOCUMENT_ID));
        when(mockCredentialBuilder.buildCredential(any(), any(), anyLong()))
                .thenReturn(mockCredentialJwt);

        Instant now = clock.instant();
        Instant expiry = now.plus(TTL_SECONDS, ChronoUnit.MINUTES);
        Date expirationTime = Date.from(expiry);

        doNothing().when(mockDynamoDbService).saveStoredCredential(any(StoredCredential.class));
        assertEquals(
                expirationTime,
                credentialService
                        .getCredential(mockAccessToken, mockProofJwt)
                        .getCredential()
                        .expirationTime());

        verify((CredentialBuilder<BasicCheckCredentialSubject>) mockCredentialBuilder, times(1))
                .buildCredential(
                        any(BasicCheckCredentialSubject.class),
                        eq(CredentialType.BASIC_DISCLOSURE_CREDENTIAL),
                        eq(TTL_SECONDS));

        verify(mockDynamoDbService).saveStoredCredential(any(StoredCredential.class));
    }

    @Test
    void Should_BuildVeteranCardCredentialSubject_And_SaveStoredCredential()
            throws AccessTokenValidationException,
                    ProofJwtValidationException,
                    NonceValidationException,
                    DataStoreException,
                    CredentialServiceException,
                    CredentialOfferException,
                    SigningException,
                    DocumentStoreException {
        when(mockDynamoDbService.getCredentialOffer(anyString()))
                .thenReturn(mockCachedCredentialOffer);
        when(mockDocumentStoreClient.getDocument(anyString()))
                .thenReturn(getMockVeteranCardDocument(DOCUMENT_ID));
        when(mockCredentialBuilder.buildCredential(any(), any(), anyLong()))
                .thenReturn(mockCredentialJwt);

        Instant now = clock.instant();
        Instant expiry = now.plus(TTL_SECONDS, ChronoUnit.MINUTES);
        Date expirationTime = Date.from(expiry);

        doNothing().when(mockDynamoDbService).saveStoredCredential(any(StoredCredential.class));
        assertEquals(
                expirationTime,
                credentialService
                        .getCredential(mockAccessToken, mockProofJwt)
                        .getCredential()
                        .expirationTime());

        verify((CredentialBuilder<VeteranCardCredentialSubject>) mockCredentialBuilder, times(1))
                .buildCredential(
                        any(VeteranCardCredentialSubject.class),
                        eq(CredentialType.DIGITAL_VETERAN_CARD),
                        eq(TTL_SECONDS));
        verify(mockDynamoDbService).saveStoredCredential(any(StoredCredential.class));
    }

    @Test
    void Should_BuildMobileDrivingLicenceCredential() throws Exception {
        when(mockDynamoDbService.getCredentialOffer(anyString()))
                .thenReturn(mockCachedCredentialOffer);
        when(mockDocumentStoreClient.getDocument(anyString()))
                .thenReturn(getMockMobileDrivingLicence(DOCUMENT_ID));
        doNothing().when(mockDynamoDbService).saveStoredCredential(any(StoredCredential.class));

        credentialService.getCredential(mockAccessToken, mockProofJwt);

        verify(mockMobileDrivingLicenceService, times(1))
                .createMobileDrivingLicence(any(DrivingLicenceDocument.class));
        verify(mockDynamoDbService).saveStoredCredential(any(StoredCredential.class));
    }

    @Test
    void Should_ThrowCredentialServiceException_When_DocumentVcTypeIsUnknown()
            throws DataStoreException, DocumentStoreException {
        when(mockDynamoDbService.getCredentialOffer(anyString()))
                .thenReturn(mockCachedCredentialOffer);
        when(mockDocumentStoreClient.getDocument(anyString()))
                .thenReturn(getMockDocumentWithInvalidVcType(DOCUMENT_ID));

        CredentialServiceException exception =
                assertThrows(
                        CredentialServiceException.class,
                        () -> credentialService.getCredential(mockAccessToken, mockProofJwt));

        assertThat(
                exception.getMessage(),
                containsString("Invalid verifiable credential type SomeOtherVcType"));
    }

    @Test
    void Should_ReturnCredentialResponse()
            throws AccessTokenValidationException,
                    ProofJwtValidationException,
                    NonceValidationException,
                    DataStoreException,
                    CredentialServiceException,
                    CredentialOfferException,
                    SigningException,
                    DocumentStoreException {
        when(mockDynamoDbService.getCredentialOffer(anyString()))
                .thenReturn(mockCachedCredentialOffer);
        when(mockDocumentStoreClient.getDocument(anyString()))
                .thenReturn(getMockSocialSecurityDocument(DOCUMENT_ID));
        when(mockCredentialBuilder.buildCredential(any(), any(), anyLong()))
                .thenReturn(mockCredentialJwt);

        Instant now = clock.instant();
        Instant expiry = now.plus(TTL_SECONDS, ChronoUnit.MINUTES);
        Date expirationTime = Date.from(expiry);

        CredentialResponse credentialServiceReturnValue =
                credentialService.getCredential(mockAccessToken, mockProofJwt);

        assertEquals(mockCredentialJwt, credentialServiceReturnValue.getCredential().credential());
        assertEquals(expirationTime, credentialServiceReturnValue.getCredential().expirationTime());
        assertEquals(NOTIFICATION_ID, credentialServiceReturnValue.getNotificationId());

        verify(mockAccessTokenService).verifyAccessToken(mockAccessToken);
        verify(mockProofJwtService).verifyProofJwt(mockProofJwt);
        verify(mockDynamoDbService, times(1)).getCredentialOffer(CREDENTIAL_IDENTIFIER);
        verify(mockDynamoDbService, times(1)).updateCredentialOffer(mockCachedCredentialOffer);
        verify((CredentialBuilder<SocialSecurityCredentialSubject>) mockCredentialBuilder, times(1))
                .buildCredential(
                        any(SocialSecurityCredentialSubject.class),
                        eq(CredentialType.SOCIAL_SECURITY_CREDENTIAL),
                        eq(TTL_SECONDS));
    }

    private CachedCredentialOffer getMockCredentialOfferCacheItem(
            String walletSubjectId, Boolean redeemed, String expiresInSeconds) {
        Long expiry = Instant.now().plusSeconds(Long.parseLong(expiresInSeconds)).getEpochSecond();
        Long ttl = Instant.now().plusSeconds(1000).getEpochSecond();
        return new CachedCredentialOffer(
                CREDENTIAL_IDENTIFIER,
                DOCUMENT_ID,
                walletSubjectId,
                NOTIFICATION_ID,
                redeemed,
                expiry,
                ttl);
    }

    private AccessTokenService.AccessTokenData getMockAccessTokenData() {
        return new AccessTokenService.AccessTokenData(
                CredentialServiceTest.WALLET_SUBJECT_ID,
                CredentialServiceTest.NONCE,
                CredentialServiceTest.CREDENTIAL_IDENTIFIER);
    }

    private ProofJwtService.ProofJwtData getMockProofJwtData(String nonce) {
        return new ProofJwtService.ProofJwtData(CredentialServiceTest.DID_KEY, nonce);
    }
}
