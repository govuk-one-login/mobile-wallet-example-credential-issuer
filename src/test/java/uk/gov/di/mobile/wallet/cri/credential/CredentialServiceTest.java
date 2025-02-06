package uk.gov.di.mobile.wallet.cri.credential;

import com.fasterxml.jackson.core.JsonProcessingException;
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
import org.jetbrains.annotations.NotNull;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import uk.gov.di.mobile.wallet.cri.credential.basicDiscloureCredential.BasicCheckCredentialSubject;
import uk.gov.di.mobile.wallet.cri.credential.digitalVeteranCard.VeteranCardCredentialSubject;
import uk.gov.di.mobile.wallet.cri.credential.socialSecurityCredential.SocialSecurityCredentialSubject;
import uk.gov.di.mobile.wallet.cri.models.CredentialOfferCacheItem;
import uk.gov.di.mobile.wallet.cri.services.ConfigurationService;
import uk.gov.di.mobile.wallet.cri.services.data_storage.DataStoreException;
import uk.gov.di.mobile.wallet.cri.services.data_storage.DynamoDbService;
import uk.gov.di.mobile.wallet.cri.services.signing.SigningException;

import java.net.URI;
import java.net.URISyntaxException;
import java.security.NoSuchAlgorithmException;
import java.text.ParseException;
import java.time.Instant;
import java.util.*;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.containsString;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.*;

@SuppressWarnings("unchecked")
@ExtendWith(MockitoExtension.class)
class CredentialServiceTest {

    @Mock private Client mockHttpClient;
    @Mock private WebTarget mockWebTarget;
    @Mock private Invocation.Builder mockInvocationBuilder;
    @Mock private Response mockResponse;
    @Mock private CredentialBuilder<?> credentialBuilder;

    private final DynamoDbService dynamoDbService = mock(DynamoDbService.class);
    private final AccessTokenService accessTokenService = mock(AccessTokenService.class);
    private final ProofJwtService proofJwtService = mock(ProofJwtService.class);
    private final ConfigurationService configurationService = mock(ConfigurationService.class);

    private CredentialService credentialService;
    private CredentialOfferCacheItem credentialOfferCacheItem;
    private SignedJWT proofJwt;
    private SignedJWT accessToken;

    private static final String DOCUMENT_ID = "de9cbf02-2fbc-4d61-a627-f97851f6840b";
    private static final String CREDENTIAL_IDENTIFIER = "efb52887-48d6-43b7-b14c-da7896fbf54d";
    private static final String NONCE = "134e0c41-a8b4-46d4-aec8-cd547e125589";
    private static final String WALLET_SUBJECT_ID =
            "urn:fdc:wallet.account.gov.uk:2024:DtPT8x-dp_73tnlY3KNTiCitziN9GEherD16bqxNt9i";

    @BeforeEach
    void setUp() throws ParseException, JOSEException {

        credentialService =
                new CredentialService(
                        configurationService,
                        dynamoDbService,
                        accessTokenService,
                        proofJwtService,
                        mockHttpClient,
                        credentialBuilder);
        long timeToLive =
                Instant.now().plusSeconds(Long.parseLong("900")).getEpochSecond(); // 15 minutes
        credentialOfferCacheItem =
                new CredentialOfferCacheItem(
                        CREDENTIAL_IDENTIFIER, DOCUMENT_ID, WALLET_SUBJECT_ID, timeToLive);
        proofJwt = getTestProofJwt(NONCE);
        accessToken = getTestAccessToken(NONCE, WALLET_SUBJECT_ID);
    }

    @Test
    void Should_Throw_AccessTokenValidationException_When_Credential_Identifiers_Claim_Is_Empty()
            throws JOSEException, DataStoreException, ParseException {
        SignedJWT accessTokenWithEmptyCredentialIdentifiers = getTestAccessToken();

        AccessTokenValidationException exception =
                assertThrows(
                        AccessTokenValidationException.class,
                        () ->
                                credentialService.getCredential(
                                        accessTokenWithEmptyCredentialIdentifiers, proofJwt));

        assertThat(
                exception.getMessage(),
                containsString(
                        "Error parsing access token custom claims: Empty credential_identifiers"));
        verify(dynamoDbService, times(0)).getCredentialOffer(any());
        verify(dynamoDbService, times(0)).deleteCredentialOffer(any());
    }

