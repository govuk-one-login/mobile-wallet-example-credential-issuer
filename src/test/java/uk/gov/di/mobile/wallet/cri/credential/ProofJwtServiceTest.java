package uk.gov.di.mobile.wallet.cri.credential;

import com.nimbusds.jose.JOSEException;
import com.nimbusds.jose.JWSAlgorithm;
import com.nimbusds.jose.JWSHeader;
import com.nimbusds.jose.crypto.ECDSASigner;
import com.nimbusds.jose.crypto.RSASSASigner;
import com.nimbusds.jose.jwk.ECKey;
import com.nimbusds.jwt.JWTClaimsSet;
import com.nimbusds.jwt.SignedJWT;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import uk.gov.di.mobile.wallet.cri.services.ConfigurationService;

import java.security.KeyFactory;
import java.security.NoSuchAlgorithmException;
import java.security.interfaces.RSAPrivateKey;
import java.security.spec.InvalidKeySpecException;
import java.security.spec.PKCS8EncodedKeySpec;
import java.text.ParseException;
import java.time.Instant;
import java.util.Base64;
import java.util.Date;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.containsString;
import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

class ProofJwtServiceTest {

    private ProofJwtService proofJwtService;
    private final ConfigurationService configurationService = mock(ConfigurationService.class);
    private static final String TEST_EXAMPLE_CRI_URL = "https://test-example-cri.gov.uk";

    @BeforeEach
    void setup() {
        proofJwtService = new ProofJwtService(configurationService);
        when(configurationService.getSelfUrl()).thenReturn(TEST_EXAMPLE_CRI_URL);
    }

    @Test
    void shouldThrowProofJwtValidationExceptionWhenJwtHeaderAlgDoesNotMatchConfig()
            throws JOSEException, InvalidKeySpecException, NoSuchAlgorithmException {
        SignedJWT signedJwt =
                new SignedJWT(
                        new JWSHeader.Builder(JWSAlgorithm.RS256).build(),
                        new JWTClaimsSet.Builder().build());

        RSASSASigner rsaSigner = new RSASSASigner(getRsaPrivateKey());
        signedJwt.sign(rsaSigner);

        ProofJwtValidationException exception =
                assertThrows(
                        ProofJwtValidationException.class,
                        () -> proofJwtService.verifyProofJwt(signedJwt));
        assertEquals(
                "JWT alg header claim [RS256] does not match client config alg [ES256]",
                exception.getMessage());
    }

    @Test
    void shouldThrowProofJwtValidationExceptionWhenJwtHeaderKidIsNull()
            throws JOSEException, ParseException {
        SignedJWT signedJwt =
                new SignedJWT(
                        new JWSHeader.Builder(JWSAlgorithm.ES256).build(),
                        new JWTClaimsSet.Builder().build());

        ECDSASigner ecSigner = new ECDSASigner(getEsPrivateKey());
        signedJwt.sign(ecSigner);

        ProofJwtValidationException exception =
                assertThrows(
                        ProofJwtValidationException.class,
                        () -> proofJwtService.verifyProofJwt(signedJwt));
        assertEquals("JWT kid header claim is null", exception.getMessage());
    }

    @Test
    void shouldThrowProofJwtValidationExceptionWhenRequiredClaimAreNull()
            throws JOSEException, ParseException {
        SignedJWT signedJwt =
                new SignedJWT(
                        new JWSHeader.Builder(JWSAlgorithm.ES256)
                                .keyID("did:key:zDnaeUqPxbNEqiYDMyo6EHt9XxpQcE2arUVgkZyfwA6G5Xacf")
                                .build(),
                        new JWTClaimsSet.Builder().build());

        ECDSASigner ecSigner = new ECDSASigner(getEsPrivateKey());
        signedJwt.sign(ecSigner);

        ProofJwtValidationException exception =
                assertThrows(
                        ProofJwtValidationException.class,
                        () -> proofJwtService.verifyProofJwt(signedJwt));
        assertEquals("JWT missing required claims: [aud, iat, iss, nonce]", exception.getMessage());
    }

