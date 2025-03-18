package uk.gov.di.mobile.wallet.cri.services.authentication;

import com.nimbusds.jose.JOSEException;
import com.nimbusds.jose.JWSAlgorithm;
import com.nimbusds.jose.JWSHeader;
import com.nimbusds.jose.KeySourceException;
import com.nimbusds.jose.crypto.ECDSASigner;
import com.nimbusds.jose.jwk.JWK;
import com.nimbusds.jwt.JWTClaimsSet;
import com.nimbusds.jwt.SignedJWT;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.junit.jupiter.MockitoExtension;
import testUtils.MockAccessTokenBuilder;
import uk.gov.di.mobile.wallet.cri.services.ConfigurationService;
import uk.gov.di.mobile.wallet.cri.services.JwksService;

import java.text.ParseException;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;
import static testUtils.EsKeyHelper.getEsKey;

@ExtendWith(MockitoExtension.class)
class AccessTokenServiceTest {

    private AccessTokenService accessTokenService;
    private ECDSASigner ecSigner;
    private final ConfigurationService configurationService = mock(ConfigurationService.class);
    private final JwksService jwksService = mock(JwksService.class);

    @BeforeEach
    void setup() throws ParseException, JOSEException {
        ecSigner = new ECDSASigner(getEsKey());
        accessTokenService = new AccessTokenService(jwksService, configurationService);
        when(configurationService.getSelfUrl()).thenReturn("https://issuer-url.gov.uk");
        when(configurationService.getOneLoginAuthServerUrl()).thenReturn("https://auth-url.gov.uk");
    }

    @Test
    void Should_ThrowException_When_AlgParamDoesNotMatchConfig() {
        SignedJWT mockAccessToken = new MockAccessTokenBuilder("RS256").build();

        AccessTokenValidationException exception =
                assertThrows(
                        AccessTokenValidationException.class,
                        () -> accessTokenService.verifyAccessToken(mockAccessToken));
        assertEquals(
                "JWT alg header claim [RS256] does not match client config alg [ES256]",
                exception.getMessage());
    }

    @Test
    void Should_ThrowAccessTokenValidationException_When_KidParamIsNull() {
        SignedJWT mockAccessToken = new MockAccessTokenBuilder("ES256").withKid(null).build();

        AccessTokenValidationException exception =
                assertThrows(
                        AccessTokenValidationException.class,
                        () -> accessTokenService.verifyAccessToken(mockAccessToken));
        assertEquals("JWT kid header claim is null", exception.getMessage());
    }

    @Test
    void Should_ThrowAccessTokenValidationException_When_RequiredClaimsAreMissing() {
        SignedJWT mockAccessToken =
                new SignedJWT(
                        new JWSHeader.Builder(JWSAlgorithm.ES256)
                                .keyID("cb5a1a8b-809a-4f32-944d-caae1a57ed91")
                                .build(),
                        new JWTClaimsSet.Builder().build());

        AccessTokenValidationException exception =
                assertThrows(
                        AccessTokenValidationException.class,
                        () -> accessTokenService.verifyAccessToken(mockAccessToken));
        assertEquals(
                "JWT missing required claims: [aud, c_nonce, credential_identifiers, iss, sub]",
                exception.getMessage());
    }

    @Test
    void Should_ThrowAccessTokenValidationException_When_CredentialIdentifiersClaimIsEmpty() {
        SignedJWT mockAccessToken =
                new MockAccessTokenBuilder("ES256")
                        .withClaim("credential_identifiers", List.of())
                        .build();

        AccessTokenValidationException exception =
                assertThrows(
                        AccessTokenValidationException.class,
                        () -> accessTokenService.verifyAccessToken(mockAccessToken));
        assertEquals("Empty credential_identifiers claim", exception.getMessage());
    }