    @Test
    void Should_Throw_ProofJwtValidationException_When_Nonce_Values_Do_Not_Match()
            throws JOSEException, DataStoreException, ParseException {
        SignedJWT proofJwtWithDifferentNonce =
                getTestProofJwt("c5408ee2-9c5d-4be3-acda-06b95285489a");

        ProofJwtValidationException exception =
                assertThrows(
                        ProofJwtValidationException.class,
                        () ->
                                credentialService.getCredential(
                                        accessToken, proofJwtWithDifferentNonce));

        assertEquals(
                "Access token c_nonce claim does not match Proof JWT nonce claim",
                exception.getMessage());
        verify(dynamoDbService, times(0)).getCredentialOffer(any());
        verify(dynamoDbService, times(0)).deleteCredentialOffer(any());
    }

    @Test
    void Should_Throw_CredentialOfferNotFoundException_When_Credential_Offer_Not_Found()
            throws DataStoreException {
        when(dynamoDbService.getCredentialOffer(anyString())).thenReturn(null);

        CredentialOfferNotFoundException exception =
                assertThrows(
                        CredentialOfferNotFoundException.class,
                        () -> credentialService.getCredential(accessToken, proofJwt));

        assertEquals(
                "Credential offer not found for credentialOfferId efb52887-48d6-43b7-b14c-da7896fbf54d",
                exception.getMessage());
        verify(dynamoDbService, times(1)).getCredentialOffer(CREDENTIAL_IDENTIFIER);
        verify(dynamoDbService, times(0)).deleteCredentialOffer(any());
    }

    @Test
    void Should_Throw_DataStoreException_When_Call_To_Database_Throws_Error()
            throws DataStoreException {
        when(dynamoDbService.getCredentialOffer(anyString()))
                .thenThrow(new DataStoreException("Some database error"));

        DataStoreException exception =
                assertThrows(
                        DataStoreException.class,
                        () -> credentialService.getCredential(accessToken, proofJwt));

        assertEquals("Some database error", exception.getMessage());
        verify(dynamoDbService, times(1)).getCredentialOffer(CREDENTIAL_IDENTIFIER);
        verify(dynamoDbService, times(0)).deleteCredentialOffer(any());
    }

    @Test
    void Should_Throw_AccessTokenValidationException_When_Wallet_Subject_IDs_Do_Not_Match()
            throws java.text.ParseException, DataStoreException, JOSEException {
        SignedJWT accessToken = getTestAccessToken(NONCE, "954a4cff-0d38-4558-b29d-ee709f4f227e");
        when(dynamoDbService.getCredentialOffer(anyString())).thenReturn(credentialOfferCacheItem);

        AccessTokenValidationException exception =
                assertThrows(
                        AccessTokenValidationException.class,
                        () -> credentialService.getCredential(accessToken, proofJwt));

        assertThat(
                exception.getMessage(),
                containsString("Access token sub claim does not match cached walletSubjectId"));
        verify(dynamoDbService, times(1)).getCredentialOffer(CREDENTIAL_IDENTIFIER);
        verify(dynamoDbService, times(0)).deleteCredentialOffer(any());
    }

    @Test
    void Should_Throw_CredentialOfferNotFoundException_When_Credential_Offer_Is_Expired()
            throws DataStoreException {
        long timeToLive =
                Instant.now().minusSeconds(Long.parseLong("2")).getEpochSecond(); // 2 seconds ago
        CredentialOfferCacheItem expiredCredentialOfferCacheItem =
                new CredentialOfferCacheItem(
                        CREDENTIAL_IDENTIFIER, DOCUMENT_ID, WALLET_SUBJECT_ID, timeToLive);
        when(dynamoDbService.getCredentialOffer(anyString()))
                .thenReturn(expiredCredentialOfferCacheItem);

        CredentialOfferNotFoundException exception =
                assertThrows(
                        CredentialOfferNotFoundException.class,
                        () -> credentialService.getCredential(accessToken, proofJwt));

        assertThat(
                exception.getMessage(),
                containsString(
                        "Credential offer for credentialOfferId efb52887-48d6-43b7-b14c-da7896fbf54d expired at"));
    }