    @Test
    void shouldThrowProofJwtValidationExceptionWhenAudienceClaimDoesNotMatchConfig()
            throws JOSEException, ParseException {
        SignedJWT signedJwt =
                getTestProofJwt(
                        "did:key:zDnaeUqPxbNEqiYDMyo6EHt9XxpQcE2arUVgkZyfwA6G5Xacf",
                        "urn:fdc:gov:uk:wallet",
                        "invalid-audience");

        ECDSASigner ecSigner = new ECDSASigner(getEsPrivateKey());
        signedJwt.sign(ecSigner);

        ProofJwtValidationException exception =
                assertThrows(
                        ProofJwtValidationException.class,
                        () -> proofJwtService.verifyProofJwt(signedJwt));
        assertEquals(
                "JWT aud claim has value [invalid-audience], must be [https://test-example-cri.gov.uk]",
                exception.getMessage());
    }

    @Test
    void shouldThrowProofJwtValidationExceptionWhenAIssuerClaimDoesNotMatchConfig()
            throws JOSEException, ParseException {
        SignedJWT signedJwt =
                getTestProofJwt(
                        "did:key:zDnaeUqPxbNEqiYDMyo6EHt9XxpQcE2arUVgkZyfwA6G5Xacf",
                        "invalid-issuer",
                        "http://localhost:8080");
        ECDSASigner ecSigner = new ECDSASigner(getEsPrivateKey());
        signedJwt.sign(ecSigner);

        ProofJwtValidationException exception =
                assertThrows(
                        ProofJwtValidationException.class,
                        () -> proofJwtService.verifyProofJwt(signedJwt));
        assertEquals(
                "JWT iss claim has value invalid-issuer, must be urn:fdc:gov:uk:wallet",
                exception.getMessage());
    }

    @Test
    void shouldThrowProofJwtValidationExceptionWhenDidKeyIsInvalid()
            throws JOSEException, ParseException {
        SignedJWT signedJwt =
                getTestProofJwt(
                        "did:key:notAValidDidKey", "urn:fdc:gov:uk:wallet", TEST_EXAMPLE_CRI_URL);
        ECDSASigner ecSigner = new ECDSASigner(getEsPrivateKey());
        signedJwt.sign(ecSigner);

        ProofJwtValidationException exception =
                assertThrows(
                        ProofJwtValidationException.class,
                        () -> proofJwtService.verifyProofJwt(signedJwt));

        assertThat(
                exception.getMessage(),
                containsString("Error verifying signature: did:key must be base58 encoded"));
    }

    @Test
    void shouldThrowProofJwtValidationExceptionWhenSignatureVerificationFailed()
            throws JOSEException, ParseException {
        SignedJWT signedJwt =
                getTestProofJwt(
                        "did:key:zDnaewZMz7MN6xSaAFADkDZJzMLbGSV25uKHAeXaxnPCwZomX",
                        "urn:fdc:gov:uk:wallet",
                        TEST_EXAMPLE_CRI_URL);

        ECDSASigner ecSigner = new ECDSASigner(getEsPrivateKey());
        signedJwt.sign(ecSigner);

        ProofJwtValidationException exception =
                assertThrows(
                        ProofJwtValidationException.class,
                        () -> proofJwtService.verifyProofJwt(signedJwt));

        assertThat(
                exception.getMessage(), containsString("Proof JWT signature verification failed"));
    }

    @Test
    void shouldNotThrowErrorWhenJwtVerificationSucceeds() throws JOSEException, ParseException {
        SignedJWT signedJwt =
                getTestProofJwt(
                        "did:key:zDnaeUqPxbNEqiYDMyo6EHt9XxpQcE2arUVgkZyfwA6G5Xacf",
                        "urn:fdc:gov:uk:wallet",
                        TEST_EXAMPLE_CRI_URL);
        ECDSASigner ecSigner = new ECDSASigner(getEsPrivateKey());
        signedJwt.sign(ecSigner);

        assertDoesNotThrow(() -> proofJwtService.verifyProofJwt(signedJwt));
    }

    private static SignedJWT getTestProofJwt(String kid, String issuer, String audience) {
        return new SignedJWT(
                new JWSHeader.Builder(JWSAlgorithm.ES256).keyID(kid).build(),
                new JWTClaimsSet.Builder()
                        .issueTime(Date.from(Instant.now()))
                        .issuer(issuer)
                        .audience(audience)
                        .claim("nonce", "test-nonce")
                        .build());
    }

