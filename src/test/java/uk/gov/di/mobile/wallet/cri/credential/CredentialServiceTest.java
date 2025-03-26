package uk.gov.di.mobile.wallet.cri.credential;

import com.nimbusds.jwt.SignedJWT;
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
import org.slf4j.Logger;
import testUtils.MockAccessTokenBuilder;
import testUtils.MockProofBuilder;
import uk.gov.di.mobile.wallet.cri.credential.basic_check_credential.BasicCheckCredentialSubject;
import uk.gov.di.mobile.wallet.cri.credential.digital_veteran_card.VeteranCardCredentialSubject;
import uk.gov.di.mobile.wallet.cri.credential.social_security_credential.SocialSecurityCredentialSubject;
import uk.gov.di.mobile.wallet.cri.models.CredentialOfferCacheItem;
import uk.gov.di.mobile.wallet.cri.services.ConfigurationService;
import uk.gov.di.mobile.wallet.cri.services.authentication.AccessTokenService;
import uk.gov.di.mobile.wallet.cri.services.authentication.AccessTokenValidationException;
import uk.gov.di.mobile.wallet.cri.services.data_storage.DataStoreException;
import uk.gov.di.mobile.wallet.cri.services.data_storage.DynamoDbService;
import uk.gov.di.mobile.wallet.cri.services.signing.SigningException;

import java.net.URI;
import java.net.URISyntaxException;
import java.security.NoSuchAlgorithmException;
import java.text.ParseException;
import java.time.Instant;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.containsString;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.eq;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static testUtils.MockDocuments.*;

@SuppressWarnings("unchecked")
@ExtendWith(MockitoExtension.class)
class CredentialServiceTest {

    @Mock private Client mockHttpClient;
    @Mock private WebTarget mockWebTarget;
    @Mock private Invocation.Builder mockInvocationBuilder;
    @Mock private Response mockResponse;
    @Mock private CredentialBuilder<?> mockCredentialBuilder;
    @Mock private Logger mockLogger;

    private final DynamoDbService mockDynamoDbService = mock(DynamoDbService.class);
    private final AccessTokenService mockAccessTokenService = mock(AccessTokenService.class);
    private final ProofJwtService mockProofJwtService = mock(ProofJwtService.class);
    private final ConfigurationService mockConfigurationService = mock(ConfigurationService.class);

    private CredentialService credentialService;
    private CredentialOfferCacheItem mockCredentialOfferCacheItem;
    private SignedJWT mockProofJwt;
    private SignedJWT mockAccessToken;
    private ProofJwtService.ProofJwtData mockAccessProofJwtData;
    private SignedJWT mockCredentialJwt;

    private static final String DOCUMENT_ID = "de9cbf02-2fbc-4d61-a627-f97851f6840b";
    private static final String NOTIFICATION_ID = "3fwe98js";
    private static final String CREDENTIAL_IDENTIFIER = "efb52887-48d6-43b7-b14c-da7896fbf54d";
    private static final String NONCE = "134e0c41-a8b4-46d4-aec8-cd547e125589";
    private static final String WALLET_SUBJECT_ID =
            "urn:fdc:wallet.account.gov.uk:2024:DtPT8x-dp_73tnlY3KNTiCitziN9GEherD16bqxNt9i";
    private static final String DID_KEY =
            "did:key:MFkwEwYHKoZIzj0CAQYIKoZIzj0DAQcDQgAEaUItVYrAvVK+1efrBvWDXtmapkl1PHqXUHytuK5/F7lfIXprXHD9zIdAinRrWSFeh28OJJzoSH1zqzOJ+ZhFOA==";

    @BeforeEach
    void setUp()
            throws AccessTokenValidationException, ProofJwtValidationException, ParseException {
        mockCredentialOfferCacheItem =
                getMockCredentialOfferCacheItem(WALLET_SUBJECT_ID, false, "300");
        mockProofJwt = new MockProofBuilder("ES256").build();
        mockAccessToken = new MockAccessTokenBuilder("ES256").build();
        credentialService =
                new CredentialService(
                        mockConfigurationService,
                        mockDynamoDbService,
                        mockAccessTokenService,
                        mockProofJwtService,
                        mockHttpClient,
                        mockCredentialBuilder) {
                    @Override
                    protected Logger getLogger() {
                        return mockLogger;
                    }
                };
        mockAccessProofJwtData = getMockProofJwtData(NONCE);
        when(mockProofJwtService.verifyProofJwt(any())).thenReturn(mockAccessProofJwtData);
        when(mockAccessTokenService.verifyAccessToken(any())).thenReturn(getMockAccessTokenData());

        mockCredentialJwt =
                SignedJWT.parse(
                        "eyJhbGciOiJIUzI1NiIsInR5cCI6IkpXVCJ9.eyJzdWIiOiIxMjM0NTY3ODkwIiwibmFtZSI6IkpvaG4gRG9lIiwiaWF0IjoxNTE2MjM5MDIyfQ.SflKxwRJSMeKKF2QT4fwpMeJf36POk6yJV_adQssw5c");
    }