    @Test
    void Should_Throw_RuntimeException_When_Document_Endpoint_Returns_500()
            throws DataStoreException {
        when(dynamoDbService.getCredentialOffer(anyString())).thenReturn(credentialOfferCacheItem);
        when(mockHttpClient.target(any(URI.class))).thenReturn(mockWebTarget);
        when(mockWebTarget.request(MediaType.APPLICATION_JSON)).thenReturn(mockInvocationBuilder);
        when(mockInvocationBuilder.get()).thenReturn(mockResponse);
        when(mockResponse.getStatus()).thenReturn(500);

        CredentialServiceException exception =
                assertThrows(
                        CredentialServiceException.class,
                        () -> credentialService.getCredential(accessToken, proofJwt));

        assertThat(
                exception.getMessage(),
                containsString(
                        "Request to fetch document details for documentId de9cbf02-2fbc-4d61-a627-f97851f6840b failed with status code 500"));
        verify(dynamoDbService, times(1)).getCredentialOffer(CREDENTIAL_IDENTIFIER);
        verify(dynamoDbService, times(0)).deleteCredentialOffer(any());
    }

    @Test
    void Should_Call_Credential_Builder_With_SocialSecurityCredentialSubject()
            throws AccessTokenValidationException,
                    ProofJwtValidationException,
                    DataStoreException,
                    CredentialServiceException,
                    CredentialOfferNotFoundException,
                    SigningException,
                    NoSuchAlgorithmException,
                    URISyntaxException,
                    JsonProcessingException {
        when(dynamoDbService.getCredentialOffer(anyString())).thenReturn(credentialOfferCacheItem);
        when(mockHttpClient.target(any(URI.class))).thenReturn(mockWebTarget);
        when(mockWebTarget.request(MediaType.APPLICATION_JSON)).thenReturn(mockInvocationBuilder);
        when(mockInvocationBuilder.get()).thenReturn(mockResponse);
        when(mockResponse.getStatus()).thenReturn(200);
        when(mockResponse.readEntity(Document.class)).thenReturn(getTestSocialSecurityDocument());
        when(credentialBuilder.buildCredential(any(), anyString())).thenReturn(any());

        credentialService.getCredential(accessToken, proofJwt);

        verify((CredentialBuilder<SocialSecurityCredentialSubject>) credentialBuilder, times(1))
                .buildCredential(
                        any(SocialSecurityCredentialSubject.class), eq("SocialSecurityCredential"));
    }

    @Test
    void Should_Call_Credential_Builder_With_BasicCheckCredentialSubject()
            throws AccessTokenValidationException,
                    ProofJwtValidationException,
                    DataStoreException,
                    CredentialServiceException,
                    CredentialOfferNotFoundException,
                    SigningException,
                    NoSuchAlgorithmException,
                    URISyntaxException,
                    JsonProcessingException {
        when(dynamoDbService.getCredentialOffer(anyString())).thenReturn(credentialOfferCacheItem);
        when(mockHttpClient.target(any(URI.class))).thenReturn(mockWebTarget);
        when(mockWebTarget.request(MediaType.APPLICATION_JSON)).thenReturn(mockInvocationBuilder);
        when(mockInvocationBuilder.get()).thenReturn(mockResponse);
        when(mockResponse.getStatus()).thenReturn(200);
        when(mockResponse.readEntity(Document.class)).thenReturn(getTestBasicCheckDocument());
        when(credentialBuilder.buildCredential(any(), anyString(), anyString())).thenReturn(any());

        credentialService.getCredential(accessToken, proofJwt);

        verify((CredentialBuilder<BasicCheckCredentialSubject>) credentialBuilder, times(1))
                .buildCredential(
                        any(BasicCheckCredentialSubject.class),
                        eq("BasicCheckCredential"),
                        eq("2025-02-06"));
    }

