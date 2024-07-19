package uk.gov.di.mobile.wallet.cri.credential;

import com.nimbusds.jose.JOSEException;
import com.nimbusds.jose.JWSAlgorithm;
import com.nimbusds.jose.JWSHeader;
import com.nimbusds.jose.KeySourceException;
import com.nimbusds.jose.crypto.ECDSASigner;
import com.nimbusds.jose.crypto.RSASSASigner;
import com.nimbusds.jose.jwk.ECKey;
import com.nimbusds.jose.jwk.JWK;
import com.nimbusds.jwt.JWTClaimsSet;
import com.nimbusds.jwt.SignedJWT;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.junit.jupiter.MockitoExtension;
import uk.gov.di.mobile.wallet.cri.services.ConfigurationService;

import java.net.URL;
import java.security.KeyFactory;
import java.security.NoSuchAlgorithmException;
import java.security.interfaces.RSAPrivateKey;
import java.security.spec.InvalidKeySpecException;
import java.security.spec.PKCS8EncodedKeySpec;
import java.text.ParseException;
import java.time.Instant;
import java.util.Arrays;
import java.util.Base64;
import java.util.Date;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;
import static org.mockito.Mockito.verify;

@ExtendWith(MockitoExtension.class)
class AccessTokenServiceTest {

    private AccessTokenService accessTokenService;
    private final ConfigurationService configurationService = new ConfigurationService();
    private final JwksService jwksService = mock(JwksService.class);

    @BeforeEach
    void setup() {
        accessTokenService = new AccessTokenService(configurationService, jwksService);
    }

    @Test
    void shouldThrowAccessTokenValidationExceptionWhenJwtHeaderAlgDoesNotMatchConfig()
            throws JOSEException, InvalidKeySpecException, NoSuchAlgorithmException {
        SignedJWT signedJwt =
                new SignedJWT(
                        new JWSHeader.Builder(JWSAlgorithm.RS256).build(),
                        new JWTClaimsSet.Builder().build());
        RSASSASigner rsaSigner = new RSASSASigner(getRsaKey());
        signedJwt.sign(rsaSigner);

        AccessTokenValidationException exception =
                assertThrows(
                        AccessTokenValidationException.class,
                        () -> accessTokenService.verifyAccessToken(signedJwt));
        assertEquals(
                "JWT alg header claim [RS256] does not match client config alg [ES256]",
                exception.getMessage());
    }

    @Test
    void shouldThrowAccessTokenValidationExceptionWhenJwtHeaderKidIsNull()
            throws JOSEException, ParseException {
        SignedJWT signedJwt =
                new SignedJWT(
                        new JWSHeader.Builder(JWSAlgorithm.ES256).build(),
                        new JWTClaimsSet.Builder().build());
        ECDSASigner ecSigner = new ECDSASigner(getEsKey());
        signedJwt.sign(ecSigner);

        AccessTokenValidationException exception =
                assertThrows(
                        AccessTokenValidationException.class,
                        () -> accessTokenService.verifyAccessToken(signedJwt));
        assertEquals("JWT kid header claim is null", exception.getMessage());
    }

    @Test
    void shouldThrowAccessTokenValidationExceptionWhenRequiredClaimAreNull()
            throws JOSEException, ParseException {
        SignedJWT signedJwt =
                new SignedJWT(
                        new JWSHeader.Builder(JWSAlgorithm.ES256)
                                .keyID("cb5a1a8b-809a-4f32-944d-caae1a57ed91")
                                .build(),
                        new JWTClaimsSet.Builder().build());
        ECDSASigner ecSigner = new ECDSASigner(getEsKey());
        signedJwt.sign(ecSigner);

        AccessTokenValidationException exception =
                assertThrows(
                        AccessTokenValidationException.class,
                        () -> accessTokenService.verifyAccessToken(signedJwt));
        assertEquals(
                "JWT missing required claims: [aud, c_nonce, credential_identifiers, iss, sub]",
                exception.getMessage());
    }

    @Test
    void shouldThrowAccessTokenValidationExceptionWhenAudienceClaimDoesNotMatchConfig()
            throws JOSEException, ParseException {
        SignedJWT signedJwt = getTestAccessToken("urn:fdc:gov:uk:wallet", "invalid-audience");
        ECDSASigner ecSigner = new ECDSASigner(getEsKey());
        signedJwt.sign(ecSigner);

        AccessTokenValidationException exception =
                assertThrows(
                        AccessTokenValidationException.class,
                        () -> accessTokenService.verifyAccessToken(signedJwt));
        assertEquals(
                "JWT aud claim has value [invalid-audience], must be [urn:fdc:gov:uk:example-credential-issuer]",
                exception.getMessage());
    }

