package uk.gov.di.mobile.wallet.cri.credential;

import com.nimbusds.jose.JOSEException;
import com.nimbusds.jose.JWSAlgorithm;
import com.nimbusds.jose.JWSHeader;
import com.nimbusds.jose.crypto.ECDSASigner;
import com.nimbusds.jose.jwk.ECKey;
import com.nimbusds.jwt.JWTClaimsSet;
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
import uk.gov.di.mobile.wallet.cri.models.CredentialOfferCacheItem;
import uk.gov.di.mobile.wallet.cri.services.ConfigurationService;
import uk.gov.di.mobile.wallet.cri.services.data_storage.DataStoreException;
import uk.gov.di.mobile.wallet.cri.services.data_storage.DynamoDbService;
import uk.gov.di.mobile.wallet.cri.services.signing.SigningException;

import java.net.URI;
import java.net.URISyntaxException;
import java.security.NoSuchAlgorithmException;
import java.time.Instant;
import java.util.Arrays;
import java.util.Base64;
import java.util.Date;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.containsString;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class CredentialServiceTest {

    @Mock private Client mockHttpClient;
    @Mock private WebTarget mockWebTarget;
    @Mock private Invocation.Builder mockInvocationBuilder;
    @Mock private Response mockResponse;
    private CredentialService credentialService;
    private CredentialOfferCacheItem credentialOfferCacheItem;
    private final DynamoDbService dynamoDbService = mock(DynamoDbService.class);
    private final AccessTokenService accessTokenService = mock(AccessTokenService.class);
    private final ProofJwtService proofJwtService = mock(ProofJwtService.class);
    private final CredentialBuilder credentialBuilder = mock(CredentialBuilder.class);
    private final ConfigurationService configurationService = new ConfigurationService();

    @BeforeEach
    void setUp() {
        credentialService =
                new CredentialService(
                        configurationService,
                        dynamoDbService,
                        accessTokenService,
                        proofJwtService,
                        mockHttpClient,
                        credentialBuilder);
        credentialOfferCacheItem =
                new CredentialOfferCacheItem(
                        "test-credential-identifier", "test-document-id", "test-wallet-sub", 900L);
    }

    @Test
    void shouldThrowAccessTokenValidationExceptionWhenAccessTokenCredentialIdentifiersIsEmpty()
            throws java.text.ParseException, JOSEException {
        ECDSASigner ecSigner = new ECDSASigner(getEsPrivateKey());
        SignedJWT accessToken = getTestAccessToken("test-nonce");
        SignedJWT proofJwt = getTestProofJwt("test-nonce");
        accessToken.sign(ecSigner);
        proofJwt.sign(ecSigner);

        AccessTokenValidationException exception =
                assertThrows(
                        AccessTokenValidationException.class,
                        () -> credentialService.getCredential(accessToken, proofJwt));
        assertThat(
                exception.getMessage(),
                containsString(
                        "Error parsing access token custom claims: credential_identifiers is invalid"));
    }

    @Test
    void shouldThrowProofJwtValidationExceptionWhenNoncesDoNotMatch()
            throws java.text.ParseException, JOSEException {
        ECDSASigner ecSigner = new ECDSASigner(getEsPrivateKey());
        SignedJWT accessToken =
                getTestAccessToken("test-nonce-1", "test-credential-identifier", "test-wallet-sub");
        SignedJWT proofJwt = getTestProofJwt("test-nonce-2");
        accessToken.sign(ecSigner);
        proofJwt.sign(ecSigner);

        ProofJwtValidationException exception =
                assertThrows(
                        ProofJwtValidationException.class,
                        () -> credentialService.getCredential(accessToken, proofJwt));
        assertEquals(
                "Access token c_nonce claim does not match Proof JWT nonce claim",
                exception.getMessage());
    }

    @Test
    void shouldThrowAccessTokenValidationExceptionWhenCredentialOfferNotInDataStore()
            throws java.text.ParseException, DataStoreException, JOSEException {
        ECDSASigner ecSigner = new ECDSASigner(getEsPrivateKey());
        SignedJWT accessToken =
                getTestAccessToken("test-nonce", "test-credential-identifier", "test-wallet-sub");
        SignedJWT proofJwt = getTestProofJwt("test-nonce");
        accessToken.sign(ecSigner);
        proofJwt.sign(ecSigner);
        when(dynamoDbService.getCredentialOffer(anyString())).thenReturn(null);

        AccessTokenValidationException exception =
                assertThrows(
                        AccessTokenValidationException.class,
                        () -> credentialService.getCredential(accessToken, proofJwt));
        assertEquals(
                "Null response returned when fetching credential offer", exception.getMessage());
    }

    @Test
    void shouldThrowDataStoreExceptionWhenCallToDatabaseThrowsAnError()
            throws java.text.ParseException, DataStoreException, JOSEException {
        ECDSASigner ecSigner = new ECDSASigner(getEsPrivateKey());
        SignedJWT accessToken =
                getTestAccessToken("test-nonce", "test-credential-identifier", "test-wallet-sub");
        SignedJWT proofJwt = getTestProofJwt("test-nonce");
        accessToken.sign(ecSigner);
        proofJwt.sign(ecSigner);
        when(dynamoDbService.getCredentialOffer(anyString()))
                .thenThrow(new DataStoreException("Some database error"));

        DataStoreException exception =
                assertThrows(
                        DataStoreException.class,
                        () -> credentialService.getCredential(accessToken, proofJwt));
        assertEquals("Some database error", exception.getMessage());
    }

    @Test
    void shouldThrowAccessTokenValidationExceptionWhenWalletSubjectIdsDoNotMatch()
            throws java.text.ParseException, DataStoreException, JOSEException {
        ECDSASigner ecSigner = new ECDSASigner(getEsPrivateKey());
        SignedJWT accessToken =
                getTestAccessToken(
                        "test-nonce", "test-credential-identifier", "different-test-wallet-sub");
        SignedJWT proofJwt = getTestProofJwt("test-nonce");
        accessToken.sign(ecSigner);
        proofJwt.sign(ecSigner);
        when(dynamoDbService.getCredentialOffer(anyString())).thenReturn(credentialOfferCacheItem);

        AccessTokenValidationException exception =
                assertThrows(
                        AccessTokenValidationException.class,
                        () -> credentialService.getCredential(accessToken, proofJwt));
        assertThat(
                exception.getMessage(),
                containsString("Access token sub claim does not match cached walletSubjectId"));
    }

    @Test
    void shouldThrowRuntimeExceptionWhenDocumentDetailsCannotBeFetched()
            throws java.text.ParseException,
                    DataStoreException,
                    SigningException,
                    NoSuchAlgorithmException,
                    JOSEException {
        ECDSASigner ecSigner = new ECDSASigner(getEsPrivateKey());
        SignedJWT accessToken =
                getTestAccessToken("test-nonce", "test-credential-identifier", "test-wallet-sub");
        SignedJWT proofJwt = getTestProofJwt("test-nonce");
        accessToken.sign(ecSigner);
        proofJwt.sign(ecSigner);
        when(dynamoDbService.getCredentialOffer(anyString())).thenReturn(credentialOfferCacheItem);
        when(mockHttpClient.target(any(URI.class))).thenReturn(mockWebTarget);
        when(mockWebTarget.request(MediaType.APPLICATION_JSON)).thenReturn(mockInvocationBuilder);
        when(mockInvocationBuilder.get()).thenReturn(mockResponse);
        when(mockResponse.getStatus()).thenReturn(500);
        SignedJWT testCredentialJwt =
                SignedJWT.parse(
                        "eyJhbGciOiJIUzI1NiIsInR5cCI6IkpXVCJ9.eyJzdWIiOiIxMjM0NTY3ODkwIiwibmFtZSI6IkpvaG4gRG9lIiwiaWF0IjoxNTE2MjM5MDIyfQ.SflKxwRJSMeKKF2QT4fwpMeJf36POk6yJV_adQssw5c");
        Credential testCredentialObject = new Credential(testCredentialJwt);
        when(credentialBuilder.buildCredential("test-did-key", new Object()))
                .thenReturn(testCredentialObject);

        CredentialServiceException exception =
                assertThrows(
                        CredentialServiceException.class,
                        () -> credentialService.getCredential(accessToken, proofJwt));
        assertThat(
                exception.getMessage(),
                containsString(
                        "Request to fetch document details for documentId test-document-id failed with status code 500"));
    }

    @Test
    void shouldReturnCredential()
            throws AccessTokenValidationException,
                    java.text.ParseException,
                    ProofJwtValidationException,
                    DataStoreException,
                    SigningException,
                    NoSuchAlgorithmException,
                    JOSEException,
                    URISyntaxException,
                    CredentialServiceException {
        ECDSASigner ecSigner = new ECDSASigner(getEsPrivateKey());
        SignedJWT accessToken =
                getTestAccessToken("test-nonce", "test-credential-identifier", "test-wallet-sub");
        SignedJWT proofJwt = getTestProofJwt("test-nonce");
        accessToken.sign(ecSigner);
        proofJwt.sign(ecSigner);
        when(dynamoDbService.getCredentialOffer(anyString())).thenReturn(credentialOfferCacheItem);
        when(mockHttpClient.target(any(URI.class))).thenReturn(mockWebTarget);
        when(mockWebTarget.request(MediaType.APPLICATION_JSON)).thenReturn(mockInvocationBuilder);
        when(mockInvocationBuilder.get()).thenReturn(mockResponse);
        when(mockResponse.getStatus()).thenReturn(200);
        Object testDocumentDetails = new Object();
        when(mockResponse.readEntity(Object.class)).thenReturn(testDocumentDetails);
        SignedJWT testCredentialJwt =
                SignedJWT.parse(
                        "eyJhbGciOiJIUzI1NiIsInR5cCI6IkpXVCJ9.eyJzdWIiOiIxMjM0NTY3ODkwIiwibmFtZSI6IkpvaG4gRG9lIiwiaWF0IjoxNTE2MjM5MDIyfQ.SflKxwRJSMeKKF2QT4fwpMeJf36POk6yJV_adQssw5c");
        Credential testCredentialObject = new Credential(testCredentialJwt);
        when(credentialBuilder.buildCredential(any(), any())).thenReturn(testCredentialObject);
        Credential credentialServiceReturnValue =
                credentialService.getCredential(accessToken, proofJwt);

        assertEquals(
                testCredentialObject.getCredential(), credentialServiceReturnValue.getCredential());
        verify(credentialBuilder)
                .buildCredential(
                        "did:key:MFkwEwYHKoZIzj0CAQYIKoZIzj0DAQcDQgAEaUItVYrAvVK+1efrBvWDXtmapkl1PHqXUHytuK5/F7lfIXprXHD9zIdAinRrWSFeh28OJJzoSH1zqzOJ+ZhFOA==",
                        testDocumentDetails);
        verify(accessTokenService).verifyAccessToken(accessToken);
        verify(proofJwtService).verifyProofJwt(proofJwt);
    }

    private static SignedJWT getTestProofJwt(String nonce) {
        return new SignedJWT(
                new JWSHeader.Builder(JWSAlgorithm.ES256)
                        .keyID(
                                "did:key:MFkwEwYHKoZIzj0CAQYIKoZIzj0DAQcDQgAEaUItVYrAvVK+1efrBvWDXtmapkl1PHqXUHytuK5/F7lfIXprXHD9zIdAinRrWSFeh28OJJzoSH1zqzOJ+ZhFOA==")
                        .build(),
                new JWTClaimsSet.Builder()
                        .issueTime(Date.from(Instant.now()))
                        .issuer("urn:fdc:gov:uk:wallet")
                        .audience("urn:fdc:gov:uk:example-credential-issuer")
                        .claim("nonce", nonce)
                        .build());
    }

    private static SignedJWT getTestAccessToken(
            String nonce, String credentialIdentifier, String subject) {
        return new SignedJWT(
                new JWSHeader.Builder(JWSAlgorithm.ES256)
                        .keyID("cb5a1a8b-809a-4f32-944d-caae1a57ed91")
                        .build(),
                new JWTClaimsSet.Builder()
                        .issueTime(Date.from(Instant.now()))
                        .issuer("urn:fdc:gov:uk:wallet")
                        .audience("urn:fdc:gov:uk:example-credential-issuer")
                        .subject(subject)
                        .claim("c_nonce", nonce)
                        .claim("credential_identifiers", Arrays.asList(credentialIdentifier))
                        .build());
    }

    private static SignedJWT getTestAccessToken(String nonce) {
        return new SignedJWT(
                new JWSHeader.Builder(JWSAlgorithm.ES256)
                        .keyID("cb5a1a8b-809a-4f32-944d-caae1a57ed91")
                        .build(),
                new JWTClaimsSet.Builder()
                        .issueTime(Date.from(Instant.now()))
                        .issuer("urn:fdc:gov:uk:wallet")
                        .audience("urn:fdc:gov:uk:example-credential-issuer")
                        .subject("test-sub")
                        .claim("c_nonce", nonce)
                        .claim("credential_identifiers", Arrays.asList())
                        .build());
    }

    private static ECKey getEsPrivateKey() throws java.text.ParseException {
        String privateKeyJwkBase64 =
                "eyJrdHkiOiJFQyIsImQiOiI4SGJYN0xib1E1OEpJOGo3eHdfQXp0SlRVSDVpZTFtNktIQlVmX3JnakVrIiwidXNlIjoic2lnIiwiY3J2IjoiUC0yNTYiLCJraWQiOiJmMDYxMWY3Zi04YTI5LTQ3ZTEtYmVhYy1mNWVlNWJhNzQ3MmUiLCJ4IjoiSlpKeE83b2JSOElzdjU4NUVzaWcwYlAwQUdfb1N6MDhSMS11VXBiYl9JRSIsInkiOiJtNjBRMmtMMExiaEhTbHRjS1lyTG8wczE1M1hveF9tVDV2UlV6Z3g4TWtFIiwiaWF0IjoxNzEyMTQ2MTc5fQ==";
        return ECKey.parse(new String(Base64.getDecoder().decode(privateKeyJwkBase64)));
    }
}