    @Test
    void Should_Call_Credential_Builder_With_VeteranCardCredentialSubject()
            throws AccessTokenValidationException,
                    ProofJwtValidationException,
                    DataStoreException,
                    CredentialServiceException,
                    CredentialOfferNotFoundException,
                    SigningException,
                    NoSuchAlgorithmException,
                    URISyntaxException,
                    JsonProcessingException {
        when(dynamoDbService.getCredentialOffer(anyString())).thenReturn(credentialOfferCacheItem);
        when(mockHttpClient.target(any(URI.class))).thenReturn(mockWebTarget);
        when(mockWebTarget.request(MediaType.APPLICATION_JSON)).thenReturn(mockInvocationBuilder);
        when(mockInvocationBuilder.get()).thenReturn(mockResponse);
        when(mockResponse.getStatus()).thenReturn(200);
        when(mockResponse.readEntity(Document.class)).thenReturn(getTestVeteranCardDocument());
        when(credentialBuilder.buildCredential(any(), anyString(), anyString())).thenReturn(any());

        credentialService.getCredential(accessToken, proofJwt);

        verify((CredentialBuilder<VeteranCardCredentialSubject>) credentialBuilder, times(1))
                .buildCredential(
                        any(VeteranCardCredentialSubject.class),
                        eq("digitalVeteranCard"),
                        eq("2000-07-11"));
    }

    @Test
    void shouldReturnCredential()
            throws AccessTokenValidationException,
                    ProofJwtValidationException,
                    DataStoreException,
                    CredentialServiceException,
                    CredentialOfferNotFoundException,
                    ParseException,
                    SigningException,
                    NoSuchAlgorithmException,
                    URISyntaxException,
                    JsonProcessingException {

        when(dynamoDbService.getCredentialOffer(anyString())).thenReturn(credentialOfferCacheItem);
        when(mockHttpClient.target(any(URI.class))).thenReturn(mockWebTarget);
        when(mockWebTarget.request(MediaType.APPLICATION_JSON)).thenReturn(mockInvocationBuilder);
        when(mockInvocationBuilder.get()).thenReturn(mockResponse);
        when(mockResponse.getStatus()).thenReturn(200);
        when(mockResponse.readEntity(Document.class)).thenReturn(getTestSocialSecurityDocument());
        SignedJWT testCredentialJwt =
                SignedJWT.parse(
                        "eyJhbGciOiJIUzI1NiIsInR5cCI6IkpXVCJ9.eyJzdWIiOiIxMjM0NTY3ODkwIiwibmFtZSI6IkpvaG4gRG9lIiwiaWF0IjoxNTE2MjM5MDIyfQ.SflKxwRJSMeKKF2QT4fwpMeJf36POk6yJV_adQssw5c");
        Credential testCredentialObject = new Credential(testCredentialJwt);
        when(credentialBuilder.buildCredential(any(), anyString()))
                .thenReturn(testCredentialObject);

        Credential credentialServiceReturnValue =
                credentialService.getCredential(accessToken, proofJwt);

        assertEquals(
                testCredentialObject.getCredential(), credentialServiceReturnValue.getCredential());
        verify(accessTokenService).verifyAccessToken(accessToken);
        verify(proofJwtService).verifyProofJwt(proofJwt);
        verify(dynamoDbService, times(1)).getCredentialOffer(CREDENTIAL_IDENTIFIER);
        verify(dynamoDbService, times(1)).deleteCredentialOffer(CREDENTIAL_IDENTIFIER);
        verify((CredentialBuilder<SocialSecurityCredentialSubject>) credentialBuilder, times(1))
                .buildCredential(
                        any(SocialSecurityCredentialSubject.class), eq("SocialSecurityCredential"));
    }

    private static @NotNull Document getTestSocialSecurityDocument() {
        HashMap<String, Object> data = new HashMap<>();
        data.put("familyName", "Edwards Green");
        data.put("givenName", "Sarah Elizabeth");
        data.put("nino", "QQ123456C");
        data.put("title", "Miss");
        return new Document(DOCUMENT_ID, data, "SocialSecurityCredential", "v2.0");
    }