    private RSAPrivateKey getRsaPrivateKey()
            throws InvalidKeySpecException, NoSuchAlgorithmException {
        String privateKey =
                "MIIEvQIBADANBgkqhkiG9w0BAQEFAASCBKcwggSjAgEAAoIBAQC3ZD/jwRxlA/vv43rx9T6ovlczQnsntAjgTbDRTDcsw7TYM1Q3wwD3OPYt5qaKg5qTKaOB90at2jBPih8QH/jDXfSjlU7RlSe8p7fYxaj73ds4ULGAOWaQqmbu+BppcUUPzKII1QBF8Mk2qn/GiVVfqCwDx7uOOT+jPr3H/Wysi+lI8JFYn60wj4oanmbr6iuIVKGWP41STlLu7aLhVM4JxAA2T85Ddot9nPCMftXLBW+QDZUvrbv4CSbtLjuxuqkKdN6FNpSjvx/oSZDDw6jyWydLI68CUQ09Nbm63oY/KpY5V/o+m81Uv2J0Ov+oHCF/0SU8/Nf7kWTK9uTXRW03AgMBAAECggEAE5rV8aUNQgdBAY4R8JfFEQj4DXTH8aCfaksj4dwB8fkh9hLWp/divQsL1jBYEWqsNZs37YbfuWofzAD5/SFN2KTMqEgn2uPVEafkUXof7Hz1GHoX35tDSafNxTIksKz5Mw0vLT6H/vIUsJFdg33e8JDr06Ogez3Hfc4RP7XpzjAgdUoVcEhF4VKFo6r7ImETUWQ4E5mCmWnqKPiqrGrVcwP0xVtz09yeE45eAVX89Sgn4Brxv48TDnKtKYxU2NhEKRJyL2EU0QLjW80tyGl/zzEzhn/QpjJqfKPB5REP41oPBfER/HVHoXDinelcNXYUkBUqbtcKexNI8wPHA5/TcQKBgQDb9UsmJiDHvlaTZS/Ge7+6wcn2zDT/o0S3ZRLzJeWcNLBonohiOt0FBvj/NvttqDGK53yjZZFRfWqJaG2pFJqhY/PLiX4hlt6nAQkzUDxpI08sU0Q4IgEomJUvXmicEJNHR1L4YYyPyv5nDB3fmGsyUTdbeWOFJRIxr25lOS1MNQKBgQDVcRPjsXJSDuRNOo/GnAGZiYlfAvxeXfkIsB45vbfEp+9HcvA7BZZP3b2JbMDIj2JcpYo5+W/Z3uU5nBVU0WjtXZ79uSvV/14oH95z5uAIm6JtIWgVGC31fGyPl8RLgn00fYa6MGXWUo82WnLYDBmC3wa7Xfar/dKVUQBZWGsJOwKBgC3+dOBdSK17146qsfrHFahvrVO2D78E3PGcaQH/AqxPODQoMkyYEm9ird5wGNMtQG7TSPTB4Ekx+H0TIRsh+9OTmv8MmRtc+OHjDZF1TayOfZe/MZyrP6LFhSyKiUVZEfLtryPRAhtvTxMtLXH75S54XSL7lxvYTJ2nGWaBNj+hAoGAf67ujAZp8ibQcla3DcPjvRqm3/ykRjuHL6hT3IzeszkXDjH2/gfgnJR0vxIc3Z3Q5MVuxDGwtK//hpAVvrCrSVv5MaUlURY8GFrAM6uIl/2qlAgpAH1/eNxfASN0HQvJpK32+8jaEvU+kPBYxV+vnzeWCl4yoz7rS8GyKMCY/2MCgYEAtDBPMVHPbjauWjzIjXlaJTMlAoWOmnTB36rkNC9U0SRCxvP6hcLTgvDv1TR2+mKM5tQS2/C1XC3zajUFhIqR0funEEWqUoKfZEWqagOILxpIaeida2MwWXNYLQIAimoMQ6CbqdHsPOrQ/ZtxqlwDUnPYumRawSyxsGIThDnN4oc=";
        return (RSAPrivateKey)
                KeyFactory.getInstance("RSA")
                        .generatePrivate(
                                new PKCS8EncodedKeySpec(Base64.getDecoder().decode(privateKey)));
    }

    private ECKey getEsPrivateKey() throws ParseException {
        String privateKeyJwk =
                "{\"kty\":\"EC\",\"d\":\"aWs8vn4m77PZ_SFMqpGgDlmgBCvtccsV1sE8UCmWPm0\",\"crv\":\"P-256\",\"x\":\"QW9GkrKtsARqx2stUsf1EwBmFaORYzheMbCq28oAIsg\",\"y\":\"DM7AJ0OmO9EduJoQEzGVT0pNKuzwGr1KI1r3fuU85oQ\"}";
        return ECKey.parse(privateKeyJwk);
    }
}