    @Test
    void Should_ThrowProofJwtValidationException_When_NonceValuesDontMatch()
            throws ProofJwtValidationException {
        mockAccessProofJwtData = getMockProofJwtData("not_the_same_nonce");
        when(mockProofJwtService.verifyProofJwt(any())).thenReturn(mockAccessProofJwtData);

        ProofJwtValidationException exception =
                assertThrows(
                        ProofJwtValidationException.class,
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
        mockCredentialOfferCacheItem =
                getMockCredentialOfferCacheItem("not_the_same_wallet_subject_id", false, "300");
        when(mockDynamoDbService.getCredentialOffer(anyString()))
                .thenReturn(mockCredentialOfferCacheItem);

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
        mockCredentialOfferCacheItem =
                getMockCredentialOfferCacheItem(WALLET_SUBJECT_ID, false, "-1");
        when(mockDynamoDbService.getCredentialOffer(anyString()))
                .thenReturn(mockCredentialOfferCacheItem);

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
        mockCredentialOfferCacheItem =
                getMockCredentialOfferCacheItem(WALLET_SUBJECT_ID, true, "300");
        when(mockDynamoDbService.getCredentialOffer(anyString()))
                .thenReturn(mockCredentialOfferCacheItem);

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
    void Should_Throw_RuntimeException_When_Document_Endpoint_Returns_500()
            throws DataStoreException {
        when(mockDynamoDbService.getCredentialOffer(anyString()))
                .thenReturn(mockCredentialOfferCacheItem);
        when(mockHttpClient.target(any(URI.class))).thenReturn(mockWebTarget);
        when(mockWebTarget.request(MediaType.APPLICATION_JSON)).thenReturn(mockInvocationBuilder);
        when(mockInvocationBuilder.get()).thenReturn(mockResponse);
        when(mockResponse.getStatus()).thenReturn(500);

        CredentialServiceException exception =
                assertThrows(
                        CredentialServiceException.class,
                        () -> credentialService.getCredential(mockAccessToken, mockProofJwt));

        assertThat(
                exception.getMessage(),
                containsString(
                        "Request to fetch document de9cbf02-2fbc-4d61-a627-f97851f6840b failed with status code 500"));
    }

    @Test
    void Should_BuildSocialSecurityCredentialSubject()
            throws AccessTokenValidationException,
                    ProofJwtValidationException,
                    DataStoreException,
                    CredentialServiceException,
                    CredentialOfferException,
                    SigningException,
                    NoSuchAlgorithmException,
                    URISyntaxException {
        when(mockDynamoDbService.getCredentialOffer(anyString()))
                .thenReturn(mockCredentialOfferCacheItem);
        when(mockHttpClient.target(any(URI.class))).thenReturn(mockWebTarget);
        when(mockWebTarget.request(MediaType.APPLICATION_JSON)).thenReturn(mockInvocationBuilder);
        when(mockInvocationBuilder.get()).thenReturn(mockResponse);
        when(mockResponse.getStatus()).thenReturn(200);
        when(mockResponse.readEntity(Document.class))
                .thenReturn(getMockSocialSecurityDocument(DOCUMENT_ID, null));
        when(mockCredentialBuilder.buildCredential(any(), any(), any()))
                .thenReturn(mockCredentialJwt);

        credentialService.getCredential(mockAccessToken, mockProofJwt);

        verify((CredentialBuilder<SocialSecurityCredentialSubject>) mockCredentialBuilder, times(1))
                .buildCredential(
                        any(SocialSecurityCredentialSubject.class),
                        eq(CredentialType.SOCIAL_SECURITY_CREDENTIAL),
                        eq(null));
    }

    @Test
    void Should_BuildBasicCheckCredentialSubject()
            throws AccessTokenValidationException,
                    ProofJwtValidationException,
                    DataStoreException,
                    CredentialServiceException,
                    CredentialOfferException,
                    SigningException,
                    NoSuchAlgorithmException,
                    URISyntaxException {
        when(mockDynamoDbService.getCredentialOffer(anyString()))
                .thenReturn(mockCredentialOfferCacheItem);
        when(mockHttpClient.target(any(URI.class))).thenReturn(mockWebTarget);
        when(mockWebTarget.request(MediaType.APPLICATION_JSON)).thenReturn(mockInvocationBuilder);
        when(mockInvocationBuilder.get()).thenReturn(mockResponse);
        when(mockResponse.getStatus()).thenReturn(200);
        when(mockResponse.readEntity(Document.class))
                .thenReturn(getMockBasicCheckDocument(DOCUMENT_ID));
        when(mockCredentialBuilder.buildCredential(any(), any(), anyString()))
                .thenReturn(mockCredentialJwt);

        credentialService.getCredential(mockAccessToken, mockProofJwt);

        verify((CredentialBuilder<BasicCheckCredentialSubject>) mockCredentialBuilder, times(1))
                .buildCredential(
                        any(BasicCheckCredentialSubject.class),
                        eq(CredentialType.BASIC_CHECK_CREDENTIAL),
                        eq("2025-07-11"));
    }

    @Test
    void Should_BuildVeteranCardCredentialSubject()
            throws AccessTokenValidationException,
                    ProofJwtValidationException,
                    DataStoreException,
                    CredentialServiceException,
                    CredentialOfferException,
                    SigningException,
                    NoSuchAlgorithmException,
                    URISyntaxException {
        when(mockDynamoDbService.getCredentialOffer(anyString()))
                .thenReturn(mockCredentialOfferCacheItem);
        when(mockHttpClient.target(any(URI.class))).thenReturn(mockWebTarget);
        when(mockWebTarget.request(MediaType.APPLICATION_JSON)).thenReturn(mockInvocationBuilder);
        when(mockInvocationBuilder.get()).thenReturn(mockResponse);
        when(mockResponse.getStatus()).thenReturn(200);
        when(mockResponse.readEntity(Document.class))
                .thenReturn(getMockVeteranCardDocument(DOCUMENT_ID));
        when(mockCredentialBuilder.buildCredential(any(), any(), anyString()))
                .thenReturn(mockCredentialJwt);

        credentialService.getCredential(mockAccessToken, mockProofJwt);

        verify((CredentialBuilder<VeteranCardCredentialSubject>) mockCredentialBuilder, times(1))
                .buildCredential(
                        any(VeteranCardCredentialSubject.class),
                        eq(CredentialType.DIGITAL_VETERAN_CARD),
                        eq("2000-07-11"));
    }

    @Test
    void Should_ThrowCredentialServiceException_When_DocumentVcTypeIsUnknown()
            throws DataStoreException {
        when(mockDynamoDbService.getCredentialOffer(anyString()))
                .thenReturn(mockCredentialOfferCacheItem);
        when(mockHttpClient.target(any(URI.class))).thenReturn(mockWebTarget);
        when(mockWebTarget.request(MediaType.APPLICATION_JSON)).thenReturn(mockInvocationBuilder);
        when(mockInvocationBuilder.get()).thenReturn(mockResponse);
        when(mockResponse.getStatus()).thenReturn(200);
        when(mockResponse.readEntity(Document.class))
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
                    DataStoreException,
                    CredentialServiceException,
                    CredentialOfferException,
                    SigningException,
                    NoSuchAlgorithmException,
                    URISyntaxException {
        when(mockDynamoDbService.getCredentialOffer(anyString()))
                .thenReturn(mockCredentialOfferCacheItem);
        when(mockHttpClient.target(any(URI.class))).thenReturn(mockWebTarget);
        when(mockWebTarget.request(MediaType.APPLICATION_JSON)).thenReturn(mockInvocationBuilder);
        when(mockInvocationBuilder.get()).thenReturn(mockResponse);
        when(mockResponse.getStatus()).thenReturn(200);
        when(mockResponse.readEntity(Document.class))
                .thenReturn(getMockSocialSecurityDocument(DOCUMENT_ID, null));
        when(mockCredentialBuilder.buildCredential(any(), any(), any()))
                .thenReturn(mockCredentialJwt);

        CredentialResponse credentialServiceReturnValue =
                credentialService.getCredential(mockAccessToken, mockProofJwt);

        assertEquals(mockCredentialJwt.serialize(), credentialServiceReturnValue.getCredential());
        assertEquals(NOTIFICATION_ID, credentialServiceReturnValue.getNotificationId());

        verify(mockAccessTokenService).verifyAccessToken(mockAccessToken);
        verify(mockProofJwtService).verifyProofJwt(mockProofJwt);
        verify(mockDynamoDbService, times(1)).getCredentialOffer(CREDENTIAL_IDENTIFIER);
        verify(mockDynamoDbService, times(1)).updateCredentialOffer(mockCredentialOfferCacheItem);
        verify((CredentialBuilder<SocialSecurityCredentialSubject>) mockCredentialBuilder, times(1))
                .buildCredential(
                        any(SocialSecurityCredentialSubject.class),
                        eq(CredentialType.SOCIAL_SECURITY_CREDENTIAL),
                        eq(null));
    }

    private CredentialOfferCacheItem getMockCredentialOfferCacheItem(
            String walletSubjectId, Boolean redeemed, String expiresInSeconds) {
        Long expiry = Instant.now().plusSeconds(Long.parseLong(expiresInSeconds)).getEpochSecond();
        Long ttl = Instant.now().plusSeconds(1000).getEpochSecond();
        return new CredentialOfferCacheItem(
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