    private static @NotNull Document getTestBasicCheckDocument() {
        HashMap<String, Object> data = new HashMap<>();
        data.put("issuance-day", "11");
        data.put("issuance-month", "07");
        data.put("issuance-year", "2024");
        data.put("birth-day", "05");
        data.put("birth-month", "12");
        data.put("birth-year", "1970");
        data.put("firstName", "Bonnie");
        data.put("lastName", "Blue");
        data.put("subBuildingName", "Flat 11");
        data.put("buildingName", "Blashford");
        data.put("streetName", "Adelaide Road");
        data.put("addressLocality", "London");
        data.put("addressCountry", "GB");
        data.put("postalCode", "NW3 3RX");
        data.put("certificateNumber", "009878863");
        data.put("applicationNumber", "E0023455534");
        return new Document(DOCUMENT_ID, data, "BasicCheckCredential", "v2.0");
    }

    private static @NotNull Document getTestVeteranCardDocument() {
        HashMap<String, Object> data = new HashMap<>();
        data.put("cardExpiryDate-day", "11");
        data.put("cardExpiryDate-month", "07");
        data.put("cardExpiryDate-year", "2000");
        data.put("dateOfBirth-day", "05");
        data.put("dateOfBirth-month", "12");
        data.put("dateOfBirth-year", "1970");
        data.put("givenName", "Bonnie");
        data.put("familyName", "Blue");
        data.put("serviceNumber", "25057386");
        data.put("serviceBranch", "HM Naval Service");
        return new Document(DOCUMENT_ID, data, "digitalVeteranCard", "v2.0");
    }

    private static SignedJWT getTestProofJwt(String nonce) throws ParseException, JOSEException {
        SignedJWT proofJwt =
                new SignedJWT(
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
        ECDSASigner ecSigner = new ECDSASigner(getEsPrivateKey());
        proofJwt.sign(ecSigner);
        return proofJwt;
    }

    private static SignedJWT getTestAccessToken() throws ParseException, JOSEException {
        SignedJWT accessToken =
                new SignedJWT(
                        new JWSHeader.Builder(JWSAlgorithm.ES256)
                                .keyID("cb5a1a8b-809a-4f32-944d-caae1a57ed91")
                                .build(),
                        new JWTClaimsSet.Builder()
                                .claim("credential_identifiers", new ArrayList<String>())
                                .build());
        ECDSASigner ecSigner = new ECDSASigner(getEsPrivateKey());
        accessToken.sign(ecSigner);
        return accessToken;
    }

    private static SignedJWT getTestAccessToken(String nonce, String subject)
            throws ParseException, JOSEException {
        SignedJWT accessToken =
                new SignedJWT(
                        new JWSHeader.Builder(JWSAlgorithm.ES256)
                                .keyID("cb5a1a8b-809a-4f32-944d-caae1a57ed91")
                                .build(),
                        new JWTClaimsSet.Builder()
                                .issueTime(Date.from(Instant.now()))
                                .issuer("urn:fdc:gov:uk:wallet")
                                .audience("urn:fdc:gov:uk:example-credential-issuer")
                                .subject(subject)
                                .claim("c_nonce", nonce)
                                .claim(
                                        "credential_identifiers",
                                        Collections.singletonList(CREDENTIAL_IDENTIFIER))
                                .build());
        ECDSASigner ecSigner = new ECDSASigner(getEsPrivateKey());
        accessToken.sign(ecSigner);
        return accessToken;
    }

    private static ECKey getEsPrivateKey() throws java.text.ParseException {
        String privateKeyJwkBase64 =
                "eyJrdHkiOiJFQyIsImQiOiI4SGJYN0xib1E1OEpJOGo3eHdfQXp0SlRVSDVpZTFtNktIQlVmX3JnakVrIiwidXNlIjoic2lnIiwiY3J2IjoiUC0yNTYiLCJraWQiOiJmMDYxMWY3Zi04YTI5LTQ3ZTEtYmVhYy1mNWVlNWJhNzQ3MmUiLCJ4IjoiSlpKeE83b2JSOElzdjU4NUVzaWcwYlAwQUdfb1N6MDhSMS11VXBiYl9JRSIsInkiOiJtNjBRMmtMMExiaEhTbHRjS1lyTG8wczE1M1hveF9tVDV2UlV6Z3g4TWtFIiwiaWF0IjoxNzEyMTQ2MTc5fQ==";
        return ECKey.parse(new String(Base64.getDecoder().decode(privateKeyJwkBase64)));
    }
}