    @Test
    void shouldThrowAccessTokenValidationExceptionWhenIssuerClaimDoesNotMatchConfig()
            throws JOSEException, ParseException {
        SignedJWT signedJwt =
                getTestAccessToken("invalid-issuer", "urn:fdc:gov:uk:example-credential-issuer");
        ECDSASigner ecSigner = new ECDSASigner(getEsKey());
        signedJwt.sign(ecSigner);

        AccessTokenValidationException exception =
                assertThrows(
                        AccessTokenValidationException.class,
                        () -> accessTokenService.verifyAccessToken(signedJwt));
        assertEquals(
                "JWT iss claim has value invalid-issuer, must be urn:fdc:gov:uk:wallet",
                exception.getMessage());
    }

    @Test
    void shouldThrowAccessTokenValidationExceptionWhenSignatureVerificationFails()
            throws JOSEException, ParseException {
        JWK publicKey =
                JWK.parse(
                        "{\"kty\":\"EC\",\"crv\":\"P-256\",\"kid\":\"cb5a1a8b-809a-4f32-944d-caae1a57ed91\",\"x\":\"sSdmBkED2EfjTdX-K2_cT6CfBwXQFt-DJ6v8-6tr_n8\",\"y\":\"WTXmQdqLwrmHN5tiFsTFUtNAvDYhhTQB4zyfteCrWIE\",\"alg\":\"ES256\"}");
        when(jwksService.retrieveJwkFromURLWithKeyId(any(URL.class), any(String.class)))
                .thenReturn(publicKey);

        SignedJWT signedJwt =
                getTestAccessToken(
                        "urn:fdc:gov:uk:wallet", "urn:fdc:gov:uk:example-credential-issuer");
        ECDSASigner ecSigner = new ECDSASigner(getEsKey());
        signedJwt.sign(ecSigner);

        AccessTokenValidationException exception =
                assertThrows(
                        AccessTokenValidationException.class,
                        () -> accessTokenService.verifyAccessToken(signedJwt));
        assertEquals("Access token signature verification failed", exception.getMessage());
    }

    @Test
    void shouldThrowAccessTokenValidationExceptionWhenJwksServiceThrowsKeySourceException()
            throws JOSEException, ParseException {
        when(jwksService.retrieveJwkFromURLWithKeyId(any(URL.class), any(String.class)))
                .thenThrow(new KeySourceException("Some error fetching JWKs"));

        SignedJWT signedJwt =
                getTestAccessToken(
                        "urn:fdc:gov:uk:wallet", "urn:fdc:gov:uk:example-credential-issuer");
        ECDSASigner ecSigner = new ECDSASigner(getEsKey());
        signedJwt.sign(ecSigner);

        AccessTokenValidationException exception =
                assertThrows(
                        AccessTokenValidationException.class,
                        () -> accessTokenService.verifyAccessToken(signedJwt));
        assertEquals("Some error fetching JWKs", exception.getMessage());
    }

    @Test
    void shouldNotThrowErrorWhenJwtVerificationSucceeds() throws JOSEException, ParseException {
        ECKey key = getEsKey();
        JWK publicKey = key.toPublicJWK();
        when(jwksService.retrieveJwkFromURLWithKeyId(any(URL.class), any(String.class)))
                .thenReturn(publicKey);

        SignedJWT signedJwt =
                getTestAccessToken(
                        "urn:fdc:gov:uk:wallet", "urn:fdc:gov:uk:example-credential-issuer");
        ECDSASigner ecSigner = new ECDSASigner(key);
        signedJwt.sign(ecSigner);

        assertDoesNotThrow(() -> accessTokenService.verifyAccessToken(signedJwt));
        verify(jwksService)
                .retrieveJwkFromURLWithKeyId(
                        any(URL.class), any(String.class));
    }

    private static SignedJWT getTestAccessToken(String issuer, String audience) {
        return new SignedJWT(
                new JWSHeader.Builder(JWSAlgorithm.ES256)
                        .keyID("cb5a1a8b-809a-4f32-944d-caae1a57ed91")
                        .build(),
                new JWTClaimsSet.Builder()
                        .issueTime(Date.from(Instant.now()))
                        .issuer(issuer)
                        .audience(audience)
                        .subject("test-sub")
                        .claim("c_nonce", "test-c-nonce")
                        .claim(
                                "credential_identifiers",
                                Arrays.asList("test-credential-identifier"))
                        .build());
    }

