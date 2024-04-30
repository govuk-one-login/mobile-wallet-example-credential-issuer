package uk.gov.di.mobile.wallet.cri.credential;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.nimbusds.jwt.SignedJWT;
import com.nimbusds.oauth2.sdk.ParseException;
import com.nimbusds.oauth2.sdk.token.BearerAccessToken;
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
import java.security.NoSuchAlgorithmException;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.*;
import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
public class CredentialServiceTest {
    @Mock private Client mockHttpClient;
    @Mock private WebTarget mockWebTarget;
    @Mock private Invocation.Builder mockInvocationBuilder;
    @Mock private Response mockResponse;
    private CredentialService credentialService;
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
    }

    @Test
    void shouldThrowRuntimeExceptionWhenAccessTokenCredentialIdentifiersIsNotAnArray()
            throws ParseException,
                    JsonProcessingException,
                    AccessTokenValidationException,
                    java.text.ParseException {
        BearerAccessToken bearerAccessToken =
                BearerAccessToken.parse(
                        "Bearer eyJhbGciOiJIUzI1NiIsInR5cCI6IkpXVCJ9.eyJjcmVkZW50aWFsX2lkZW50aWZpZXJzIjoiY3JlZGVudGlhbF9pZGVudGlmaWVyIiwic3ViIjoiMTIzNDU2Nzg5MCIsImNfbm9uY2UiOiJjX25vbmNlIn0.pXLW_iQ7GuCW0bCQB-V1kysFpvTB0AyvmHF8riTVfMk");
        JsonNode payload =
                new ObjectMapper()
                        .readTree(
                                "{\"proof\":{\"proof_type\":\"jwt\",\"jwt\": \"eyJhbGciOiJIUzI1NiIsInR5cCI6IkpXVCIsImtpZCI6InRlc3Qta2lkIn0.eyJub25jZSI6Im5vbmNlIn0.K5FhFCgxYEpRkga4tCRSbFaCiLfm-RQozcTHLRMYwbg\"}}");
        CredentialRequestBody credentialRequestBody = CredentialRequestBody.from(payload);
        when(accessTokenService.verifyAccessToken(bearerAccessToken))
                .thenReturn(SignedJWT.parse(bearerAccessToken.getValue()));

        RuntimeException exception =
                assertThrows(
                        RuntimeException.class,
                        () -> credentialService.run(bearerAccessToken, credentialRequestBody));
        assertThat(
                exception.getMessage(), containsString("Error parsing access token custom claims"));
    }

    @Test
    void shouldThrowClaimMismatchExceptionWhenNoncesDoNotMatch()
            throws ParseException,
                    JsonProcessingException,
                    AccessTokenValidationException,
                    java.text.ParseException,
                    ProofJwtValidationException {
        BearerAccessToken bearerAccessToken =
                BearerAccessToken.parse(
                        "Bearer eyJhbGciOiJIUzI1NiIsInR5cCI6IkpXVCJ9.eyJjcmVkZW50aWFsX2lkZW50aWZpZXJzIjpbImNyZWRlbnRpYWxfaWRlbnRpZmllciJdLCJzdWIiOiIxMjM0NTY3ODkwIiwiY19ub25jZSI6ImNfbm9uY2UifQ.zjj4jBfXidXAI_cUGb4srf5Hk1M1XceSGck4aH-iXKc");
        JsonNode payload =
                new ObjectMapper()
                        .readTree(
                                "{\"proof\":{\"proof_type\":\"jwt\",\"jwt\": \"eyJhbGciOiJIUzI1NiIsInR5cCI6IkpXVCIsImtpZCI6InRlc3Qta2lkIn0.eyJub25jZSI6Im5vbmNlIn0.K5FhFCgxYEpRkga4tCRSbFaCiLfm-RQozcTHLRMYwbg\"}}");
        CredentialRequestBody credentialRequestBody = CredentialRequestBody.from(payload);
        when(accessTokenService.verifyAccessToken(bearerAccessToken))
                .thenReturn(SignedJWT.parse(bearerAccessToken.getValue()));
        String proofJwtString = credentialRequestBody.getProof().getJwt();
        when(proofJwtService.verifyProofJwt(proofJwtString))
                .thenReturn(SignedJWT.parse(proofJwtString));

        ClaimMismatchException exception =
                assertThrows(
                        ClaimMismatchException.class,
                        () -> credentialService.run(bearerAccessToken, credentialRequestBody));
        assertEquals(
                "Access token c_nonce claim does not match Proof JWT nonce claim",
                exception.getMessage());
    }

    @Test
    void shouldThrowDataStoreExceptionWhenCredentialOfferNotInDataStore()
            throws ParseException,
                    JsonProcessingException,
                    AccessTokenValidationException,
                    java.text.ParseException,
                    ProofJwtValidationException,
                    DataStoreException {
        BearerAccessToken bearerAccessToken =
                BearerAccessToken.parse(
                        "Bearer eyJhbGciOiJIUzI1NiIsInR5cCI6IkpXVCJ9.eyJjcmVkZW50aWFsX2lkZW50aWZpZXJzIjpbImNyZWRlbnRpYWxfaWRlbnRpZmllciJdLCJzdWIiOiIxMjM0NTY3ODkwIiwiY19ub25jZSI6IjEyMzQ1In0.bFS0x1PwS4LwpA_14W7sBrAjEz4F9ChPh82bkYl_zkM");
        JsonNode payload =
                new ObjectMapper()
                        .readTree(
                                "{\"proof\":{\"proof_type\":\"jwt\",\"jwt\": \"eyJhbGciOiJIUzI1NiIsInR5cCI6IkpXVCIsImtpZCI6InRlc3Qta2lkIn0.eyJub25jZSI6IjEyMzQ1In0.jatpaC088-drgyiqshU3BsRSp38i_5xDczT1_GyVOdI\"}}");
        CredentialRequestBody credentialRequestBody = CredentialRequestBody.from(payload);
        when(accessTokenService.verifyAccessToken(bearerAccessToken))
                .thenReturn(SignedJWT.parse(bearerAccessToken.getValue()));
        String proofJwtString = credentialRequestBody.getProof().getJwt();
        when(proofJwtService.verifyProofJwt(proofJwtString))
                .thenReturn(SignedJWT.parse(proofJwtString));
        when(dynamoDbService.getCredentialOffer(anyString())).thenReturn(null);

        DataStoreException exception =
                assertThrows(
                        DataStoreException.class,
                        () -> credentialService.run(bearerAccessToken, credentialRequestBody));
        assertEquals(
                "Null response returned when fetching credential offer", exception.getMessage());
    }

    @Test
    void shouldThrowDataStoreExceptionWhenWalletSubjectIdsDoNotMatch()
            throws ParseException,
                    JsonProcessingException,
                    AccessTokenValidationException,
                    java.text.ParseException,
                    ProofJwtValidationException,
                    DataStoreException {
        BearerAccessToken bearerAccessToken =
                BearerAccessToken.parse(
                        "Bearer eyJhbGciOiJIUzI1NiIsInR5cCI6IkpXVCJ9.eyJjcmVkZW50aWFsX2lkZW50aWZpZXJzIjpbImNyZWRlbnRpYWxfaWRlbnRpZmllciJdLCJzdWIiOiIxMjM0NTY3ODkwIiwiY19ub25jZSI6IjEyMzQ1In0.bFS0x1PwS4LwpA_14W7sBrAjEz4F9ChPh82bkYl_zkM");
        JsonNode payload =
                new ObjectMapper()
                        .readTree(
                                "{\"proof\":{\"proof_type\":\"jwt\",\"jwt\": \"eyJhbGciOiJIUzI1NiIsInR5cCI6IkpXVCIsImtpZCI6InRlc3Qta2lkIn0.eyJub25jZSI6IjEyMzQ1In0.jatpaC088-drgyiqshU3BsRSp38i_5xDczT1_GyVOdI\"}}");
        CredentialRequestBody credentialRequestBody = CredentialRequestBody.from(payload);
        when(accessTokenService.verifyAccessToken(bearerAccessToken))
                .thenReturn(SignedJWT.parse(bearerAccessToken.getValue()));
        String proofJwtString = credentialRequestBody.getProof().getJwt();
        when(proofJwtService.verifyProofJwt(proofJwtString))
                .thenReturn(SignedJWT.parse(proofJwtString));
        CredentialOfferCacheItem credentialOfferCacheItem =
                new CredentialOfferCacheItem(
                        "test-credential-identifier", "test-document-id", "test-wallet-subject-id");
        when(dynamoDbService.getCredentialOffer(anyString())).thenReturn(credentialOfferCacheItem);

        ClaimMismatchException exception =
                assertThrows(
                        ClaimMismatchException.class,
                        () -> credentialService.run(bearerAccessToken, credentialRequestBody));
        assertThat(
                exception.getMessage(),
                containsString("Access token sub claim does not match cached walletSubjectId"));
    }

    @Test
    void shouldThrowRuntimeExceptionWhenDocumentDetailsCannotBeFetched()
            throws ParseException,
                    JsonProcessingException,
                    AccessTokenValidationException,
                    java.text.ParseException,
                    ProofJwtValidationException,
                    DataStoreException,
                    SigningException,
                    NoSuchAlgorithmException {
        BearerAccessToken bearerAccessToken =
                BearerAccessToken.parse(
                        "Bearer eyJhbGciOiJIUzI1NiIsInR5cCI6IkpXVCJ9.eyJjcmVkZW50aWFsX2lkZW50aWZpZXJzIjpbImNyZWRlbnRpYWxfaWRlbnRpZmllciJdLCJzdWIiOiJ0ZXN0LXdhbGxldC1zdWJqZWN0LWlkIiwiY19ub25jZSI6IjEyMzQ1In0.gXgeBUJ2d7gT2gzv-lkKXIcWcBmwxfwdivNT0p5J_Xc");
        JsonNode payload =
                new ObjectMapper()
                        .readTree(
                                "{\"proof\":{\"proof_type\":\"jwt\",\"jwt\": \"eyJhbGciOiJIUzI1NiIsInR5cCI6IkpXVCIsImtpZCI6InRlc3Qta2lkIn0.eyJub25jZSI6IjEyMzQ1In0.jatpaC088-drgyiqshU3BsRSp38i_5xDczT1_GyVOdI\"}}");
        CredentialRequestBody credentialRequestBody = CredentialRequestBody.from(payload);
        when(accessTokenService.verifyAccessToken(bearerAccessToken))
                .thenReturn(SignedJWT.parse(bearerAccessToken.getValue()));
        String proofJwtString = credentialRequestBody.getProof().getJwt();
        when(proofJwtService.verifyProofJwt(proofJwtString))
                .thenReturn(SignedJWT.parse(proofJwtString));
        CredentialOfferCacheItem credentialOfferCacheItem =
                new CredentialOfferCacheItem(
                        "test-credential-identifier", "test-document-id", "test-wallet-subject-id");
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

        RuntimeException exception =
                assertThrows(
                        RuntimeException.class,
                        () -> credentialService.run(bearerAccessToken, credentialRequestBody));
        assertThat(
                exception.getMessage(),
                containsString(
                        "Request to fetch document details for documentId test-document-id failed with status code 500"));
    }

    @Test
    void shouldReturnCredential()
            throws ParseException,
                    JsonProcessingException,
                    AccessTokenValidationException,
                    java.text.ParseException,
                    ProofJwtValidationException,
                    DataStoreException,
                    SigningException,
                    ClaimMismatchException,
                    NoSuchAlgorithmException {
        BearerAccessToken bearerAccessToken =
                BearerAccessToken.parse(
                        "Bearer eyJhbGciOiJIUzI1NiIsInR5cCI6IkpXVCJ9.eyJjcmVkZW50aWFsX2lkZW50aWZpZXJzIjpbImNyZWRlbnRpYWxfaWRlbnRpZmllciJdLCJzdWIiOiJ0ZXN0LXdhbGxldC1zdWJqZWN0LWlkIiwiY19ub25jZSI6IjEyMzQ1In0.gXgeBUJ2d7gT2gzv-lkKXIcWcBmwxfwdivNT0p5J_Xc");
        JsonNode payload =
                new ObjectMapper()
                        .readTree(
                                "{\"proof\":{\"proof_type\":\"jwt\",\"jwt\": \"eyJhbGciOiJIUzI1NiIsInR5cCI6IkpXVCIsImtpZCI6InRlc3Qta2lkIn0.eyJub25jZSI6IjEyMzQ1In0.jatpaC088-drgyiqshU3BsRSp38i_5xDczT1_GyVOdI\"}}");
        CredentialRequestBody credentialRequestBody = CredentialRequestBody.from(payload);
        when(accessTokenService.verifyAccessToken(bearerAccessToken))
                .thenReturn(SignedJWT.parse(bearerAccessToken.getValue()));
        String proofJwtString = credentialRequestBody.getProof().getJwt();
        when(proofJwtService.verifyProofJwt(proofJwtString))
                .thenReturn(SignedJWT.parse(proofJwtString));
        CredentialOfferCacheItem credentialOfferCacheItem =
                new CredentialOfferCacheItem(
                        "test-credential-identifier", "test-document-id", "test-wallet-subject-id");
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
                credentialService.run(bearerAccessToken, credentialRequestBody);

        assertEquals(
                testCredentialObject.getCredential(), credentialServiceReturnValue.getCredential());
        verify(credentialBuilder).buildCredential("test-kid", testDocumentDetails);
    }
}