    @Test
    void Should_ThrowAccessTokenValidationException_When_AudClaimDoesNotMatchConfig() {
        MockAccessTokenBuilder builder = new MockAccessTokenBuilder("ES256");
        SignedJWT mockAccessToken = builder.withAudience("invalid-audience").build();

        AccessTokenValidationException exception =
                assertThrows(
                        AccessTokenValidationException.class,
                        () -> accessTokenService.verifyAccessToken(mockAccessToken));
        assertEquals(
                "JWT aud claim has value [invalid-audience], must be [https://issuer-url.gov.uk]",
                exception.getMessage());
    }

    @Test
    void Should_ThrowAccessTokenValidationException_When_IssClaimDoesNotMatchConfig() {
        MockAccessTokenBuilder builder = new MockAccessTokenBuilder("ES256");
        SignedJWT mockAccessToken = builder.withIssuer("invalid-issuer").build();

        AccessTokenValidationException exception =
                assertThrows(
                        AccessTokenValidationException.class,
                        () -> accessTokenService.verifyAccessToken(mockAccessToken));
        assertEquals(
                "JWT iss claim has value invalid-issuer, must be https://auth-url.gov.uk",
                exception.getMessage());
    }

    @Test
    void Should_ThrowAccessTokenValidationException_When_SignatureVerificationFails()
            throws JOSEException, ParseException {
        JWK publicKey =
                JWK.parse(
                        "{\"kty\":\"EC\",\"crv\":\"P-256\",\"kid\":\"cb5a1a8b-809a-4f32-944d-caae1a57ed91\",\"x\":\"sSdmBkED2EfjTdX-K2_cT6CfBwXQFt-DJ6v8-6tr_n8\",\"y\":\"WTXmQdqLwrmHN5tiFsTFUtNAvDYhhTQB4zyfteCrWIE\",\"alg\":\"ES256\"}");
        when(jwksService.retrieveJwkFromURLWithKeyId(any(String.class))).thenReturn(publicKey);
        SignedJWT mockAccessToken = new MockAccessTokenBuilder("ES256").build();
        mockAccessToken.sign(ecSigner);

        AccessTokenValidationException exception =
                assertThrows(
                        AccessTokenValidationException.class,
                        () -> accessTokenService.verifyAccessToken(mockAccessToken));
        assertEquals("Access token signature verification failed", exception.getMessage());
    }

    @Test
    void Should_ThrowAccessTokenValidationException_When_JwksServiceThrowsKeySourceException()
            throws JOSEException {
        SignedJWT mockAccessToken = new MockAccessTokenBuilder("ES256").build();
        when(jwksService.retrieveJwkFromURLWithKeyId(any(String.class)))
                .thenThrow(new KeySourceException("Some error fetching JWKs"));

        AccessTokenValidationException exception =
                assertThrows(
                        AccessTokenValidationException.class,
                        () -> accessTokenService.verifyAccessToken(mockAccessToken));
        assertEquals("Some error fetching JWKs", exception.getMessage());
    }

    @Test
    void Should_ReturnTokenData_When_JwtVerificationSucceeds()
            throws JOSEException, ParseException, AccessTokenValidationException {
        JWK publicKey = getEsKey().toPublicJWK();
        when(jwksService.retrieveJwkFromURLWithKeyId(any(String.class))).thenReturn(publicKey);
        SignedJWT mockAccessToken = new MockAccessTokenBuilder("ES256").build();
        mockAccessToken.sign(ecSigner);

        AccessTokenService.AccessTokenData response =
                accessTokenService.verifyAccessToken(mockAccessToken);

        assertEquals("efb52887-48d6-43b7-b14c-da7896fbf54d", response.credentialIdentifier());
        assertEquals(
                "urn:fdc:wallet.account.gov.uk:2024:DtPT8x-dp_73tnlY3KNTiCitziN9GEherD16bqxNt9i",
                response.walletSubjectId());
        assertEquals("134e0c41-a8b4-46d4-aec8-cd547e125589", response.nonce());
    }
}