    private RSAPrivateKey getRsaKey() throws InvalidKeySpecException, NoSuchAlgorithmException {
        String privateKey =
                "MIIEvQIBADANBgkqhkiG9w0BAQEFAASCBKcwggSjAgEAAoIBAQC3ZD/jwRxlA/vv43rx9T6ovlczQnsntAjgTbDRTDcsw7TYM1Q3wwD3OPYt5qaKg5qTKaOB90at2jBPih8QH/jDXfSjlU7RlSe8p7fYxaj73ds4ULGAOWaQqmbu+BppcUUPzKII1QBF8Mk2qn/GiVVfqCwDx7uOOT+jPr3H/Wysi+lI8JFYn60wj4oanmbr6iuIVKGWP41STlLu7aLhVM4JxAA2T85Ddot9nPCMftXLBW+QDZUvrbv4CSbtLjuxuqkKdN6FNpSjvx/oSZDDw6jyWydLI68CUQ09Nbm63oY/KpY5V/o+m81Uv2J0Ov+oHCF/0SU8/Nf7kWTK9uTXRW03AgMBAAECggEAE5rV8aUNQgdBAY4R8JfFEQj4DXTH8aCfaksj4dwB8fkh9hLWp/divQsL1jBYEWqsNZs37YbfuWofzAD5/SFN2KTMqEgn2uPVEafkUXof7Hz1GHoX35tDSafNxTIksKz5Mw0vLT6H/vIUsJFdg33e8JDr06Ogez3Hfc4RP7XpzjAgdUoVcEhF4VKFo6r7ImETUWQ4E5mCmWnqKPiqrGrVcwP0xVtz09yeE45eAVX89Sgn4Brxv48TDnKtKYxU2NhEKRJyL2EU0QLjW80tyGl/zzEzhn/QpjJqfKPB5REP41oPBfER/HVHoXDinelcNXYUkBUqbtcKexNI8wPHA5/TcQKBgQDb9UsmJiDHvlaTZS/Ge7+6wcn2zDT/o0S3ZRLzJeWcNLBonohiOt0FBvj/NvttqDGK53yjZZFRfWqJaG2pFJqhY/PLiX4hlt6nAQkzUDxpI08sU0Q4IgEomJUvXmicEJNHR1L4YYyPyv5nDB3fmGsyUTdbeWOFJRIxr25lOS1MNQKBgQDVcRPjsXJSDuRNOo/GnAGZiYlfAvxeXfkIsB45vbfEp+9HcvA7BZZP3b2JbMDIj2JcpYo5+W/Z3uU5nBVU0WjtXZ79uSvV/14oH95z5uAIm6JtIWgVGC31fGyPl8RLgn00fYa6MGXWUo82WnLYDBmC3wa7Xfar/dKVUQBZWGsJOwKBgC3+dOBdSK17146qsfrHFahvrVO2D78E3PGcaQH/AqxPODQoMkyYEm9ird5wGNMtQG7TSPTB4Ekx+H0TIRsh+9OTmv8MmRtc+OHjDZF1TayOfZe/MZyrP6LFhSyKiUVZEfLtryPRAhtvTxMtLXH75S54XSL7lxvYTJ2nGWaBNj+hAoGAf67ujAZp8ibQcla3DcPjvRqm3/ykRjuHL6hT3IzeszkXDjH2/gfgnJR0vxIc3Z3Q5MVuxDGwtK//hpAVvrCrSVv5MaUlURY8GFrAM6uIl/2qlAgpAH1/eNxfASN0HQvJpK32+8jaEvU+kPBYxV+vnzeWCl4yoz7rS8GyKMCY/2MCgYEAtDBPMVHPbjauWjzIjXlaJTMlAoWOmnTB36rkNC9U0SRCxvP6hcLTgvDv1TR2+mKM5tQS2/C1XC3zajUFhIqR0funEEWqUoKfZEWqagOILxpIaeida2MwWXNYLQIAimoMQ6CbqdHsPOrQ/ZtxqlwDUnPYumRawSyxsGIThDnN4oc=";
        return (RSAPrivateKey)
                KeyFactory.getInstance("RSA")
                        .generatePrivate(
                                new PKCS8EncodedKeySpec(Base64.getDecoder().decode(privateKey)));
    }

    private ECKey getEsKey() throws ParseException {
        String privateKeyJwkBase64 =
                "eyJrdHkiOiJFQyIsImQiOiI4SGJYN0xib1E1OEpJOGo3eHdfQXp0SlRVSDVpZTFtNktIQlVmX3JnakVrIiwidXNlIjoic2lnIiwiY3J2IjoiUC0yNTYiLCJraWQiOiJmMDYxMWY3Zi04YTI5LTQ3ZTEtYmVhYy1mNWVlNWJhNzQ3MmUiLCJ4IjoiSlpKeE83b2JSOElzdjU4NUVzaWcwYlAwQUdfb1N6MDhSMS11VXBiYl9JRSIsInkiOiJtNjBRMmtMMExiaEhTbHRjS1lyTG8wczE1M1hveF9tVDV2UlV6Z3g4TWtFIiwiaWF0IjoxNzEyMTQ2MTc5fQ==";
        return ECKey.parse(new String(Base64.getDecoder().decode(privateKeyJwkBase64)));
    }
}
